package no.nav.aura.basta.domain.input.bigip;

import no.nav.aura.appconfig.Application;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.Zone;

import java.util.Map;

public class BigIPOrderInput extends MapOperations implements Input {

    public static final String APPLICATION_NAME = "application";
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";
    public static final String VIRTUAL_SERVER = "virtualServer";

    public static final String ZONE = "zone";

    public BigIPOrderInput(Map<String, String> map) {
        super(map);
    }

    @Override
    public String getOrderDescription() {
        return "BigIP";
    }

    public EnvironmentClass getEnvironmentClass() {
        return getEnumOrNull(EnvironmentClass.class, ENVIRONMENT_CLASS);
    }

    public String getEnvironmentName() {
        return get(ENVIRONMENT_NAME);
    }

    public Zone getZone() {
        return getEnumOrNull(Zone.class, ZONE);
    }

    public String getApplicationName(){
        return get(APPLICATION_NAME);
    }


    public String getVirtualServer() {
        return get(VIRTUAL_SERVER);
    }
}
