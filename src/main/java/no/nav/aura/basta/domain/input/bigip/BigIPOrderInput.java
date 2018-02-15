package no.nav.aura.basta.domain.input.bigip;

import java.util.Map;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.Zone;

public class BigIPOrderInput extends MapOperations implements Input {

    public static final String APPLICATION_NAME = "application";
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";
    public static final String VIRTUAL_SERVER = "virtualserver";
    public static final String CONTEXT_ROOTS = "contextroots";
    public static final String HOSTNAME = "hostname";
    public static final String USE_HOSTNAME_MATCHING = "useHostnameMatching";
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

    public boolean getUseHostnameMatching() {
        return Boolean.parseBoolean(get(USE_HOSTNAME_MATCHING));
    }

    public String getHostname() {
        return get(HOSTNAME);
    }

    public String getApplicationName() {
        return get(APPLICATION_NAME);
    }

    public String getVirtualServer() {
        return get(VIRTUAL_SERVER);
    }

    public String getContextRoots() {
        return get(CONTEXT_ROOTS);
    }
}
