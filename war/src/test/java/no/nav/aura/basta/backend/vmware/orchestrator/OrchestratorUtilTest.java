package no.nav.aura.basta.backend.vmware.orchestrator;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.net.URI;

import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;

import org.junit.Before;
import org.junit.Test;

public class OrchestratorUtilTest {

    private ProvisionRequest request;
    private Vm vm;

    @Before
    public void setup() {
        VMOrderInput input = new VMOrderInput();
        request = new ProvisionRequest(OrchestratorEnvironmentClass.test, input, URI.create("http://url"), URI.create("http://url"));
        vm = new Vm(Zone.fss, OSType.rhel60, MiddleWareType.bpm, Classification.standard, 1, 3);
        request.addVm(vm);
    }

    @Test
    public void censorePassWord() {
        vm.addPuppetFact(FactType.cloud_app_bpm_adminpwd, "secret");
        String xml = OrchestratorUtil.censore(request);
        assertThat(xml, not(containsString("secret")));
        assertThat(xml, containsString("<value>********"));
    }

    @Test
    public void censorePassWordWithRegexpInPassword() {
        vm.addPuppetFact(FactType.cloud_app_bpm_adminpwd, "sec(ret");
        String xml = OrchestratorUtil.censore(request);
        assertThat(xml, not(containsString("sec(ret")));
        assertThat(xml, containsString("<value>********"));
    }

}
