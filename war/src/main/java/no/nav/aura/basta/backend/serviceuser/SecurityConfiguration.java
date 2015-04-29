package no.nav.aura.basta.backend.serviceuser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityConfiguration {

    private static Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);
    private final Map<String, SecurityConfigElement> configElements = new HashMap<>();

    public SecurityConfiguration() {
        configElements.put("adeo.no", createConfigElement("adeo.no"));
        configElements.put("preprod.local", createConfigElement("preprod.local"));
        configElements.put("test.local", createConfigElement("test.local"));
    }

    public SecurityConfigElement getConfigForDomain(Domain domain) {
        String caDomain = domain.getSecurityDomain();
        log.info("CA Server domain for " + domain + " is: " + caDomain);
        return configElements.get(caDomain);
    }

    public Collection<SecurityConfigElement> getConfigElements() {
        return configElements.values();
    }

    private SecurityConfigElement createConfigElement(String caDomain) {

        final String scepServerURLProperty = "scep." + caDomain + ".url";
        final String scepServerUsernameProperty = "scep." + caDomain + ".username";
        final String scepServerPasswordProperty = "scep." + caDomain + ".password";

        String scepServerURL = System.getProperty(scepServerURLProperty);
        if (scepServerURL == null)
            throw new IllegalArgumentException("Environment property not defined: " + scepServerURLProperty);

        String username = System.getProperty(scepServerUsernameProperty);
        if (username == null)
            throw new IllegalArgumentException("Environment property not defined: " + scepServerUsernameProperty);

        String password = System.getProperty(scepServerPasswordProperty);
        if (password == null)
            throw new IllegalArgumentException("Environment property not defined: " + scepServerPasswordProperty);

        return new SecurityConfigElement(scepServerURL, username, password);
    }

}
