package no.nav.aura.basta.rest.mq;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.restassured.http.ContentType;
import no.nav.aura.basta.backend.fasit.rest.model.FasitSearchResults;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SearchResultPayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.backend.mq.MqAdminUser;
import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.AbstractRestServiceTest;

public class MqQueueRestServiceTest extends AbstractRestServiceTest {
	private static final Logger log = LoggerFactory.getLogger(MqQueueRestServiceTest.class);

	private MqService mq;
    private Map<EnvironmentClass, MqAdminUser> envCredMap = new HashMap<>();

    private static final String MQ_QUEUE_URL = "/rest/v1/mq/order/queue";
    private static final String DEFAULT_QUEUE_NAME = "MYENV_MYAPP_SOMEQUEUE";
    private static final String NON_EMPTY_QUEUE_NAME = "MYENV_MYAPP_NONEMPTYQUEUE";

    @BeforeEach
    public void setup() {
    	reset(restTemplate);
        mq = applicationContext.getBean(MqService.class);

        envCredMap.put(EnvironmentClass.u, new MqAdminUser("mqadmin", "secret", "SRVAURA.ADMIN"));

        when(restTemplate.exchange(
				anyString(),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(String.class)
				)).thenReturn(new ResponseEntity<>("100", HttpStatus.CREATED));

        when(restTemplate.exchange(
				anyString(),
				eq(HttpMethod.PUT),
				any(HttpEntity.class),
				eq(String.class)
				)).thenReturn(new ResponseEntity<>("200", HttpStatus.CREATED));

        when(mq.getQueue(any(MqQueueManager.class), eq(DEFAULT_QUEUE_NAME))).thenReturn(Optional.of(new MqQueue(
                "someQueue", 1, 1, "mockup queue for test")));
        when(mq.getQueue(any(MqQueueManager.class), eq(NON_EMPTY_QUEUE_NAME))).thenReturn(Optional.of(new MqQueue(
                "nonEmptyQueue", 1, 1, "mockup queue for test")));

        when(mq.getCredentialMap()).thenReturn(envCredMap);

        when(mq.isQueueEmpty(any(MqQueueManager.class), eq("SOMEQUEUE"))).thenReturn(true);
        when(mq.isQueueEmpty(any(MqQueueManager.class), eq("NONEMPTYQUEUE"))).thenReturn(false);
    }

    // Helper methods to reduce duplication
    private Map<String, String> createBasicQueueInput(String queueName) {
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("queueManager", "mq://host:123/mdlclient03");
        input.put("mqQueueName", queueName);
        input.put("mqOrderType", "Queue");
        return input;
    }

    private SearchResultPayload createSearchResult(String queueName, Long id) {
        SearchResultPayload searchResult = new SearchResultPayload();
        searchResult.name = queueName;
        searchResult.type = "resource";
        searchResult.id = id;
        searchResult.link = URI.create("http://fasit/api/v2/resources/" + id);
        return searchResult;
    }

    private ResourcePayload createResourcePayload(String queueName, Long id) {
        ResourcePayload resourcePayload = new ResourcePayload(ResourceType.Queue, queueName);
        resourcePayload.id = id;
        resourcePayload.scope = new ScopePayload().environmentClass(EnvironmentClass.u);
        return resourcePayload;
    }

    private void mockFasitSearch(SearchResultPayload searchResult) {
        when(restTemplate.exchange(
                contains("/api/v1/search"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FasitSearchResults.class)
        )).thenReturn(new ResponseEntity<>( new FasitSearchResults(List.of(searchResult)), HttpStatus.OK));
    }

    private void mockEmptyFasitSearch() {
        when(restTemplate.exchange(
                contains("/api/v1/search"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FasitSearchResults.class)
        )).thenReturn(new ResponseEntity<>( new FasitSearchResults(List.of()), HttpStatus.OK));
    }

