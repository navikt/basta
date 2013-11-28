package no.nav.aura.basta;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import no.nav.aura.basta.Converters;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.envClass;

import org.junit.Test;

import com.google.common.collect.Sets;

public class ConvertersTest {

    @Test
    public void orchestratorEnvironmentClassFromLocal() {
        Set<envClass> list = Sets.newHashSet();
        for (EnvironmentClass environmentClass : EnvironmentClass.values()) {
            list.add(Converters.orchestratorEnvironmentClassFromLocal(environmentClass));
        }
        assertThat(list.size(), equalTo(4));
    }

    @Test
    public void domainFrom() {
        assertThat(Converters.domainFrom(EnvironmentClass.u, Zone.fss), equalTo("devillo.no"));
        assertThat(Converters.domainFrom(EnvironmentClass.p, Zone.fss), equalTo("adeo.no"));
        assertThat(Converters.domainFrom(EnvironmentClass.p, Zone.sbs), equalTo("oera.no"));
    }
}
