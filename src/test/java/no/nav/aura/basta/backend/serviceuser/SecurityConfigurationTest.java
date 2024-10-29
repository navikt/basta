package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.domain.input.Domain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class SecurityConfigurationTest {

    private static SecurityConfiguration config;

    @BeforeAll
    public static void setup() {
        System.setProperty("security_CA_test_url", "https://scep.test");
        System.setProperty("security_CA_test_username", "test");
        System.setProperty("security_CA_test_password", "passtest");
        System.setProperty("security_CA_adeo_url", "https://scep.adeo");
        System.setProperty("security_CA_adeo_username", "adeo");
        System.setProperty("security_CA_adeo_password", "passprod");
        System.setProperty("security_CA_preprod_url", "https://scep.preprod");
        System.setProperty("security_CA_preprod_username", "preprod");
        System.setProperty("security_CA_preprod_password", "passpreprod");
        config = new SecurityConfiguration();
    }

    @Test
    public void checkAdeo() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.Adeo);
        Assertions.assertEquals(URI.create("ldap://ldapgw.adeo.no:636"), adeoConfig.getLdapUrl());
        Assertions.assertEquals("adeo", adeoConfig.getUsername());
        Assertions.assertEquals("passprod", adeoConfig.getPassword());
        Assertions.assertEquals(URI.create("https://scep.adeo"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkOera() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.Oera);
        Assertions.assertEquals(URI.create("ldap://ldapgw.oera.no:636"), adeoConfig.getLdapUrl());
        Assertions.assertEquals("adeo", adeoConfig.getUsername());
        Assertions.assertEquals("passprod", adeoConfig.getPassword());
        Assertions.assertEquals(URI.create("https://scep.adeo"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkPreprod() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.PreProd);
        Assertions.assertEquals(URI.create("ldap://ldapgw.preprod.local:636"), adeoConfig.getLdapUrl());
        Assertions.assertEquals("preprod", adeoConfig.getUsername());
        Assertions.assertEquals("passpreprod", adeoConfig.getPassword());
        Assertions.assertEquals(URI.create("https://scep.preprod"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkOeraQ() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.OeraQ);
        Assertions.assertEquals(URI.create("ldap://ldapgw.oera-q.local:636"), adeoConfig.getLdapUrl());
        Assertions.assertEquals("preprod", adeoConfig.getUsername());
        Assertions.assertEquals("passpreprod", adeoConfig.getPassword());
        Assertions.assertEquals(URI.create("https://scep.preprod"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkTestLocal() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.TestLocal);
        Assertions.assertEquals(URI.create("ldap://ldapgw.test.local:636"), adeoConfig.getLdapUrl());
        Assertions.assertEquals("test", adeoConfig.getUsername());
        Assertions.assertEquals("passtest", adeoConfig.getPassword());
        Assertions.assertEquals(URI.create("https://scep.test"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkOeraT() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.OeraT);
        Assertions.assertEquals(URI.create("ldap://ldapgw.oera-t.local:636"), adeoConfig.getLdapUrl());
        Assertions.assertEquals("test", adeoConfig.getUsername());
        Assertions.assertEquals("passtest", adeoConfig.getPassword());
        Assertions.assertEquals(URI.create("https://scep.test"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkDevilloShouldGiveTestLocalSettings() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.Devillo);
        Assertions.assertEquals(URI.create("ldap://ldapgw.test.local:636"), adeoConfig.getLdapUrl());
        Assertions.assertEquals("test", adeoConfig.getUsername());
        Assertions.assertEquals("passtest", adeoConfig.getPassword());
        Assertions.assertEquals(URI.create("https://scep.test"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkDevilloSBSShouldGiveTestLocalSettings() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.DevilloSBS);
        Assertions.assertEquals(URI.create("ldap://ldapgw.test.local:636"), adeoConfig.getLdapUrl());
        Assertions.assertEquals("test", adeoConfig.getUsername());
        Assertions.assertEquals("passtest", adeoConfig.getPassword());
        Assertions.assertEquals(URI.create("https://scep.test"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkiAppShouldGiveTestLocalSettings() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.iApp);
        Assertions.assertEquals(URI.create("ldap://ldapgw.adeo.no:636"), adeoConfig.getLdapUrl());
        Assertions.assertEquals("adeo", adeoConfig.getUsername());
        Assertions.assertEquals("passprod", adeoConfig.getPassword());
        Assertions.assertEquals(URI.create("https://scep.adeo"), adeoConfig.getSigningURL());
    }
}