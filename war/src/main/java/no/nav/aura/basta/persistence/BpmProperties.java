package no.nav.aura.basta.persistence;

import no.nav.aura.basta.rest.OrderDetailsDO;

public abstract class BpmProperties {

    public static final String BPM_CELL_DATASOURCE_ALIAS = "bpmCellDatasourceAlias";
    public static final String BPM_COMMON_DATASOURCE_ALIAS = "bpmCommonDatasourceAlias";
    public static final String WAS_ADMIN_CREDENTIAL_ALIAS = "wasAdminCredentialAlias";
    public static final String BPM_SERVICE_CREDENTIAL_ALIAS = "bpmServiceCredential";

    private BpmProperties() {
    }

    public static void apply(OrderDetailsDO source, Settings target) {
        target.setProperty(BPM_COMMON_DATASOURCE_ALIAS, source.getCommonDatasource());
        target.setProperty(BPM_CELL_DATASOURCE_ALIAS, source.getCellDatasource());
        target.setProperty(WAS_ADMIN_CREDENTIAL_ALIAS, source.getWasAdminCredential());
        target.setProperty(BPM_SERVICE_CREDENTIAL_ALIAS, source.getBpmServiceCredential());
    }

    public static void apply(Settings source, OrderDetailsDO target) {
        target.setCommonDatasource(source.getProperty(BPM_COMMON_DATASOURCE_ALIAS).orNull());
        target.setCellDatasource(source.getProperty(BPM_CELL_DATASOURCE_ALIAS).orNull());
        target.setWasAdminCredential(source.getProperty(WAS_ADMIN_CREDENTIAL_ALIAS).orNull());
        target.setBpmServiceCredential(source.getProperty(BPM_SERVICE_CREDENTIAL_ALIAS).orNull());
    }

}
