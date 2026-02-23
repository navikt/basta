package no.nav.aura.basta.rest.mq;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.restassured.http.ContentType;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
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
        mq = applicationContext.getBean(MqService.class);
        
        channelInFasit = new ResourcePayload(ResourceType.Channel, "alias");
        channelInFasit.id = 100L;
        channelInFasit.addProperty("name", EXISTING_CHANNEL);
        
        envCredMap.put(EnvironmentClass.u, new MqAdminUser("mqadmin", "secret", "SRVAURA.ADMIN"));
        System.setProperty("fasit_lifecycle_v1_url", "https://thefasitresourceapi.com");

        when(restClient.createFasitResource(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.of(channelInFasit.id.toString()));
        when(restClient.updateFasitResource(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.of("OK"));
        when(mq.findChannelNames(any(MqQueueManager.class), eq(EXISTING_CHANNEL))).thenReturn(Collections.singletonList(EXISTING_CHANNEL));
        when(mq.getCredentialMap()).thenReturn(envCredMap);
       
    }
    
    private void mockExists() {
       when(restClient.findFasitResources(eq(ResourceType.Channel), any(), any(ScopePayload.class))).thenReturn(new ResourcesListPayload(channelInFasit));
    }

    @Test
    public void testCreateChannel() {
        when(restClient.findFasitResources(eq(ResourceType.Channel), any(), any(ScopePayload.class))).thenReturn(ResourcesListPayload.emptyResourcesList());
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
						.statusCode(201)
						.extract().path("id");
        
        
        verify(mq).createChannel(any(MqQueueManager.class), any(MqChannel.class));
        verify(restClient).createFasitResource(any(), any(), any(), any());
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
//        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
//        input.setEnvironmentClass(EnvironmentClass.u);
//        input.setQueueManager("mq://host:123/mdlclient03");
//        input.setMqChannelName(EXISTING_CHANNEL);
        
        Map<String, String> input = new HashMap<>();
        input.put(MqOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u.name());
        input.put(MqOrderInput.ENVIRONMENT_NAME, "u1");
        input.put(MqOrderInput.QUEUE_MANAGER, "mq://host:123/mdlclient03");
        input.put(MqOrderInput.MQ_CHANNEL_NAME, EXISTING_CHANNEL);
        
		when(restClient.findFasitResources(eq(ResourceType.Channel), any(), any(ScopePayload.class)))
				.thenReturn(new ResourcesListPayload(channelInFasit));
		
        int orderId = given()
						.auth().preemptive().basic("user", "user")
						.body(input)
						.contentType(ContentType.JSON)
						.when()
						.put("/rest/v1/mq/order/channel/stop")
						.then()
						.statusCode(201)
						.extract().path("id");
        
        verify(restClient).updateFasitResource(anyString(), anyString(), anyString(), anyString());
        verify(mq).stopChannel(any(MqQueueManager.class), any(MqChannel.class));
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.STOP, order.getOrderOperation());
    }

    @Test
    public void testStart() {
        mockExists();
//        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
//        input.setEnvironmentClass(EnvironmentClass.u);
//        input.setQueueManager("mq://host:123/mdlclient03");
//        input.setMqChannelName(EXISTING_CHANNEL);
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
        verify(restClient).updateFasitResource(anyString(), anyString(), anyString(), anyString());
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.START, order.getOrderOperation());
    }

    @Test
    public void testRemove() {
        mockExists();
        when(restClient.deleteFasitResource(anyString(), anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
//        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
//        input.setEnvironmentClass(EnvironmentClass.u);
//        input.setEnvironment("u1");
//        input.setQueueManager("mq://host:123/mdlclient03");
//        input.setMqChannelName(EXISTING_CHANNEL);
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
						.statusCode(201)
						.extract().path("id");

        verify(mq).deleteChannel(any(MqQueueManager.class), any(MqChannel.class));
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.DELETE, order.getOrderOperation());
    }

}
