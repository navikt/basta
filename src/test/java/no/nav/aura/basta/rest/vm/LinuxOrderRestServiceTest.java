package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.OSType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.URI;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;

public class LinuxOrderRestServiceTest extends AbstractOrchestratorTest {

    private LinuxOrderRestService ordersRestService;

    @Before
    public void setup() {
        ordersRestService = new LinuxOrderRestService(orderRepository, orchestratorClient);
        login("user", "user");
    }

    @Test
    public void orderPlainLinuxShouldgiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setServerCount(1);
        input.setMemory(1);
        input.setCpuCount(1);
        input.setHasIbmSoftware("false");

        mockOrchestratorProvision();

        Response response = ordersRestService.createNewPlainLinux(input.copy(), createUriInfo());
        Order order = getCreatedOrderFromResponseLocation(response);

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());

        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/linux_order.xml");

    }

    @Test
    public void orderFlatcarLinuxShouldgiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setServerCount(1);
        input.setMemory(1);
        input.setCpuCount(1);
        input.setHasIbmSoftware("false");
        input.setOsType(OSType.flatcar);
        mockOrchestratorProvision();

        Response response = ordersRestService.createNewFlatcarLinux(input.copy(), createUriInfo());
        Order order = getCreatedOrderFromResponseLocation(response);

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());

        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/flatcarlinux_order.xml");

    }

    @Test
    public void orderContainerLinuxShouldgiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setServerCount(1);
        input.setMemory(1);
        input.setCpuCount(1);
        input.setHasIbmSoftware("false");
        input.setOsType(OSType.coreos);
        mockOrchestratorProvision();

        Response response = ordersRestService.createNewContainerLinux(input.copy(), createUriInfo());
        Order order = getCreatedOrderFromResponseLocation(response);

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());

        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/containerlinux_order.xml");

    }
}
