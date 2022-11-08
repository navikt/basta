package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomServiceUserAccountTest {

    @Test
    public void prodShouldGiveStandardDNAndCN() {
        String standardCn = "srvusername";
        assertLdapPaths(new CustomServiceUserAccount(EnvironmentClass.p, Zone.fss, "srvusername"), Domain.Adeo, "DC=adeo,DC=no", standardCn);
        assertLdapPaths(new CustomServiceUserAccount(EnvironmentClass.p, Zone.sbs, "srvusername"), Domain.Oera, "DC=oera,DC=no", standardCn);
    }

    @Test
    public void qShouldGiveStandardDNAndCN() {
        String standardCn = "srvusername";
        assertLdapPaths(new CustomServiceUserAccount(EnvironmentClass.q, Zone.fss, "srvusername"), Domain.PreProd, "DC=preprod,DC=local", standardCn);
        assertLdapPaths(new CustomServiceUserAccount(EnvironmentClass.q, Zone.sbs, "srvusername"), Domain.OeraQ, "DC=oera-q,DC=local", standardCn);
    }

    @Test
    public void tShouldGiveStandardDNAndCN() {
        String standardCn = "srvusername";
        assertLdapPaths(new CustomServiceUserAccount(EnvironmentClass.t, Zone.fss, "srvusername"), Domain.TestLocal, "DC=test,DC=local", standardCn);
        assertLdapPaths(new CustomServiceUserAccount(EnvironmentClass.t, Zone.sbs, "srvusername"), Domain.OeraT, "DC=oera-t,DC=local", standardCn);
    }

    @Test
    public void uShouldGiveTestlocalDNAndCustomCN() {
        assertLdapPaths(new CustomServiceUserAccount(EnvironmentClass.u, Zone.fss, "srvusername"), Domain.Devillo, "DC=test,DC=local", "srvusername");
        assertLdapPaths(new CustomServiceUserAccount(EnvironmentClass.u, Zone.sbs, "srvusername"), Domain.DevilloSBS, "DC=test,DC=local", "srvusername");
    }

    private void assertLdapPaths(CustomServiceUserAccount user, Domain expectedDomain, String expectedDn, String expectedCn) {
        assertEquals(expectedDomain, user.getDomain(), "expected domain");
        assertEquals(expectedCn, user.getUserAccountName(), "cn");
        assertEquals(expectedDn, user.getBaseDN(), "baseDn");
        assertEquals("OU=ApplAccounts,OU=ServiceAccounts," + expectedDn, user.getServiceUserSearchBase(), "Ldap search base for user");
        assertEquals("cn=" + expectedCn + ",OU=ApplAccounts,OU=ServiceAccounts," + expectedDn, user.getServiceUserDN(), "ldap full path");
    }
}
