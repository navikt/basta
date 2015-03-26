package no.nav.aura.basta.domain.input.serviceuser;

import java.util.Map;

import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.envconfig.client.ResourceTypeDO;

public class ServiceUserOrderInput extends MapOperations implements Input {

    public static final String APPLICATION = "application";
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ZONE = "zone";
    public static final String RESOURCE_TYPE = "resourceType";

    public ServiceUserOrderInput(Map<String, String> map) {
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

    public ResourceTypeDO getResultType() {
        return getEnumOrNull(ResourceTypeDO.class, RESOURCE_TYPE);
    }

    public void setResultType(ResourceTypeDO resultType) {
        put(RESOURCE_TYPE, resultType.name());
    }

    public ServiceUserAccount getUserAccount() {
        System.out.println(getEnvironmentClass() + " " + getZone());
        return new ServiceUserAccount(getEnvironmentClass(), getZone(), getAppliation());

    }

    @Override
    public String getOrderDescription() {
        return getResultType().name();
    }

}
