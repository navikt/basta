package no.nav.aura.basta.spring;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LookupResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class VaultConfig {
    private static final Logger logger = LoggerFactory.getLogger(VaultConfig.class);

    private static final String VAULT_TOKEN_PROPERTY = "VAULT_TOKEN";
    private static final String VAULT_TOKEN_PATH_PROPERTY = "VAULT_TOKEN_PATH";

    @Bean
    public Vault getVaultClient() throws VaultError {
        com.bettercloud.vault.VaultConfig vaultConfig = null;
        try {
            vaultConfig = new com.bettercloud.vault.VaultConfig()
                    .address(System.getenv().getOrDefault("VAULT_ADDR", "https://vault.adeo.no"))
                    .token(getVaultToken())
                    .openTimeout(5)
                    .readTimeout(30)
                    .sslConfig(new SslConfig().build())
                    .build();
        } catch (VaultException e) {
            throw new VaultError("Could not instantiate the Vault REST client", e);
        }

        final Vault vault = new Vault(vaultConfig);

        // Verify that the token is ok
        LookupResponse lookupSelf = null;
        try {
            lookupSelf = vault.auth().lookupSelf();
            logger.info("Found a Vault token with TTL " + lookupSelf.getTTL());
            return vault;
        } catch (VaultException e) {
            if (e.getHttpStatusCode() == 403) {
                throw new VaultError("The application's vault token seems to be invalid", e);
            } else {
                throw new VaultError("Could not validate the application's vault token", e);
            }
        }
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

    public static final class VaultError extends Exception {
        public VaultError(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
