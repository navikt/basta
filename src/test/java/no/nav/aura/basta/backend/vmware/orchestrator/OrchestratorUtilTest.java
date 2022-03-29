package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

public class OrchestratorUtilTest {

    private ProvisionRequest request;
    private Vm vm;

    @BeforeEach
    public void setup() {
        VMOrderInput input = new VMOrderInput();
        request = new ProvisionRequest(OrchestratorEnvironmentClass.test, input, URI.create("http://url"), URI.create("http://url"));
        vm = new Vm(Zone.fss, OSType.rhel70, MiddlewareType.bpm, Classification.standard, 1, 3);
        request.addVm(vm);
    }

    @Test
    public void shouldStripFqdnFromHostnames() {
        List<String> hostnames = OrchestratorUtil.stripFqdnFromHostnames("host1.devillo.no", "host2.adeo.no", "host3");
        MatcherAssert.assertThat(hostnames, Matchers.containsInAnyOrder("host1", "host2", "host3"));
    }

}