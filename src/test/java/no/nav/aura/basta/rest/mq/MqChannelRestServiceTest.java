package no.nav.aura.basta.rest.mq;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.restassured.http.ContentType;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.backend.mq.MqAdminUser;
import no.nav.aura.basta.backend.mq.MqChannel;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.mq.MqOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.AbstractRestServiceTest;

public class MqChannelRestServiceTest extends AbstractRestServiceTest {

    private static final String EXISTING_CHANNEL = "U1_MYAPP";
    private MqService mq;
    private ResourcePayload channelInFasit;
    private final Map<EnvironmentClass, MqAdminUser> envCredMap = new HashMap<>();

    @BeforeEach
    public void setup() {
        // Reset the mock before each test to clear previous interactions
        reset(restTemplate);
        
        mq = applicationContext.getBean(MqService.class);

        channelInFasit = new ResourcePayload(ResourceType.Channel, "alias");
        channelInFasit.id = 100L;
        channelInFasit.addProperty("name", EXISTING_CHANNEL);
        
        envCredMap.put(EnvironmentClass.u, new MqAdminUser("mqadmin", "secret", "SRVAURA.ADMIN"));
    }
    
    private void mockExists() {
        // Mock MqService to return the existing channel name (so channelExists() returns true)
        when(mq.findChannelNames(any(MqQueueManager.class), eq(EXISTING_CHANNEL)))
                .thenReturn(List.of(EXISTING_CHANNEL));

        // Mock FasitRestClient to return a list with the existing channel
        List<ResourcePayload> existingChannels = new ArrayList<>();
        existingChannels.add(channelInFasit);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<ResourcePayload>>>any()
        )).thenReturn(new ResponseEntity<>(existingChannels, HttpStatus.OK));
    }


    @Test
    public void testCreateChannel() {
        // Mock GET request to find existing channels by alias - return empty list
        when(restTemplate.exchange(
			anyString(),
			eq(HttpMethod.GET),
			any(HttpEntity.class),
			ArgumentMatchers.<ParameterizedTypeReference<List<ResourcePayload>>>any()
			)).thenReturn(new ResponseEntity<>(
					new ArrayList<>(), HttpStatus.OK));
        
        // Mock POST request to create resource in Fasit
        when(restTemplate.exchange(
			contains("/api/v2/resources"),
			eq(HttpMethod.POST),
			any(HttpEntity.class),
			eq(String.class)
		)).thenReturn(new ResponseEntity<>("{\"id\":\"" + channelInFasit.id + "\"}", HttpStatus.CREATED));

//        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
//        input.setEnvironmentClass(EnvironmentClass.u);
//        input.setApplication("myApp");
//        input.setEnvironment("myenv");
//        input.setQueueManager("mq://host:123/mdlclient03");
//        input.setMqChannelName("U4_MYAPP");
//        input.setAlias("myapp_channel");
      
    	Map<String, String> input = new HashMap<>();
		input.put(MqOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u.name());
		input.put(MqOrderInput.APPLICATION, "myApp");
		input.put(MqOrderInput.ENVIRONMENT_NAME, "myenv");
		input.put(MqOrderInput.QUEUE_MANAGER, "mq://host:123/mdlclient03");
		input.put(MqOrderInput.MQ_CHANNEL_NAME, "U4_MYAPP");
		input.put(MqOrderInput.ALIAS, "myapp_channel");
        
        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.post("/rest/v1/mq/order/channel")
						.then()
						.log().all()
						.statusCode(201)
						.extract().path("id");
        
        verify(mq).createChannel(any(MqQueueManager.class), any(MqChannel.class));
        // Verify that POST was called to create the resource in Fasit
//        verify(restTemplate).exchange(contains("/api/v2/resources"), eq(HttpMethod.POST), any(HttpEntity.class), eq(ResourcePayload.class));
        verify(restTemplate).exchange(contains("/api/v2/resources"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.CREATE, order.getOrderOperation());
    }

    @Test
    public void testCreateChannelNoAccess() {
    	Map<String, String> input = new HashMap<>();
		input.put(MqOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u.name());
		input.put(MqOrderInput.APPLICATION, "myApp");
		input.put(MqOrderInput.ENVIRONMENT_NAME, "myenv");
		input.put(MqOrderInput.QUEUE_MANAGER, "mq://host:123/mdlclient03");
		input.put(MqOrderInput.MQ_CHANNEL_NAME, "U4_MYAPP");
		input.put(MqOrderInput.ALIAS, "myapp_channel");
		given()
			.body(input)
			.contentType(ContentType.JSON)
			.when()
			.post("/rest/v1/mq/order/channel")
			.then()
			.statusCode(401);
    }

    @Test
    public void testStop() {
        mockExists();
        
        // Mock PUT request to update resource in Fasit
        when(restTemplate.exchange(
    		contains("/api/v2/resources/"),
    		eq(HttpMethod.PUT),
    		any(HttpEntity.class),
    		eq(String.class)
    		)).thenReturn(new ResponseEntity<>("{\"id\":\"" + channelInFasit.id + "\"}", HttpStatus.OK));
        
        when(restTemplate.exchange(
			contains("/api/v1/lifecycle/"),
			eq(HttpMethod.PUT),
			any(HttpEntity.class),
			eq(String.class)
			)).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        Map<String, String> input = new HashMap<>();
        input.put(MqOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u.name());
        input.put(MqOrderInput.ENVIRONMENT_NAME, "u1");
        input.put(MqOrderInput.QUEUE_MANAGER, "mq://host:123/mdlclient03");
        input.put(MqOrderInput.MQ_CHANNEL_NAME, EXISTING_CHANNEL);
        
        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.put("/rest/v1/mq/order/channel/stop")
						.then()
						.log().all()
						.statusCode(201)
						.extract().path("id");
        
//        verify(restClient).updateFasitResource(anyString(), anyString(), anyString(), anyString());

        verify(mq).stopChannel(any(MqQueueManager.class), any(MqChannel.class));
        verify(restTemplate).exchange(contains("/api/v1/lifecycle/"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class));
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.STOP, order.getOrderOperation());
    }

    @Test
    public void testStart() {
        mockExists();
        
        // Mock PUT request to update resource in Fasit
        when(restTemplate.exchange(
    		contains("/api/v2/resources/"),
    		eq(HttpMethod.PUT),
    		any(HttpEntity.class),
    		eq(String.class)
    		)).thenReturn(new ResponseEntity<>("{\"id\":\"" + channelInFasit.id + "\"}", HttpStatus.OK));
        
        when(restTemplate.exchange(
			contains("/api/v1/lifecycle/"),
			eq(HttpMethod.PUT),
			any(HttpEntity.class),
			eq(String.class)
			)).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
        
        Map<String, String> input = new HashMap<>();
        input.put(MqOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u.name());
        input.put(MqOrderInput.ENVIRONMENT_NAME, "u1");
        input.put(MqOrderInput.QUEUE_MANAGER, "mq://host:123/mdlclient03");
        input.put(MqOrderInput.MQ_CHANNEL_NAME, EXISTING_CHANNEL);

        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.put("/rest/v1/mq/order/channel/start")
						.then()
						.statusCode(201)
						.extract().path("id");
        verify(mq).startChannel(any(MqQueueManager.class), any(MqChannel.class));
        verify(restTemplate).exchange(contains("/api/v1/lifecycle/"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class));
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.START, order.getOrderOperation());
    }

    @Test
    public void testRemove() {
    	mockExists();
    	
        // Mock FasitRestClient for DELETE operation
        ResponseEntity<String> deleteResponse = new ResponseEntity<>("", HttpStatus.NO_CONTENT);
        
        when(restTemplate.exchange(
			contains("/api/v2/resources/"),
			eq(HttpMethod.DELETE),
			any(HttpEntity.class),
			eq(String.class)
			)).thenReturn(deleteResponse);
				
        Map<String, String> input = new HashMap<>();
        input.put(MqOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u.name());
        input.put(MqOrderInput.ENVIRONMENT_NAME, "u1");
        input.put(MqOrderInput.QUEUE_MANAGER, "mq://host:123/mdlclient03");
        input.put(MqOrderInput.MQ_CHANNEL_NAME, EXISTING_CHANNEL);
        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.put("/rest/v1/mq/order/channel/remove")
						.then()
						.log().all()
						.statusCode(201)
						.extract().path("id");

        verify(mq).deleteChannel(any(MqQueueManager.class), any(MqChannel.class));
        verify(restTemplate).exchange(contains("/api/v2/resources/"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class));
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.DELETE, order.getOrderOperation());
    }

}
