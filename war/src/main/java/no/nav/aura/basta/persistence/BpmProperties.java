package no.nav.aura.basta.persistence;

import no.nav.aura.basta.rest.OrderDetailsDO;

public abstract class BpmProperties {

    public static final String BPM_CELL_DATASOURCE_ALIAS = "bpmCellDatasourceAlias";
    public static final String BPM_COMMON_DATASOURCE_ALIAS = "bpmCommonDatasourceAlias";

    private BpmProperties() {
    }

    public static void apply(OrderDetailsDO source, Settings target) {
        target.setProperty(BPM_COMMON_DATASOURCE_ALIAS, source.getCommonDatasource());
        target.setProperty(BPM_CELL_DATASOURCE_ALIAS, source.getCellDatasource());
    }

    public static void apply(Settings source, OrderDetailsDO target) {
        target.setCommonDatasource(source.getProperty(BPM_COMMON_DATASOURCE_ALIAS).orNull());
        target.setCellDatasource(source.getProperty(BPM_CELL_DATASOURCE_ALIAS).orNull());
    }

}
