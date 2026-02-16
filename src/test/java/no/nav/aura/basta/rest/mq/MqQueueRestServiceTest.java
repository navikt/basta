package no.nav.aura.basta.rest.mq;

import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MqQueueRestServiceTest extends AbstractRestServiceTest {
	private static final Logger log = LoggerFactory.getLogger(MqQueueRestServiceTest.class);

	private MqService mq;
    private Map<EnvironmentClass, MqAdminUser> envCredMap = new HashMap<>();

    private static final String MQ_QUEUE_URL = "/rest/v1/mq/order/queue";

    @BeforeEach
    public void setup() {
        mq = applicationContext.getBean(MqService.class);
        
        envCredMap.put(EnvironmentClass.u, new MqAdminUser("mqadmin", "secret", "SRVAURA.ADMIN"));

        when(restClient.createFasitResource(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.of("100"));
        when(restClient.updateFasitResource(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.of("200"));

        when(mq.getQueue(any(MqQueueManager.class), eq("MYENV_MYAPP_SOMEQUEUE"))).thenReturn(Optional.of(new MqQueue(
                "someQueue", 1, 1, "mockup queue for test")));
        when(mq.getQueue(any(MqQueueManager.class), eq("MYENV_MYAPP_NONEMPTYQUEUE"))).thenReturn(Optional.of(new MqQueue(
                "nonEmptyQueue", 1, 1, "mockup queue for test")));

        when(mq.getCredentialMap()).thenReturn(envCredMap);

        when(mq.isQueueEmpty(any(MqQueueManager.class), eq("SOMEQUEUE"))).thenReturn(true);
        when(mq.isQueueEmpty(any(MqQueueManager.class), eq("NONEMPTYQUEUE"))).thenReturn(false);
    }

    @Test
    public void testCreateQueue() {
        when(restClient.findFasitResources(eq(ResourceType.Queue), any(), any(ScopePayload.class))).thenReturn(ResourcesListPayload.emptyResourcesList());
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
        when(restClient.findFasitResources(eq(ResourceType.Queue), any(), any(ScopePayload.class))).thenReturn(ResourcesListPayload.emptyResourcesList());
        
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("queueManager", "mq://host:123/mdlclient03");
        input.put("mqQueueName", "MYENV_MYAPP_SOMEQUEUE");
        input.put("mqOrderType", "Queue");
        
        HttpEntity<Map<String, String>> requestEntity = createHTTPEntity(input);
        
        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.put(MQ_QUEUE_URL + "/stop")
						.then()
						.statusCode(201)
						.extract().path("id");
        
        verify(mq).disableQueue(any(MqQueueManager.class), any(MqQueue.class));
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);

        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.STOP, order.getOrderOperation());
    }

	private HttpEntity<Map<String, String>> createHTTPEntity(Map<String, String> input) {
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(input, headers);
		return requestEntity;
	}
    
    @Test
    public void testStart() {
        when(restClient.findFasitResources(eq(ResourceType.Queue), any(), any(ScopePayload.class))).thenReturn(ResourcesListPayload.emptyResourcesList());
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("queueManager", "mq://host:123/mdlclient03");
        input.put("mqQueueName", "MYENV_MYAPP_SOMEQUEUE");
        input.put("mqOrderType", "Queue");
        
        HttpEntity<Map<String, String>> requestEntity = createHTTPEntity(input);

        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.put(MQ_QUEUE_URL + "/start")
						.then()
						.statusCode(201)
						.extract().path("id");
        
        verify(mq).enableQueue(any(MqQueueManager.class), any(MqQueue.class));
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.START, order.getOrderOperation());
    }
    
    @Test
    public void testRemoveEmptyQueue() {
        when(restClient.findFasitResources(eq(ResourceType.Queue), any(), any(ScopePayload.class))).thenReturn(ResourcesListPayload.emptyResourcesList());
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("queueManager", "mq://host:123/mdlclient03");
        input.put("mqQueueName", "MYENV_MYAPP_SOMEQUEUE");
        input.put("mqOrderType", "Queue");
        
        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.put(MQ_QUEUE_URL + "/remove")
						.then()
						.statusCode(201)
						.extract().path("id");
        
        verify(mq, times(3)).deleteQueue(any(MqQueueManager.class), anyString());
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);

        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.DELETE, order.getOrderOperation());
    }

    @Test
    public void testRemoveNonEmptyQueue() {
        when(restClient.findFasitResources(eq(ResourceType.Queue), any(), any(ScopePayload.class))).thenReturn(ResourcesListPayload.emptyResourcesList());
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("queueManager", "mq://host:123/mdlclient03");
        input.put("mqQueueName", "MYENV_MYAPP_NONEMPTYQUEUE");
        input.put("mqOrderType", "Queue");
        
        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.put(MQ_QUEUE_URL + "/remove")
						.then()
						.statusCode(201)
						.extract().path("id");
        verify(mq, never()).deleteQueue(any(MqQueueManager.class), anyString());
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.ERROR, order.getStatus());
        assertEquals(OrderOperation.DELETE, order.getOrderOperation());
    }
}
