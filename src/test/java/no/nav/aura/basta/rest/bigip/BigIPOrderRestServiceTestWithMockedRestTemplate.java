package no.nav.aura.basta.rest.bigip;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.restassured.http.ContentType;
import no.nav.aura.basta.backend.fasit.rest.model.ApplicationPayload;
import no.nav.aura.basta.backend.fasit.rest.model.EnvironmentPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.AbstractRestServiceTest;

public class BigIPOrderRestServiceTestWithMockedRestTemplate extends AbstractRestServiceTest {

    @BeforeEach
    public void setup() {
        // Reset the mock before each test to clear previous interactions
        reset(restTemplate);
    }

    private ResourcePayload createBigipResource() {
    	ResourcePayload r = new ResourcePayload(ResourceType.LoadBalancer, "bigip");
    	r.scope = new ScopePayload().environmentClass(EnvironmentClass.u);
    	r.addProperty("url", "https://bigip.example.com");
    	return r;
    }

    // -----------------------------------------------------------------------
    // Stub helpers
    //
    // All spy methods (getScopedFasitResource, findScopedFasitResource,
    // getApplicationByName, getEnvironmentByName) are stubbed directly on the
    // fasitRestClient spy — these never touch restTemplate.
    //
    // Only POST/PUT write calls go through restTemplate.exchange directly.
    // -----------------------------------------------------------------------

    private void mockScopedResourceFound() {
        doReturn(createBigipResource())
                .when(fasitRestClient).getScopedFasitResource(eq(ResourceType.LoadBalancer), eq("bigip"), any());
    }

    private void mockScopedResourceNotFound() {
        doThrow(new IllegalArgumentException("No matching resource found in fasit with alias bigip for scope"))
                .when(fasitRestClient).getScopedFasitResource(eq(ResourceType.LoadBalancer), eq("bigip"), any());
    }

    /** Stubs getApplicationByName and getEnvironmentByName on the spy — used by verifyFasitEntities. */
    private void mockApplicationAndEnvironmentFound() {
        ApplicationPayload ap = new ApplicationPayload();
        ap.name = "myapp";
        doReturn(ap).when(fasitRestClient).getApplicationByName(eq("myapp"));

        EnvironmentPayload ep = new EnvironmentPayload();
        ep.name = "myenv";
        ep.environmentClass = EnvironmentClass.u;
        doReturn(ep).when(fasitRestClient).getEnvironmentByName(eq("myenv"));
    }

    /**
     * Stubs findFasitResources for LoadBalancerConfig on the spy — used by
     * possibleToUpdateFasit. Returns empty to indicate no existing LBConfig.
     */
    private void mockNoExistingLBConfig() {
        doReturn(new ResourcesListPayload(List.of()))
        	.when(fasitRestClient).findFasitResources(eq(ResourceType.LoadBalancerConfig), eq(null), any());
//            .when(fasitRestClient).findScopedFasitResource(eq(ResourceType.LoadBalancerConfig), eq(null), any());
    }

    /**
     * Stubs findFasitResources for LoadBalancerConfig on the spy — used by
     * possibleToUpdateFasit and getPotentiallyExistingLBConfigId.
     * Returns a resource with id=42 to simulate an existing LBConfig.
     */
    private void mockExistingLBConfig() {
        ResourcePayload existingResource = new ResourcePayload(ResourceType.LoadBalancerConfig, "loadbalancer:myapp");
        existingResource.id = 42L;
        doReturn(new ResourcesListPayload(List.of(existingResource)))
                .when(fasitRestClient).findFasitResources(eq(ResourceType.LoadBalancerConfig), eq(null), any());
    }

