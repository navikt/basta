package no.nav.aura.basta.domain.input.database;

import java.util.Map;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.Input;

public class DBOrderInput extends MapOperations implements Input {

    public static final String APPLICATION_NAME = "applicationName";
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";
    public static final String DATABASE_NAME = "databaseName";
    public static final String TEMPLATE_URI = "templateURI";
    public static final String ZONE_URI = "zoneURI";

    public DBOrderInput(Map<String, String> map) {
        super(map);
    }

    @Override
    public String getOrderDescription() {
        return "Oracle";
    }

}
