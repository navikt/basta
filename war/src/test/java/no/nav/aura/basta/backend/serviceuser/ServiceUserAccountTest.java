package no.nav.aura.basta.backend.serviceuser;

import static org.junit.Assert.*;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;

import org.junit.Test;

public class ServiceUserAccountTest {

    @Test
    public void prodShouldGiveStandardDNAndCN() {
        String standardCn = "srvapp";
        assertLdapPaths(new ServiceUserAccount(EnvironmentClass.p, Zone.fss, "app"), Domain.Adeo, "DC=adeo,DC=no", standardCn);
        assertLdapPaths(new ServiceUserAccount(EnvironmentClass.p, Zone.sbs, "app"), Domain.Oera, "DC=oera,DC=no", standardCn);
    }

    @Test
    public void qShouldGiveStandardDNAndCN() {
        String standardCn = "srvapp";
        assertLdapPaths(new ServiceUserAccount(EnvironmentClass.q, Zone.fss, "app"), Domain.PreProd, "DC=preprod,DC=local", standardCn);
        assertLdapPaths(new ServiceUserAccount(EnvironmentClass.q, Zone.sbs, "app"), Domain.OeraQ, "DC=oera-q,DC=local", standardCn);
    }

    @Test
    public void tShouldGiveStandardDNAndCN() {
        String standardCn = "srvapp";
        assertLdapPaths(new ServiceUserAccount(EnvironmentClass.t, Zone.fss, "app"), Domain.TestLocal, "DC=test,DC=local", standardCn);
        assertLdapPaths(new ServiceUserAccount(EnvironmentClass.t, Zone.sbs, "app"), Domain.OeraT, "DC=oera-t,DC=local", standardCn);
    }

    @Test
    public void uShouldGiveTestlocalDNAndCustomCN() {
        assertLdapPaths(new ServiceUserAccount(EnvironmentClass.u, Zone.fss, "app"), Domain.Devillo, "DC=test,DC=local", "srvapp_u");
        assertLdapPaths(new ServiceUserAccount(EnvironmentClass.u, Zone.sbs, "app"), Domain.DevilloSBS, "DC=test,DC=local", "srvapp_u");
    }

    private void assertLdapPaths(ServiceUserAccount user, Domain expectedDomain, String expectedDn, String expectedCn) {
        assertEquals("expected domain", expectedDomain, user.getDomain());
        assertEquals("cn", expectedCn, user.getUserAccountName());
        assertEquals("baseDn", expectedDn, user.getBaseDN());
        assertEquals("ldap searchbase for user", "OU=ApplAccounts,OU=ServiceAccounts," + expectedDn, user.getServiceUserSearchBase());
        assertEquals("ldap full path", "cn=" + expectedCn + ",OU=ApplAccounts,OU=ServiceAccounts," + expectedDn, user.getServiceUserDN());
    }

    @Test
    public void longUserNamesShouldBeTruncatedAndlowertcased() {
        assertEquals("srvshort", new ServiceUserAccount(EnvironmentClass.p, Zone.fss, "sHoRT").getUserAccountName());
        assertEquals("srvappwithverylong", new ServiceUserAccount(EnvironmentClass.p, Zone.fss, "appwithverylongname").getUserAccountName());
    }

    @Test
    public void longUserNamesShouldBeTruncatedAndlowertcasedAndPostfixedWithU() {
        assertEquals("srvshort_u", new ServiceUserAccount(EnvironmentClass.u, Zone.fss, "shORT").getUserAccountName());
        assertEquals("srvappwithverylo_u", new ServiceUserAccount(EnvironmentClass.u, Zone.fss, "appwithverylongname").getUserAccountName());
    }
}
