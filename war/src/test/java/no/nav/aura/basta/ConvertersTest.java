package no.nav.aura.basta;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Set;

import no.nav.aura.basta.persistence.ApplicationServerType;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Role;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.envClass;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.envconfig.client.PlatformTypeDO;
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

    @Test
    public void orchestratorZoneFromLocal() {
        checkEnumConversion(Zone.values(), new SerializableFunction<Zone, no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Zone>() {
            public no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Zone process(Zone input) {
                return Converters.orchestratorZoneFromLocal(input);
            }
        });
    }

    @Test
    public void orchestratorMiddleWareTypeFromLocal() {
        checkEnumConversion(ApplicationServerType.values(), new SerializableFunction<ApplicationServerType, MiddleWareType>() {
            public MiddleWareType process(ApplicationServerType input) {
                return Converters.orchestratorMiddleWareTypeFromLocal(input);
            }
        });
    }

    @Test
    public void roleFromApplicationServerType() {
        checkEnumConversion(ApplicationServerType.values(), new SerializableFunction<ApplicationServerType, Role>() {
            public Role process(ApplicationServerType input) {
                return Converters.roleFrom(input);
            }
        });
        assertThat(Converters.roleFrom(ApplicationServerType.jb), nullValue());
    }

    @Test
    public void platformTypeDOFromApplicationServerType() {
        checkEnumConversion(ApplicationServerType.values(), new SerializableFunction<ApplicationServerType, PlatformTypeDO>() {
            public PlatformTypeDO process(ApplicationServerType input) {
                return Converters.platformTypeDOFrom(input);
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
