package no.nav.aura.basta.persistence;

import no.nav.aura.basta.rest.OrderDetailsDO;

public abstract class FasitProperties {

    public static final String BPM_CELL_DATASOURCE_ALIAS = "bpmCellDatasourceAlias";
    public static final String BPM_COMMON_DATASOURCE_ALIAS = "bpmCommonDatasourceAlias";
    public static final String BPM_FAILOVER_DATASOURCE_ALIAS = "bpmFailoverDatasourceAlias";
    public static final String WAS_ADMIN_CREDENTIAL_ALIAS = "wasAdminCredentialAlias";
    public static final String BPM_SERVICE_CREDENTIAL_ALIAS = "bpmServiceCredential";
    public static final String LDAP_USER_CREDENTIAL_ALIAS = "wasLdapUser";

    private FasitProperties() {
    }

    public static void apply(OrderDetailsDO source, Settings target) {
        target.setProperty(BPM_CELL_DATASOURCE_ALIAS, source.getCellDatasource());
        target.setProperty(BPM_COMMON_DATASOURCE_ALIAS, source.getCommonDatasource());
        target.setProperty(BPM_FAILOVER_DATASOURCE_ALIAS, source.getFailoverDatasource());
        target.setProperty(WAS_ADMIN_CREDENTIAL_ALIAS, source.getWasAdminCredential());
        target.setProperty(BPM_SERVICE_CREDENTIAL_ALIAS, source.getBpmServiceCredential());
        target.setProperty(LDAP_USER_CREDENTIAL_ALIAS, source.getLdapUserCredential());
    }

    public static void apply(Settings source, OrderDetailsDO target) {
        target.setCellDatasource(source.getProperty(BPM_CELL_DATASOURCE_ALIAS).orNull());
        target.setCommonDatasource(source.getProperty(BPM_COMMON_DATASOURCE_ALIAS).orNull());
        target.setCommonDatasource(source.getProperty(BPM_FAILOVER_DATASOURCE_ALIAS).orNull());
        target.setWasAdminCredential(source.getProperty(WAS_ADMIN_CREDENTIAL_ALIAS).orNull());
        target.setBpmServiceCredential(source.getProperty(BPM_SERVICE_CREDENTIAL_ALIAS).orNull());
        target.setLdapUserCredential(source.getProperty(LDAP_USER_CREDENTIAL_ALIAS).orNull());
    }

}
