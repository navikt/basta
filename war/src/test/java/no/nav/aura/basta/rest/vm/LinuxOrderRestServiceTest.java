package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;

import java.net.URI;

import javax.ws.rs.core.Response;

import no.nav.aura.basta.backend.vmware.orchestrator.v2.ProvisionRequest2;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;

import org.junit.Before;
import org.junit.Test;

public class LinuxOrderRestServiceTest extends AbstractOrchestratorTest {

    private LinuxOrderRestService ordersRestService;

    @Before
    public void setup() {
        ordersRestService = new LinuxOrderRestService(orderRepository, orchestratorService);
        login("user", "user");
    }

    @Test
    public void orderPlainLinuxhsouldgiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setServerCount(1);
        input.setMemory(1);
        input.setCpuCount(1);

        mockOrchestratorProvision();

        Response response = ordersRestService.createNewPlainLinux(input.copy(), createUriInfo());
        Order order = getCreatedOrderFromResponseLocation(response);

        ProvisionRequest2 request = getAndValidateOrchestratorRequest(order.getId());

        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/linux_order.xml");

    }

}
