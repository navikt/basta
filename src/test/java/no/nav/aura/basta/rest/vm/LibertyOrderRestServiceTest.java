package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.payload.ScopePayload;
import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.OSType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.util.MapBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class LibertyOrderRestServiceTest extends AbstractOrchestratorTest {

    private LibertyOrderRestService ordersRestService;

    @BeforeEach
    public void setup() {
        ordersRestService = new LibertyOrderRestService(orderRepository, orchestratorClient, fasit);
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
        input.setOsType(OSType.rhel70);
        input.setClassification(Classification.standard);
        input.setApplicationMappingName("myapp");
        input.setEnvironmentName("u1");

        mockOrchestratorProvision();
        when(fasit.findScopedFasitResource(eq(ResourceType.credential), eq("wsadminUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(fasit.findScopedFasitResource(eq(ResourceType.credential), eq("wasLdapUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(fasit.getFasitSecret(anyString())).thenReturn("password");
        Response response = ordersRestService.createLibertyNode(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertThat(order.getExternalId(), is(notNullValue()));

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/liberty_order.xml");

    }

    private Optional<ResourcePayload> getUser() {
        return Optional.of(createResourceWithSecret(ResourceType.credential, "user", MapBuilder.stringMapBuilder().put("username", "srvUser").build()));
    }

}
