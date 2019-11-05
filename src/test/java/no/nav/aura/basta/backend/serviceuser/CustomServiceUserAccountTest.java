package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        assertEquals("expected domain", expectedDomain, user.getDomain());
        assertEquals("cn", expectedCn, user.getUserAccountName());
        assertEquals("baseDn", expectedDn, user.getBaseDN());
        assertEquals("ldap searchbase for user", "OU=ApplAccounts,OU=ServiceAccounts," + expectedDn, user.getServiceUserSearchBase());
        assertEquals("ldap full path", "cn=" + expectedCn + ",OU=ApplAccounts,OU=ServiceAccounts," + expectedDn, user.getServiceUserDN());
    }
}
