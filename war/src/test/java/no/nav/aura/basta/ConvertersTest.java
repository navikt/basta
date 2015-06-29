package no.nav.aura.basta;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import no.nav.aura.basta.backend.vmware.orchestrator.MiddleWareType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.PlatformTypeDO;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class ConvertersTest {


    @Test
    public void domainFrom() {
        assertThat(Converters.domainFqdnFrom(EnvironmentClass.u, Zone.fss), equalTo("devillo.no"));
        assertThat(Converters.domainFqdnFrom(EnvironmentClass.u, Zone.sbs), equalTo("devillo.no"));
        assertThat(Converters.domainFqdnFrom(EnvironmentClass.p, Zone.fss), equalTo("adeo.no"));
        assertThat(Converters.domainFqdnFrom(EnvironmentClass.p, Zone.sbs), equalTo("oera.no"));
    }

    @Test
    @Ignore
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
    public void platformTypeDOFromNodeTypeAndMiddleWareType() {
        assertThat(Converters.platformTypeDOFrom(NodeType.BPM_NODES, MiddleWareType.bpm), equalTo(PlatformTypeDO.BPM));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS_NODES, MiddleWareType.wa), equalTo(PlatformTypeDO.WAS));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS_NODES, MiddleWareType.was), equalTo(PlatformTypeDO.WAS));
        assertThat(Converters.platformTypeDOFrom(NodeType.JBOSS, MiddleWareType.jboss), equalTo(PlatformTypeDO.JBOSS));
        assertThat(Converters.platformTypeDOFrom(NodeType.JBOSS, MiddleWareType.jb), equalTo(PlatformTypeDO.JBOSS));


    }

    private <T, F> void checkEnumConversion(F[] values, Function<F, T> f) {
        Set<T> set = Sets.newHashSet();
        for (F environmentClass : values) {
            set.add(f.apply(environmentClass));
        }
        assertThat(set.size(), equalTo(values.length));
    }
}
