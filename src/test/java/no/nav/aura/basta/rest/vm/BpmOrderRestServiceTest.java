package no.nav.aura.basta.rest.vm;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.util.MapBuilder;


public class BpmOrderRestServiceTest extends AbstractOrchestratorTest {

    @Test
    public void testRestClientIsMocked() {
        // Verify that restClient is a mock
        System.out.println("restClient type: " + fasitRestClient.getClass().getName());
        System.out.println("restClient is mock: " + Mockito.mockingDetails(fasitRestClient).isMock());
        
        // Assert it's actually a mock
        Assertions.assertTrue(
            Mockito.mockingDetails(fasitRestClient).isMock(),
            "restClient should be a Mockito mock"
        );
    }
    
    @Test
    public void orderNewBpm86NodeShouldGiveNiceXml() {
//        VMOrderInput input = new VMOrderInput();
//        input.setEnvironmentClass(EnvironmentClass.u);
//        input.setZone(Zone.fss);
//        input.setServerCount(1);
//        input.setMemory(16);
//        input.setCpuCount(4);
//        input.setEnvironmentName("u1");
//        input.setNodeType(NodeType.BPM86_NODES);

        Map<String, String> input = new HashMap<String, String>();
        input.put("environmentClass", "u");
        input.put("zone", "fss");
        input.put("serverCount", "1");
        input.put("memory", "16");
        input.put("cpuCount", "4");
        input.put("environmentName", "u1");
        input.put("nodeType", "BPM86_NODES");
        
        mockStandard();
        doReturn("password").when(fasitRestClient).getFasitSecret(anyString());
        doReturn(getDmgr()).when(fasitRestClient).findScopedFasitResource(eq(ResourceType.DeploymentManager), eq("bpm86Dmgr"), any(ScopePayload.class));
        doReturn(0).when(fasitRestClient).getCount(anyString());

        int ord =  given()
			        	.auth().preemptive().basic("user", "user")
			        	.body(input)
			        	.contentType(ContentType.JSON)
			        	.when()
			        	.post("/rest/vm/orders/bpm/node")
			        	.then()
			        	.statusCode(201)
			        	.extract()
			        	.path("id");

        long orderId = Long.valueOf(ord);
        Order order = orderRepository.findById(orderId).orElse(null);

        MatcherAssert.assertThat(order.getExternalId(), is(notNullValue()));
//
        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/bpm86_node_order.xml");
    }

    @Test
    public void orderNewBpm86DgmrShouldGiveNiceXml() {
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("zone", "fss");
        input.put("memory", "4");
        input.put("cpuCount", "2");
        input.put("environmentName", "u1");
        input.put("nodeType", "BPM86_DEPLOYMENT_MANAGER");

        
        mockStandard();
        doReturn(Optional.empty()).when(fasitRestClient).findScopedFasitResource(eq(ResourceType.DeploymentManager), eq("bpm86Dmgr"), any(ScopePayload.class));
        doReturn("password").when(fasitRestClient).getFasitSecret(anyString());
        

        int ord = given()
                .auth().preemptive().basic("user", "user")
                .body(input)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/vm/orders/bpm/dmgr")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        long orderId = Long.valueOf(ord);
        Order order = orderRepository.findById(orderId).orElse(null);
        MatcherAssert.assertThat(order.getExternalId(), is(notNullValue()));

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/bpm86_dmgr_order.xml");
    }

    private void mockStandard() {
        doReturn(getUser()).when(fasitRestClient).findScopedFasitResource(eq(ResourceType.Credential), eq("wsadminUser"), any(ScopePayload.class));
        doReturn(getUser()).when(fasitRestClient).findScopedFasitResource(eq(ResourceType.Credential), eq("wasLdapUser"), any(ScopePayload.class));
        doReturn(getUser()).when(fasitRestClient).findScopedFasitResource(eq(ResourceType.Credential), eq("srvBpm"), any(ScopePayload.class));
        doReturn(createDatabase()).when(fasitRestClient).findScopedFasitResource(eq(ResourceType.DataSource), anyString(), any(ScopePayload.class));
        doReturn("password").when(fasitRestClient).getFasitSecret(anyString());
    }

    private Optional<ResourcePayload> createDatabase() {
        return Optional.of(createResourceWithSecret(
        		ResourceType.DataSource, 
        		"mockedDataSource", 
        		MapBuilder.stringMapBuilder()
        			.put("username", "dbuser")
        			.put("url","mocked_dburl").build()));
    }

    private Optional<ResourcePayload> getUser() {
        return Optional.of(createResourceWithSecret(ResourceType.Credential, "mockedUser", MapBuilder.stringMapBuilder().put("username", "srvUser").build()));
    }

    private Optional<ResourcePayload> getDmgr() {
        return Optional.of(createResource(ResourceType.DeploymentManager, "bpm86Dmgr", MapBuilder.stringMapBuilder().put("hostname", "dmgr.domain.no").build()));
    }
}
