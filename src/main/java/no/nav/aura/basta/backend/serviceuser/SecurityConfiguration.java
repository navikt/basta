package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.domain.input.Domain;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static no.nav.aura.basta.domain.input.Domain.*;

public class SecurityConfiguration {

    private final Map<Domain, SecurityConfigElement> configElements = new HashMap<>();

    private final static String SCEP_ADEO = "adeo";
    private final static String SCEP_PREPROD = "preprod";
    private final static String SCEP_TEST = "test";

    public SecurityConfiguration() {
        configElements.put(Adeo, createConfigElement(SCEP_ADEO, createLdapUriFromDomain(Adeo)));
        configElements.put(Oera, createConfigElement(SCEP_ADEO, createLdapUriFromDomain(Oera)));
        configElements.put(iApp, createConfigElement(SCEP_ADEO, createLdapUriFromDomain(Adeo)));
        configElements.put(PreProd, createConfigElement(SCEP_PREPROD, createLdapUriFromDomain(PreProd)));
        configElements.put(OeraQ, createConfigElement(SCEP_PREPROD, createLdapUriFromDomain(OeraQ)));
        configElements.put(TestLocal, createConfigElement(SCEP_TEST, createLdapUriFromDomain(TestLocal)));
        configElements.put(OeraT, createConfigElement(SCEP_TEST, createLdapUriFromDomain(OeraT)));
        configElements.put(Devillo, createConfigElement(SCEP_TEST, createLdapUriFromDomain(TestLocal)));
        configElements.put(DevilloSBS, createConfigElement(SCEP_TEST, createLdapUriFromDomain(TestLocal)));
    }

    private String createLdapUriFromDomain(Domain domain) {
        return "ldap://ldapgw." + domain.getFqn() + ":636";
    }

    public SecurityConfigElement getConfigForDomain(Domain domain) {
        return configElements.get(domain);
    }

    public Collection<SecurityConfigElement> getConfigElements() {
        return configElements.values();
    }

    private SecurityConfigElement createConfigElement(String scp, String ldapUri) {

        final String scepServerURLProperty = "security_CA_" + scp + "_url";
        final String scepServerUsernameProperty = "security_CA_" + scp + "_username";
        final String scepServerPasswordProperty = "security_CA_" + scp + "_password";

        String scepServerURL = System.getProperty(scepServerURLProperty);
        if (scepServerURL == null)
            throw new IllegalArgumentException("Environment property not defined: " + scepServerURLProperty);

        String username = System.getProperty(scepServerUsernameProperty);
        if (username == null)
            throw new IllegalArgumentException("Environment property not defined: " + scepServerUsernameProperty);

        String password = System.getProperty(scepServerPasswordProperty);
        if (password == null)
            throw new IllegalArgumentException("Environment property not defined: " + scepServerPasswordProperty);

        return new SecurityConfigElement(URI.create(scepServerURL), URI.create(ldapUri), username, password);
    }

}