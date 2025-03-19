package no.nav.aura.basta.domain.input.serviceuser;

import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.ResourceTypeDO;
import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.backend.serviceuser.CustomServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.FasitServiceUserAccount;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;

import java.util.Map;

public class ServiceUserOrderInput extends MapOperations implements Input {

    public static final String APPLICATION = "application";
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String ZoneType = "zone";
    public static final String hasAbacAccess = "abacAccess";
    public static final String hasStsAccess = "stsAccess";
    public static final String customUserAccountName = "username";

    public ServiceUserOrderInput(Map<String, String> map) {
        super(map);
    }

    public String getApplication() {
        return get(APPLICATION);
    }

    private Boolean hasAbacAccess() { return Boolean.valueOf(get(hasAbacAccess)); }

    private Boolean hasStsAccess() { return Boolean.valueOf(get(hasStsAccess)); }

    public EnvironmentClass getEnvironmentClass() {
        return getEnumOrNull(EnvironmentClass.class, ENVIRONMENT_CLASS);
    }

    public ResourceTypeDO getResultType() {
        return getEnumOrNull(ResourceTypeDO.class, RESOURCE_TYPE);
    }

    public void setResultType(ResourceTypeDO resultType) {
        put(RESOURCE_TYPE, resultType.name());
    }

    public CustomServiceUserAccount getCustomUserAccount() {
        CustomServiceUserAccount serviceUserAccount = new CustomServiceUserAccount(getEnvironmentClass(), getZone(), get(customUserAccountName));
        serviceUserAccount.setAbacAccess(hasAbacAccess());
        serviceUserAccount.setStsAccess(hasStsAccess());
        return  serviceUserAccount;
    }

    public FasitServiceUserAccount getUserAccount() {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(getEnvironmentClass(), getZone(), getApplication
                ());
        serviceUserAccount.setAbacAccess(hasAbacAccess());
        serviceUserAccount.setStsAccess(hasStsAccess());
        return serviceUserAccount;
    }

    public Zone getZone() {
        return getEnumOrNull(Zone.class, ZoneType);
    }

    @Override
    public String getOrderDescription() {
        return getResultType().name();
    }

}