    private void mockResourceGet(Long resourceId, ResourcePayload resourcePayload) {
        when(restTemplate.exchange(
                contains("/api/v2/resources/" + resourceId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenReturn(new ResponseEntity<>(resourcePayload, HttpStatus.OK));
    }

    private void verifyFasitSearchCalled() {
        verify(restTemplate).exchange(
                contains("/api/v1/search"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FasitSearchResults.class));        ;
    }

    private void verifyLifecyclePutCalled() {
        verify(restTemplate).exchange(
                contains("/api/v1/lifecycle/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class));
    }

    private void verifyNoFasitResourceDelete() {
        verify(restTemplate, never()).exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class));
    }

    private void assertOrderResult(Long orderId, OrderType type, OrderStatus status, OrderOperation operation) {
        Order order = orderRepository.findById(orderId).orElse(null);
        assertEquals(type, order.getOrderType());
        assertEquals(status, order.getStatus());
        assertEquals(operation, order.getOrderOperation());
    }

    private int executeQueueOperation(String operation, Map<String, String> input) {
        return given()
                .auth().preemptive().basic("user", "user")
                .body(input)
                .contentType(ContentType.JSON)
                .when()
                .put(MQ_QUEUE_URL + "/" + operation)
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    public void testCreateQueue() {
    	// mock fasitRestClient.findFasitResources
        when(restTemplate.exchange(
        		anyString(),
        		eq(HttpMethod.GET),
        		any(HttpEntity.class),
    			eq(ResourcesListPayload.class)
				)).thenReturn(new ResponseEntity<>(new ResourcesListPayload(List.of()), HttpStatus.OK));

        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("application", "myApp");
        input.put("environmentName", "myenv");
        input.put("queueManager", "mq://host:123/mdlclient03");
        input.put("mqQueueName", "MYENV_MYAPP_SOMEQUEUE");
        input.put("fasitAlias", "myapp_somequeue");
        input.put("maxMessageSize", "1");
        input.put("queueDepth", "500");
        input.put("createBackoutQueue", "true");
        input.put("backoutThreshold", "4");
        input.put("mqOrderType", "Queue");
        
        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.post(MQ_QUEUE_URL)
						.then()
						.statusCode(201)
						.extract().path("id");
        
        verify(mq).createQueue(any(MqQueueManager.class), any(MqQueue.class));
        verify(mq).createAlias(any(MqQueueManager.class), any(MqQueue.class));
        verify(mq).createBackoutQueue(any(MqQueueManager.class), any(MqQueue.class));
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.CREATE, order.getOrderOperation());
    }

    @Test
    public void testCreateQueueNoAccess() {
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "p");
        input.put("mqOrderType", "Queue");

        given()
			.auth().preemptive().basic("user", "user")
			.body(input)
			.contentType(ContentType.JSON)
			.when()
			.post(MQ_QUEUE_URL)
			.then()
			.statusCode(403);
    }

    @Test
    public void testStop() {
        // Set up Fasit resources
        SearchResultPayload searchResult = createSearchResult(DEFAULT_QUEUE_NAME, 42L);
        ResourcePayload resourcePayload = createResourcePayload(DEFAULT_QUEUE_NAME, 42L);
        
        mockFasitSearch(searchResult);
        mockResourceGet(42L, resourcePayload);

        Map<String, String> input = createBasicQueueInput(DEFAULT_QUEUE_NAME);
        int orderId = executeQueueOperation("stop", input);

        verify(mq).disableQueue(any(MqQueueManager.class), any(MqQueue.class));
        verifyLifecyclePutCalled();
        assertOrderResult((long) orderId, OrderType.MQ, OrderStatus.SUCCESS, OrderOperation.STOP);
    }

    @Test
    public void testStart() {
        // Set up Fasit resources
        SearchResultPayload searchResult = createSearchResult(DEFAULT_QUEUE_NAME, 42L);
        ResourcePayload resourcePayload = createResourcePayload(DEFAULT_QUEUE_NAME, 42L);
        
        mockFasitSearch(searchResult);
        mockResourceGet(42L, resourcePayload);

        Map<String, String> input = createBasicQueueInput(DEFAULT_QUEUE_NAME);
        int orderId = executeQueueOperation("start", input);

        verify(mq).enableQueue(any(MqQueueManager.class), any(MqQueue.class));
        verifyLifecyclePutCalled();
        assertOrderResult((long) orderId, OrderType.MQ, OrderStatus.SUCCESS, OrderOperation.START);
    }
    
    @Test
    public void testRemoveEmptyQueue() {
        Map<String, String> input = createBasicQueueInput(DEFAULT_QUEUE_NAME);

        // No matching Fasit resources — queue exists only in MQ
        mockEmptyFasitSearch();

        int orderId = executeQueueOperation("remove", input);

        verifyFasitSearchCalled();
        // Queue is empty so all 3 MQ queues (alias, name, backout) should be deleted
        verify(mq, times(3)).deleteQueue(any(MqQueueManager.class), anyString());
        verifyNoFasitResourceDelete();
        
        assertOrderResult((long) orderId, OrderType.MQ, OrderStatus.SUCCESS, OrderOperation.DELETE);
    }

    @Test
    public void testRemoveNonEmptyQueue() {
        // Set up a search result with a matching queue resource in Fasit
        SearchResultPayload searchResult = createSearchResult(NON_EMPTY_QUEUE_NAME, 123L);
        ResourcePayload resourcePayload = createResourcePayload(NON_EMPTY_QUEUE_NAME, 123L);
        
        mockFasitSearch(searchResult);
        mockResourceGet(123L, resourcePayload);

        Map<String, String> input = createBasicQueueInput(NON_EMPTY_QUEUE_NAME);

        int orderId = executeQueueOperation("remove", input);

        // Queue is non-empty so no MQ deletes should happen
        verify(mq, never()).deleteQueue(any(MqQueueManager.class), anyString());
        verifyFasitSearchCalled();
        verifyNoFasitResourceDelete(); // No Fasit resource delete since queue was not empty
        
        assertOrderResult((long) orderId, OrderType.MQ, OrderStatus.ERROR, OrderOperation.DELETE);
    }
}
