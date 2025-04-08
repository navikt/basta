package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ScopePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static no.nav.aura.basta.util.MapBuilder.stringMapBuilder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class WebsphereOrderRestServiceTest extends AbstractOrchestratorTest {

    private WebsphereOrderRestService service;

    @BeforeEach
    public void setup() {
        service = new WebsphereOrderRestService(orderRepository, orchestratorClient, fasit);
        login();
    }

    @Test
    public void orderNewWebsphere9NodeShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.sbs);
        input.setServerCount(1);
        input.setMemory(2);
        input.setCpuCount(2);
        input.setClassification(Classification.standard);
        input.setApplicationMappingName("myapp");
        input.setEnvironmentName("u1");
        input.setNodeType(NodeType.WAS9_NODES);

        mockOrchestratorProvision();
        when(fasit.findScopedFasitResource(eq(ResourceType.deploymentmanager), eq("was9Dmgr"), any(ScopePayload.class))).thenReturn(getDmgr());
        when(fasit.findScopedFasitResource(eq(ResourceType.credential), eq("wsadminUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(fasit.getFasitSecret(anyString())).thenReturn("password");

        Response response = service.createWasNode(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        Assertions.assertNotNull(order.getExternalId());

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was9_node_order.xml");
    }

    @Test
    public void orderNewWebsphere9DgmrShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setMemory(4);
        input.setCpuCount(2);
        input.setEnvironmentName("u1");
        input.setNodeType(NodeType.WAS9_DEPLOYMENT_MANAGER);

        mockOrchestratorProvision();
        when(fasit.findScopedFasitResource(eq(ResourceType.deploymentmanager), eq("was9Dmgr"), any(ScopePayload.class))).thenReturn(Optional.empty());
        when(fasit.findScopedFasitResource(eq(ResourceType.credential), eq("wsadminUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(fasit.findScopedFasitResource(eq(ResourceType.credential), eq("wasLdapUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(fasit.getFasitSecret(anyString())).thenReturn("password");

        Response response = service.createWasDmgr(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        Assertions.assertNotNull(order.getExternalId());

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was9_dmgr_order.xml");
    }

    private Optional<ResourcePayload> getUser() {
        return Optional.of(createResourceWithSecret(
                ResourceType.credential,
                "user",
                stringMapBuilder().put("username", "srvUser").build()));
    }

    private Optional<ResourcePayload> getDmgr() {
        return Optional.of(createResource(
                ResourceType.deploymentmanager,
                "was9Dmgr",
                stringMapBuilder().put("hostname", "dmgr.domain.no").build()));
    }
}