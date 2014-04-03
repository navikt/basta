package no.nav.aura.basta;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Set;

import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.OrchestratorEnvClass;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.PlatformTypeDO;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class ConvertersTest {

    @Test
    public void orchestratorEnvironmentClassFromLocal() {
        checkEnumConversion(EnvironmentClass.values(), new SerializableFunction<EnvironmentClass, OrchestratorEnvClass>() {
            public OrchestratorEnvClass process(EnvironmentClass input) {
                return Converters.orchestratorEnvironmentClassFromLocal(input,false);
            }
        });
    }

    @Test
    public void orchestratorEnvironmentClassFromLocalPredprod() throws Exception {
        
        assertThat(Converters.orchestratorEnvironmentClassFromLocal(EnvironmentClass.q, true), is(equalTo(OrchestratorEnvClass.preprod)));
        assertThat(Converters.orchestratorEnvironmentClassFromLocal(EnvironmentClass.q, false), is(equalTo(OrchestratorEnvClass.qa)));


    }
    
    
    @Test
    public void domainFrom() {
        assertThat(Converters.domainFqdnFrom(EnvironmentClass.u, Zone.fss), equalTo("devillo.no"));
        assertThat(Converters.domainFqdnFrom(EnvironmentClass.p, Zone.fss), equalTo("adeo.no"));
        assertThat(Converters.domainFqdnFrom(EnvironmentClass.p, Zone.sbs), equalTo("oera.no"));
    }

    @Test
    public void fasitZoneFromLocal() {
        checkEnumConversion(Zone.values(), new SerializableFunction<Zone, no.nav.aura.envconfig.client.DomainDO.Zone>() {
            public no.nav.aura.envconfig.client.DomainDO.Zone process(Zone input) {
                return Converters.fasitZoneFromLocal(input);
            }
        });
    }

    @Test
    public void fasitEnvironmentClassFromLocal() {
        checkEnumConversion(EnvironmentClass.values(), new SerializableFunction<EnvironmentClass, EnvClass>() {
            public EnvClass process(EnvironmentClass input) {
                return Converters.fasitEnvironmentClassFromLocal(input);
            }
        });
    }

    @Test
    public void orchestratorZoneFromLocal() {
        checkEnumConversion(Zone.values(), new SerializableFunction<Zone, no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Zone>() {
            public no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Zone process(Zone input) {
                return Converters.orchestratorZoneFromLocal(input);
            }
        });
    }

    @Test
    public void platformTypeDOFromNodeTypeAndMiddleWareType() {
        for (NodeType nodeType : NodeType.values()) {
            for (MiddleWareType middleWareType : MiddleWareType.values()) {
                if (nodeType == NodeType.BPM_NODES) {
                    assertThat(Converters.platformTypeDOFrom(nodeType, middleWareType), equalTo(PlatformTypeDO.BPM));
                } else if ((nodeType == NodeType.APPLICATION_SERVER || nodeType == NodeType.WAS_NODES) && middleWareType != MiddleWareType.ap) {
                    assertThat(Converters.platformTypeDOFrom(nodeType, middleWareType).name().substring(0, 2).toLowerCase(), equalTo(middleWareType.name()));
                } else {
                    try {
                        Converters.platformTypeDOFrom(nodeType, middleWareType);
                        fail();
                    } catch (IllegalArgumentException e) {
                        // Expected
                    }
                }
            }
        }
    }

    private <T, F> void checkEnumConversion(F[] values, Function<F, T> f) {
        Set<T> set = Sets.newHashSet();
        for (F environmentClass : values) {
            set.add(f.apply(environmentClass));
        }
        assertThat(set.size(), equalTo(values.length));
    }
}
