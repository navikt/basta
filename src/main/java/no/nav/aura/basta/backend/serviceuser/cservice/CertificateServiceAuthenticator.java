package no.nav.aura.basta.backend.serviceuser.cservice;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import no.nav.aura.basta.backend.serviceuser.SecurityConfiguration;
import no.nav.aura.basta.backend.serviceuser.SecurityConfigElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertificateServiceAuthenticator extends Authenticator {
    private static Logger log = LoggerFactory.getLogger(CertificateServiceAuthenticator.class);

    private SecurityConfiguration configuration = new SecurityConfiguration();

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        URL url = getRequestingURL();

        for (SecurityConfigElement connInfo : configuration.getConfigElements()) {
            if (url.toString().startsWith(connInfo.getSigningURL().toString())) {
				log.info("Username for {} is {} ", url, connInfo.getUsername());
                return new PasswordAuthentication(connInfo.getUsername(), connInfo.getPassword().toCharArray());
            }
        }
        log.error("No username found for URL: {}", url);
        return null;
    }
}
