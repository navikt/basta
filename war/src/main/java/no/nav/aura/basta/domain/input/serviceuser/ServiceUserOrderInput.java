package no.nav.aura.basta.domain.input.serviceuser;

import java.util.Map;

import no.nav.aura.basta.backend.certificate.ad.ServiceUserAccount;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.Zone;

public class ServiceUserOrderInput extends MapOperations implements Input {

    /* VM Order Input */
    public static final String APPLICATION = "application";
    public static final String ENVIRONMENT_CLASS = "envClass";
    public static final String ZONE = "zone";

    public ServiceUserOrderInput(Map map) {
        super(map);
    }

    public String getAppliation() {
        return get(APPLICATION);
    }

    public Zone getZone() {
        return getEnumOrNull(Zone.class, ZONE);
    }

    public EnvironmentClass getEnvironmentClass() {
        return getEnumOrNull(EnvironmentClass.class, ENVIRONMENT_CLASS);
    }

    public ServiceUserAccount getUserAccount() {
        return new ServiceUserAccount(getEnvironmentClass(), getZone(), getAppliation());

    }

    @Override
    public String getOrderDescription() {
        return "Serviceuser";
    }

}
