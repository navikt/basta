package no.nav.aura.basta;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.envClass;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class ConvertersTest {

    @Test
    public void orchestratorEnvironmentClassFromLocal() {
        checkEnumConversion(EnvironmentClass.values(), new SerializableFunction<EnvironmentClass, envClass>() {
            public envClass process(EnvironmentClass input) {
                return Converters.orchestratorEnvironmentClassFromLocal(input);
            }
        });
    }

    @Test
    public void domainFrom() {
        assertThat(Converters.domainFrom(EnvironmentClass.u, Zone.fss), equalTo("devillo.no"));
        assertThat(Converters.domainFrom(EnvironmentClass.p, Zone.fss), equalTo("adeo.no"));
        assertThat(Converters.domainFrom(EnvironmentClass.p, Zone.sbs), equalTo("oera.no"));
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

    private <T, F> void checkEnumConversion(F[] values, Function<F, T> f) {
        Set<T> set = Sets.newHashSet();
        for (F environmentClass : values) {
            set.add(f.apply(environmentClass));
        }
        assertThat(set.size(), equalTo(values.length));
    }
}
