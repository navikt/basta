package no.nav.aura.basta.domain.input.database;

import java.util.Map;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.Input;

public class DBOrderInput extends MapOperations implements Input {

    public static final String APPLICATION_MAPPING_NAME = "applicationMappingName";
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";

    public DBOrderInput(Map<String, String> map) {
        super(map);
    }

    @Override
    public String getOrderDescription() {
        return "Oracle";
    }

}
