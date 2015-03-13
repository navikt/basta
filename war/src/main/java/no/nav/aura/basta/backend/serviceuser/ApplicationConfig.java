package no.nav.aura.basta.backend.serviceuser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig {

    private static Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    static private String htmlHeader = "<html><head><title>Certificate Signing Service</title></head><body>";
    static private String htmlFooter = "</body></html>";

    static private Properties domainProperties = new Properties();
    static {
        try {
            InputStream is = CertificateRestService.class.getResourceAsStream("/certificate/domains.properties");
            domainProperties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getCADomainForDomain(String domain) {
        return domainProperties.getProperty(domain);
    }

    public static ScepConnectionInfo getServerForDomain(String domain) {
        String caDomain = domainProperties.getProperty(domain);
        if (caDomain == null)
            return null;
        log.info("domain {} {} ", caDomain, domain);
        final String scepServerURLProperty = "scep." + caDomain + ".url";
        final String scepServerUsernameProperty = "scep." + caDomain + ".username";
        final String scepServerPasswordProperty = "scep." + caDomain + ".password";

        log.info("CA Server domain for " + domain + " is: " + caDomain);

        String scepServerURL = System.getProperty(scepServerURLProperty);
        if (scepServerURL == null)
            throw new RuntimeException("Environment property not defined: " + scepServerURLProperty);

        String username = System.getProperty(scepServerUsernameProperty);
        if (username == null)
            throw new RuntimeException("Environment property not defined: " + scepServerUsernameProperty);

        String password = System.getProperty(scepServerPasswordProperty);
        if (password == null)
            throw new RuntimeException("Environment property not defined: " + scepServerPasswordProperty);

        return new ScepConnectionInfo(scepServerURL, username, password);
    }

    public static Set<String> getDomains() {
        Set<String> domains = new HashSet<String>();

        for (Entry<Object, Object> entry : domainProperties.entrySet()) {
            String domain = entry.getKey().toString();
            domains.add(domain);
        }

        return domains;
    }

    public static String getHtmlHeader() {
        return htmlHeader;
    }

    public static String getHtmlFooter() {
        return htmlFooter;
    }

}
