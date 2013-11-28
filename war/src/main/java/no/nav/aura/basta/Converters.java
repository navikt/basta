package no.nav.aura.basta;

import java.util.Arrays;

import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.util.SerializablePredicate;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.envClass;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

public class Converters {

    @SuppressWarnings("serial")
    public static String domainFrom(final EnvironmentClass environmentClass, final Zone zone) {
        return FluentIterable.from(Arrays.asList(DomainDO.values())).filter(new SerializablePredicate<DomainDO>() {
            public boolean test(DomainDO domain) {
                try {
                    return domain.getClass().getField(domain.name()).getAnnotation(Deprecated.class) == null
                            && domain.isInZone(fasitZoneFromLocal(zone))
                            && domain.getEnvironmentClass() == fasitEnvironmentClassFromLocal(environmentClass);
                } catch (NoSuchFieldException | SecurityException e) {
                    throw new RuntimeException(e);
                }
            }
        }).first().get().getFqn();
    }

    public static EnvClass fasitEnvironmentClassFromLocal(EnvironmentClass environmentClass) {
        return EnvClass.valueOf(environmentClass.name());
    }

    public static no.nav.aura.envconfig.client.DomainDO.Zone fasitZoneFromLocal(Zone zone) {
        return no.nav.aura.envconfig.client.DomainDO.Zone.valueOf(zone.name().toUpperCase());
    }

    private static ImmutableMap<EnvironmentClass, envClass> orchestratorEnvironmentClassFromLocalMap = ImmutableMap.of(
            EnvironmentClass.u, envClass.utv, EnvironmentClass.t, envClass.test, EnvironmentClass.q, envClass.preprod, EnvironmentClass.p, envClass.prod);

    public static envClass orchestratorEnvironmentClassFromLocal(EnvironmentClass environmentClass) {
        return orchestratorEnvironmentClassFromLocalMap.get(environmentClass);
    }

}
