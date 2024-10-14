package no.nav.aura.basta.spring;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.bettercloud.vault.response.LookupResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

@Configuration
public class VaultConfig {
    private static final Logger logger = LoggerFactory.getLogger(VaultConfig.class);

    private static final String VAULT_TOKEN_PROPERTY = "VAULT_TOKEN";
    private static final String VAULT_TOKEN_PATH_PROPERTY = "VAULT_TOKEN_PATH";
    private static final int MIN_REFRESH_MARGIN  = 10 * 60 * 1000; // 10 min in ms;

    private final Timer timer = new Timer("VaultScheduler", true);;

    @Bean
    public Vault getVaultClient() throws VaultError, VaultException {
        com.bettercloud.vault.VaultConfig vaultConfig;
        try {
            vaultConfig = new com.bettercloud.vault.VaultConfig()
                    .address(System.getenv().getOrDefault("VAULT_ADDR", "https://vault.adeo.no"))
                    .token(getVaultToken())
                    .openTimeout(5)
                    .readTimeout(30)
                    .sslConfig(new SslConfig().build())
                    .build();
            vaultConfig.getSecretsEnginePathMap().put("oracle", "2");
        } catch (VaultException e) {
            throw new VaultError("Could not instantiate the Vault REST client", e);
        }

        final Vault vault = new Vault(vaultConfig, true, 2);

        // Verify that the token is ok
        LookupResponse lookupSelf;
        try {
            lookupSelf = vault.auth().lookupSelf();
            logger.info("Found a Vault token with TTL " + lookupSelf.getTTL());
        } catch (VaultException e) {
            if (e.getHttpStatusCode() == 403) {
                throw new VaultError("The application's vault token seems to be invalid", e);
            } else {
                throw new VaultError("Could not validate the application's vault token", e);
            }
        }

        if (lookupSelf.isRenewable()) {
            final class RefreshTokenTask extends TimerTask {
                @Override
                public void run() {
                    try {
                        logger.info("Refreshing Vault token (old TTL = " + vault.auth().lookupSelf().getTTL() + " seconds)");
                        AuthResponse response = vault.auth().renewSelf();
                        logger.info("Refreshed Vault token (new TTL = " + vault.auth().lookupSelf().getTTL() + " seconds)");
                        timer.schedule(new RefreshTokenTask(), suggestedRefreshInterval(response.getAuthLeaseDuration() * 1000));
                    } catch (VaultException e) {
                        logger.error("Could not refresh the Vault token", e);

                        // Lets try refreshing again
                        logger.warn("Waiting 5 secs before trying to refresh the Vault token");
                        timer.schedule(new RefreshTokenTask(), 5000);
                    }
                }
            }

            logger.info("Starting a refresh timer on the vault token (TTL = " + lookupSelf.getTTL() + " seconds");
            timer.schedule(new RefreshTokenTask(), suggestedRefreshInterval(lookupSelf.getTTL() * 1000));
        } else {
            logger.warn("Vault token is not renewable");
        }

        return vault;
    }

    private static String getProperty(String propertyName) {
        return System.getProperty(propertyName, System.getenv(propertyName));
    }

    private static String getVaultToken() {
        try {
            if (getProperty(VAULT_TOKEN_PROPERTY) != null && !"".equals(getProperty(VAULT_TOKEN_PROPERTY))) {
                return getProperty(VAULT_TOKEN_PROPERTY);
            } else if (getProperty(VAULT_TOKEN_PATH_PROPERTY) != null) {
                byte[] encoded = Files.readAllBytes(Paths.get(getProperty(VAULT_TOKEN_PATH_PROPERTY)));
                return new String(encoded, "UTF-8").trim();
            } else if (Files.exists(Paths.get("/var/run/secrets/nais.io/vault/vault_token"))) {
                byte[] encoded = Files.readAllBytes(Paths.get("/var/run/secrets/nais.io/vault/vault_token"));
                return new String(encoded, "UTF-8").trim();
            } else {
                throw new RuntimeException("Neither " + VAULT_TOKEN_PROPERTY + " or " + VAULT_TOKEN_PATH_PROPERTY + " is set");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not get a vault token for authentication", e);
        }
    }

    // We should refresh tokens from Vault before they expire, so we add a MIN_REFRESH_MARGIN margin.
    // If the token is valid for less than MIN_REFRESH_MARGIN * 2, we use duration / 2 instead.
    private static long suggestedRefreshInterval(long duration) {
        if (duration < MIN_REFRESH_MARGIN * 2) {
            return duration / 2;
        } else {
            return duration - MIN_REFRESH_MARGIN;
        }
    }

    public static final class VaultError extends Exception {
        public VaultError(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