    /** Stubs the Fasit PUT call used by FasitUpdateService.updateFasitResource (update path). */
    private void mockFasitResourceUpdate() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://fasit/api/v2/resources/1"));
        when(restTemplate.exchange(
                contains("/api/v2/resources/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("1",headers, HttpStatus.OK));
    }

    /** Stubs the Fasit POST call used by FasitUpdateService.createFasitResource (create path). */
    private void mockFasitResourceCreate() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://fasit/api/v2/resources/1"));
        when(restTemplate.exchange(
                contains("/api/v2/resources"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("1", headers, HttpStatus.CREATED));
    }

    private Map<String, String> createBasicPostRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("application", "myapp");
        request.put("environmentClass", "u");
        request.put("environmentName", "myenv");
        request.put("virtualserver", "vs1");
        request.put("zone", "fss");
        return request;
    }

    // -----------------------------------------------------------------------
    // GET /rest/v1/bigip/virtualservers
    // -----------------------------------------------------------------------

    @Test
    public void getVirtualServers_returnsList_whenBigIPResourceFound() {
        mockScopedResourceFound();

        // BigIPClientSetup and BigIPClient are mocked in StandaloneRunnerTestConfig:
        // bigIPClient.getVirtualServers("AutoProv") returns [{"name":"vs_name_1"}, {"name":"vs_name_2"}]

        given()
            .auth().preemptive().basic("user", "user")
            .queryParam("environmentClass", "u")
            .queryParam("environmentName", "myenv")
            .queryParam("zone", "fss")
            .queryParam("application", "myapp")
        .when()
            .get("/rest/v1/bigip/virtualservers")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", containsInAnyOrder("vs_name_1", "vs_name_2"));
    }

    @Test
    public void getVirtualServers_returns404_whenBigIPResourceNotFound() {
        mockScopedResourceNotFound();

        given()
            .auth().preemptive().basic("user", "user")
            .queryParam("environmentClass", "u")
            .queryParam("environmentName", "myenv")
            .queryParam("zone", "fss")
            .queryParam("application", "myapp")
        .when()
            .get("/rest/v1/bigip/virtualservers")
        .then()
            .statusCode(404);
    }

    // -----------------------------------------------------------------------
    // POST /rest/v1/bigip  (createBigIpConfig)
    // -----------------------------------------------------------------------

    @Test
    public void createBigIpConfig_succeeds_withContextRoots() {
        mockApplicationAndEnvironmentFound();
        mockScopedResourceFound();
        mockNoExistingLBConfig();
        mockFasitResourceCreate();

        Map<String, String> request = createBasicPostRequest();
        request.put("contextroots", "myapp,myapp/api");
        request.put("useHostnameMatching", "false");

        int orderId = given()
            .auth().preemptive().basic("user", "user")
            .body(request)
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/v1/bigip")
        .then()
            .statusCode(200)
            .extract().as(Integer.class);

        Order order = orderRepository.findById((long) orderId).orElse(null);
        Assertions.assertNotNull(order);
        Assertions.assertEquals(OrderType.BIGIP, order.getOrderType());
        Assertions.assertEquals(OrderOperation.CREATE, order.getOrderOperation());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
    }

    @Test
    public void createBigIpConfig_succeeds_withHostnameMatching() {
        mockApplicationAndEnvironmentFound();
        mockScopedResourceFound();
        mockNoExistingLBConfig();
        mockFasitResourceCreate();

        Map<String, String> request = createBasicPostRequest();
        request.put("hostname", "myapp.adeo.no");
        request.put("useHostnameMatching", "true");

        int orderId = given()
            .auth().preemptive().basic("user", "user")
            .body(request)
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/v1/bigip")
        .then()
            .statusCode(200)
            .extract().as(Integer.class);

        Order order = orderRepository.findById((long) orderId).orElse(null);
        Assertions.assertNotNull(order);
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
    }

    @Test
    public void createBigIpConfig_succeeds_updatesExistingLBConfig() {
        mockApplicationAndEnvironmentFound();
        mockScopedResourceFound();
        mockExistingLBConfig();
        mockFasitResourceUpdate();

        Map<String, String> request = createBasicPostRequest();
        request.put("contextroots", "myapp");
        request.put("useHostnameMatching", "false");

        int orderId = given()
            .auth().preemptive().basic("user", "user")
            .body(request)
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/v1/bigip")
        .then()
        	.log().all()
            .statusCode(200)
            .extract().as(Integer.class);

        Order order = orderRepository.findById((long) orderId).orElse(null);
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());

        // Verify the PUT call to Fasit was made for the existing resource with id=42
        verify(restTemplate).exchange(
                contains("/api/v2/resources/42"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    public void createBigIpConfig_returns403_whenUserLacksAccess() {
        // environmentClass "p" (production) — user role has no access
        Map<String, String> request = createBasicPostRequest();
        request.put("environmentClass", "p");
        request.put("contextroots", "myapp");
        request.put("useHostnameMatching", "false");

        given()
            .auth().preemptive().basic("user", "user")
            .body(request)
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/v1/bigip")
        .then()
            .statusCode(403);
    }

    @Test
    public void createBigIpConfig_returns400_whenNoContextRootsAndNotHostnameMatching() {
        Map<String, String> request = createBasicPostRequest();
        request.put("useHostnameMatching", "false");
        // no contextroots set

        given()
            .auth().preemptive().basic("user", "user")
            .body(request)
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/v1/bigip")
        .then()
            .statusCode(400);
    }

    @Test
    public void createBigIpConfig_returns400_whenHostnameMatchingButNoHostname() {
        Map<String, String> request = createBasicPostRequest();
        request.put("useHostnameMatching", "true");
        // no hostname set

        given()
            .auth().preemptive().basic("user", "user")
            .body(request)
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/v1/bigip")
        .then()
            .statusCode(400);
    }

    @Test
    public void createBigIpConfig_returns400_whenHostnameIsCommonVS() {
        Map<String, String> request = createBasicPostRequest();
        request.put("useHostnameMatching", "true");
        request.put("hostname", "app-t4.adeo.no");

        given()
            .auth().preemptive().basic("user", "user")
            .body(request)
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/v1/bigip")
        .then()
            .statusCode(400);
    }
}