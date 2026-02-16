package no.nav.aura.basta.rest.vm;

import static io.restassured.RestAssured.given;
import static no.nav.aura.basta.util.MapBuilder.stringMapBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;

public class WebsphereOrderRestServiceTest extends AbstractOrchestratorTest {


    @Test
    public void orderNewWebsphere9NodeShouldGiveNiceXml() {
//        VMOrderInput input = new VMOrderInput();
//        input.setEnvironmentClass(EnvironmentClass.u);
//        input.setZone(Zone.sbs);
//        input.setServerCount(1);
//        input.setMemory(2);
//        input.setCpuCount(2);
//        input.setClassification(Classification.standard);
//        input.setApplicationMappingName("myapp");
//        input.setEnvironmentName("u1");
//        input.setNodeType(NodeType.WAS9_NODES);
        Map<String, String> input = new HashMap();
       input.put("environmentClass", "u");
       input.put("zone", "sbs");
       input.put("serverCount", "1");
       input.put("memory", "2");
       input.put("cpuCount", "2");
       input.put("classification", "standard");
       input.put("applicationMappingName", "myapp");
       input.put("environmentName", "u1");
       input.put("nodeType", "WAS9_NODES");
       
        
        when(restClient.findScopedFasitResource(eq(ResourceType.DeploymentManager), eq("was9Dmgr"), any(ScopePayload.class))).thenReturn(getDmgr());
        when(restClient.findScopedFasitResource(eq(ResourceType.Credential), eq("wsadminUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(restClient.getFasitSecret(anyString())).thenReturn("password");

        int ord = given()
                .auth().preemptive().basic("user", "user")
                .body(input)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/vm/orders/was/node")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        
        Order order = getCreatedOrderFromResponseLocation(Long.valueOf(ord));
        Assertions.assertNotNull(order.getExternalId());

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was9_node_order.xml");
    }

    @Test
    public void orderNewWebsphere9DgmrShouldGiveNiceXml() {
//        VMOrderInput input = new VMOrderInput();
//        input.setEnvironmentClass(EnvironmentClass.u);
//        input.setZone(Zone.fss);
//        input.setMemory(4);
//        input.setCpuCount(2);
//        input.setEnvironmentName("u1");
//        input.setNodeType(NodeType.WAS9_DEPLOYMENT_MANAGER);

        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("zone", "fss");
        input.put("memory", "4");
        input.put("cpuCount", "2");
        input.put("environmentName", "u1");
        input.put("nodeType", "WAS9_DEPLOYMENT_MANAGER");
        
        ScopePayload scope = new ScopePayload();
        scope.environmentclass = EnvironmentClass.u;
        scope.zone = Zone.fss;
        scope.environment = "u1";
        
        when(restClient.findScopedFasitResource(eq(ResourceType.DeploymentManager), eq("was9Dmgr"), any(ScopePayload.class))).thenReturn(Optional.empty());
        when(restClient.findScopedFasitResource(eq(ResourceType.Credential), eq("wsadminUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(restClient.findScopedFasitResource(eq(ResourceType.Credential), eq("wasLdapUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(restClient.getFasitSecret(anyString())).thenReturn("password");

//        ResponseEntity<?> response = service.createWasDmgr(input.copy());
        int ord = given()
                .auth().preemptive().basic("user", "user")
                .body(input)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/vm/orders/was/dmgr")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Order order = getCreatedOrderFromResponseLocation(Long.valueOf(ord));
        Assertions.assertNotNull(order.getExternalId());

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was9_dmgr_order.xml");
    }

    private Optional<ResourcePayload> getUser() {
        return Optional.of(createResourceWithSecret(
                ResourceType.Credential,
                "user",
                stringMapBuilder().put("username", "srvUser").build()));
    }

    private Optional<ResourcePayload> getDmgr() {
        return Optional.of(createResource(
                ResourceType.DeploymentManager,
                "was9Dmgr",
                stringMapBuilder().put("hostname", "dmgr.domain.no").build()));
    }
}