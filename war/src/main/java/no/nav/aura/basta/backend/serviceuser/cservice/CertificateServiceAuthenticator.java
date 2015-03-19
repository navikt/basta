package no.nav.aura.basta.backend.serviceuser.cservice;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.aura.basta.backend.serviceuser.AdminUserConfiguration;
import no.nav.aura.basta.backend.serviceuser.ScepConnectionInfo;

public class CertificateServiceAuthenticator extends Authenticator {
    private static Logger log = LoggerFactory.getLogger(CertificateServiceAuthenticator.class);

    private AdminUserConfiguration configuration = new AdminUserConfiguration();

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        URL url = getRequestingURL();
        for (String domain : configuration.getDomains()) {
            ScepConnectionInfo connInfo = configuration.getConfigForDomain(domain);
            if (url.toString().startsWith(connInfo.getSigningURL())) {
                log.info("Username for " + url.toString() + " is: " + connInfo.getUsername());
                return new PasswordAuthentication(connInfo.getUsername(), connInfo.getPassword().toCharArray());
            }
        }
        log.info("No username found for URL: " + url.toString());
        return null;
    }

}
