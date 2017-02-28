package no.nav.aura.basta.backend.serviceuser;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;

import no.nav.aura.basta.domain.input.Domain;

public class SecurityConfigurationTest {

    private static SecurityConfiguration config;

    @BeforeClass
    public static void setup() {
        System.setProperty("scep.test.local.url", "https://scep.test");
        System.setProperty("scep.test.local.username", "test");
        System.setProperty("scep.test.local.password", "passtest");
        System.setProperty("scep.adeo.no.url", "https://scep.adeo");
        System.setProperty("scep.adeo.no.username", "adeo");
        System.setProperty("scep.adeo.no.password", "passprod");
        System.setProperty("scep.preprod.local.url", "https://scep.preprod");
        System.setProperty("scep.preprod.local.username", "preprod");
        System.setProperty("scep.preprod.local.password", "passpreprod");
        config = new SecurityConfiguration();
    }

    @Test
    public void checkAdeo() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.Adeo);
        assertEquals(URI.create("ldap://ldapgw.adeo.no:636"), adeoConfig.getLdapUrl());
        assertEquals("adeo", adeoConfig.getUsername());
        assertEquals("passprod", adeoConfig.getPassword());
        assertEquals(URI.create("https://scep.adeo"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkOera() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.Oera);
        assertEquals(URI.create("ldap://ldapgw.oera.no:636"), adeoConfig.getLdapUrl());
        assertEquals("adeo", adeoConfig.getUsername());
        assertEquals("passprod", adeoConfig.getPassword());
        assertEquals(URI.create("https://scep.adeo"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkPreprod() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.PreProd);
        assertEquals(URI.create("ldap://ldapgw.preprod.local:636"), adeoConfig.getLdapUrl());
        assertEquals("preprod", adeoConfig.getUsername());
        assertEquals("passpreprod", adeoConfig.getPassword());
        assertEquals(URI.create("https://scep.preprod"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkOeraQ() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.OeraQ);
        assertEquals(URI.create("ldap://ldapgw.oera-q.local:636"), adeoConfig.getLdapUrl());
        assertEquals("preprod", adeoConfig.getUsername());
        assertEquals("passpreprod", adeoConfig.getPassword());
        assertEquals(URI.create("https://scep.preprod"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkTestLocal() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.TestLocal);
        assertEquals(URI.create("ldap://ldapgw.test.local:636"), adeoConfig.getLdapUrl());
        assertEquals("test", adeoConfig.getUsername());
        assertEquals("passtest", adeoConfig.getPassword());
        assertEquals(URI.create("https://scep.test"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkOeraT() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.OeraT);
        assertEquals(URI.create("ldap://ldapgw.oera-t.local:636"), adeoConfig.getLdapUrl());
        assertEquals("test", adeoConfig.getUsername());
        assertEquals("passtest", adeoConfig.getPassword());
        assertEquals(URI.create("https://scep.test"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkDevilloShouldGiveTestLocalSettings() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.Devillo);
        assertEquals(URI.create("ldap://ldapgw.test.local:636"), adeoConfig.getLdapUrl());
        assertEquals("test", adeoConfig.getUsername());
        assertEquals("passtest", adeoConfig.getPassword());
        assertEquals(URI.create("https://scep.test"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkDevilloSBSShouldGiveTestLocalSettings() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.DevilloSBS);
        assertEquals(URI.create("ldap://ldapgw.test.local:636"), adeoConfig.getLdapUrl());
        assertEquals("test", adeoConfig.getUsername());
        assertEquals("passtest", adeoConfig.getPassword());
        assertEquals(URI.create("https://scep.test"), adeoConfig.getSigningURL());
    }

    @Test
    public void checkiAppShouldGiveTestLocalSettings() {
        SecurityConfigElement adeoConfig = config.getConfigForDomain(Domain.iApp);
        assertEquals(URI.create("ldap://ldapgw.test.local:636"), adeoConfig.getLdapUrl());
        assertEquals("test", adeoConfig.getUsername());
        assertEquals("passtest", adeoConfig.getPassword());
        assertEquals(URI.create("https://scep.test"), adeoConfig.getSigningURL());
    }
}
