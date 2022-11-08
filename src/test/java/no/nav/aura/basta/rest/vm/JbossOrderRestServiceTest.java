package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;

import java.net.URI;

import javax.ws.rs.core.Response;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JbossOrderRestServiceTest extends AbstractOrchestratorTest {

    private JbossOrderRestService ordersRestService;

    @BeforeEach
    public void setup() {
        ordersRestService = new JbossOrderRestService(orderRepository, orchestratorClient);
        login("user", "user");
    }

    @Test
    public void orderNewShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.sbs);
        input.setServerCount(2);
        input.setMemory(1);
        input.setCpuCount(4);
        input.setClassification(Classification.standard);
        input.setApplicationMappingName("myapp");
        input.setEnvironmentName("u1");

        mockOrchestratorProvision();

        Response response = ordersRestService.createJbossNode(input.copy(), createUriInfo());
        Order order = getCreatedOrderFromResponseLocation(response);

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/jboss_order.xml");

    }

}
