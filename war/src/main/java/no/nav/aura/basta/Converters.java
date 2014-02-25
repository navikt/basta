package no.nav.aura.basta;

import java.util.Arrays;

import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.util.SerializablePredicate;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.OrchestratorEnvClass;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.PlatformTypeDO;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

public class Converters {

    public static String domainFqdnFrom(final EnvironmentClass environmentClass, final Zone zone) {
        return domainFrom(environmentClass, zone).getFqn();
    }

    @SuppressWarnings("serial")
    public static DomainDO domainFrom(final EnvironmentClass environmentClass, final Zone zone) {
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
        }).first().get();
    }

    public static EnvClass fasitEnvironmentClassFromLocal(EnvironmentClass environmentClass) {
        return EnvClass.valueOf(environmentClass.name());
    }

    public static no.nav.aura.envconfig.client.DomainDO.Zone fasitZoneFromLocal(Zone zone) {
        return no.nav.aura.envconfig.client.DomainDO.Zone.valueOf(zone.name().toUpperCase());
    }

    private static ImmutableMap<EnvironmentClass, OrchestratorEnvClass> orchestratorEnvironmentClassFromLocalMap = ImmutableMap.of(
            EnvironmentClass.u, OrchestratorEnvClass.utv, EnvironmentClass.t, OrchestratorEnvClass.test, EnvironmentClass.q, OrchestratorEnvClass.preprod, EnvironmentClass.p, OrchestratorEnvClass.prod);

    public static OrchestratorEnvClass orchestratorEnvironmentClassFromLocal(EnvironmentClass environmentClass) {
        return orchestratorEnvironmentClassFromLocalMap.get(environmentClass);
    }

    public static no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Zone orchestratorZoneFromLocal(Zone zone) {
        return no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Zone.valueOf(zone.name());
    }

    public static PlatformTypeDO platformTypeDOFrom(NodeType nodeType, MiddleWareType middleWareType) {
        if (nodeType == NodeType.BPM_NODES) {
            return PlatformTypeDO.BPM;
        } else if (nodeType == NodeType.APPLICATION_SERVER) {
            switch (middleWareType) {
            case ap:
                break;
            case jb:
                return PlatformTypeDO.JBOSS;
            case wa:
                return PlatformTypeDO.WAS;
            }
        }
        throw new IllegalArgumentException("No platform type for node type " + nodeType + " and middle ware type " + middleWareType);
    }

}
