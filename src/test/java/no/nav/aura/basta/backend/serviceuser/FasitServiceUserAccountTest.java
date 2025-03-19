package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FasitServiceUserAccountTest {

    @Test
    public void prodShouldGiveStandardDNAndCN() {
        String standardCn = "srvapp";
        assertLdapPaths(new FasitServiceUserAccount(EnvironmentClass.p, Zone.fss, "app"), Domain.Adeo, "DC=adeo,DC=no", standardCn);
        assertLdapPaths(new FasitServiceUserAccount(EnvironmentClass.p, Zone.sbs, "app"), Domain.Oera, "DC=oera,DC=no", standardCn);
    }

    @Test
    public void qShouldGiveStandardDNAndCN() {
        String standardCn = "srvapp";
        assertLdapPaths(new FasitServiceUserAccount(EnvironmentClass.q, Zone.fss, "app"), Domain.PreProd, "DC=preprod,DC=local", standardCn);
        assertLdapPaths(new FasitServiceUserAccount(EnvironmentClass.q, Zone.sbs, "app"), Domain.OeraQ, "DC=oera-q,DC=local", standardCn);
    }

    @Test
    public void tShouldGiveStandardDNAndCN() {
        String standardCn = "srvapp";
        assertLdapPaths(new FasitServiceUserAccount(EnvironmentClass.t, Zone.fss, "app"), Domain.TestLocal, "DC=test,DC=local", standardCn);
        assertLdapPaths(new FasitServiceUserAccount(EnvironmentClass.t, Zone.sbs, "app"), Domain.OeraT, "DC=oera-t,DC=local", standardCn);
    }

    private void assertLdapPaths(FasitServiceUserAccount user, Domain expectedDomain, String expectedDn, String expectedCn) {
        Assertions.assertEquals(expectedDomain, user.getDomain(), "expected domain");
        Assertions.assertEquals(expectedCn, user.getUserAccountName(), "cn");
        Assertions.assertEquals(expectedDn, user.getBaseDN(), "baseDn");
        Assertions.assertEquals("OU=ApplAccounts,OU=ServiceAccounts," + expectedDn, user.getServiceUserSearchBase(), "ldap searchbase for user");
        Assertions.assertEquals("cn=" + expectedCn + ",OU=ApplAccounts,OU=ServiceAccounts," + expectedDn, user.getServiceUserDN(), "ldap full path");
    }

    @Test
    public void longUserNamesShouldBeTruncatedAndlowertcased() {
        Assertions.assertEquals("srvshort", new FasitServiceUserAccount(EnvironmentClass.p, Zone.fss, "sHoRT").getUserAccountName());
        Assertions.assertEquals("srvappwithverylong", new FasitServiceUserAccount(EnvironmentClass.p, Zone.fss, "appwithverylongname").getUserAccountName());
    }

    @Test
    public void longUserNamesShouldBeTruncatedAndlowertcasedAndPostfixedWithU() {
        Assertions.assertEquals("srvshort_u", new FasitServiceUserAccount(EnvironmentClass.u, Zone.fss, "shORT").getUserAccountName());
        Assertions.assertEquals("srvappwithverylo_u", new FasitServiceUserAccount(EnvironmentClass.u, Zone.fss, "appwithverylongname").getUserAccountName());
    }
}
