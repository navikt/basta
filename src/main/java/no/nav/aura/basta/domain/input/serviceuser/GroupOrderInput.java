package no.nav.aura.basta.domain.input.serviceuser;

import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.AdGroupUsage;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;

import java.util.Map;

public class GroupOrderInput extends MapOperations implements Input {

    public static final String APPLICATION = "application";
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String GROUP_USAGE = "groupUsage";
    public static final String ZONE_TYPE = "zone";

    public GroupOrderInput(Map<String, String> map) {
        super(map);
    }

    public String getApplication() {
        return get(APPLICATION);
    }

    public EnvironmentClass getEnvironmentClass() {
        return getEnumOrNull(EnvironmentClass.class, ENVIRONMENT_CLASS);
    }

    public Zone getZone() {
        return getEnumOrNull(Zone.class, ZONE_TYPE);
    }

    public AdGroupUsage getGroupUsage() { return getEnumOrNull(AdGroupUsage.class, GROUP_USAGE); }

    @Override
    public String getOrderDescription() {
        return "AD Group";
    }

}
