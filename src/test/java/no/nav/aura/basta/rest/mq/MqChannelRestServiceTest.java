package no.nav.aura.basta.rest.mq;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ScopePayload;
import no.nav.aura.basta.backend.mq.MqAdminUser;
import no.nav.aura.basta.backend.mq.MqChannel;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.mq.MQObjectType;
import no.nav.aura.basta.domain.input.mq.MqOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.AbstractRestServiceTest;
import no.nav.aura.basta.rest.RestServiceTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MqChannelRestServiceTest extends AbstractRestServiceTest {

    private static final String EXISTING_CHANNEL = "U1_MYAPP";
    private MqService mq;
    private MqChannelRestService service;
    private ResourcePayload channelInFasit;
    private final Map<EnvironmentClass, MqAdminUser> envCredMap = new HashMap<>();

    @BeforeEach
    public void setup() {
        channelInFasit = new ResourcePayload()
                .withId("100")
                .withType(ResourceType.channel)
                .withAlias("alias")
                .withProperty("name", EXISTING_CHANNEL);

        mq = mock(MqService.class);
        envCredMap.put(EnvironmentClass.u, new MqAdminUser("mqadmin", "secret", "SRVAURA.ADMIN"));
        System.setProperty("fasit_lifecycle_v1_url", "https://thefasitresourceapi.com");
        FasitUpdateService fasitUpdateService = new FasitUpdateService(null, fasit);
        service = new MqChannelRestService(orderRepository, fasit, fasitUpdateService, mq);

        when(fasit.createFasitResource(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.of(channelInFasit.id));
        when(mq.findChannelNames(any(MqQueueManager.class), eq(EXISTING_CHANNEL))).thenReturn(Collections.singletonList(EXISTING_CHANNEL));
        when(mq.getCredentialMap()).thenReturn(envCredMap);
       
    }

    private void mockExists() {
       when(fasit.findFasitResources(eq(ResourceType.channel), any(), any(ScopePayload.class))).thenReturn(new ResourcesListPayload(channelInFasit));
    }

    @Test
    public void testCreateChannel() {
        when(fasit.findFasitResources(eq(ResourceType.channel), any(), any(ScopePayload.class))).thenReturn(ResourcesListPayload.emptyResourcesList());
        login();
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setApplication("myApp");
        input.setEnvironment("myenv");
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName("U4_MYAPP");
        input.setAlias("myapp_channel");
        Response response = service.createMqChannel(input.copy(), RestServiceTestUtils.createUriInfo());
        //verify(mq).createChannel(any(MqQueueManager.class), any(MqChannel.class));
        verify(fasit).createFasitResource(any(), any(), any(), any());
        Assertions.assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.CREATE, order.getOrderOperation());
    }

    @Test
    public void testCreateChannelNoAccess() {
        login();
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.p);
        input.setApplication("myApp");
        input.setEnvironment("myenv");
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName("U4_MYAPP");
        input.setAlias("myapp_channel");
        assertThrows(NotAuthorizedException.class , () ->
            service.createMqChannel(input.copy(), RestServiceTestUtils.createUriInfo()));
    }

    @Test
    public void testStop() {
        mockExists();
        login();
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName(EXISTING_CHANNEL);
        Response response = service.stopChannel(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).stopChannel(any(MqQueueManager.class), any(MqChannel.class));
        //verify(fasit).updateResource(eq(channelInFasit.getId()), any(ResourceElement.class), anyString());
        Assertions.assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.STOP, order.getOrderOperation());
    }

    @Test
    public void testStart() {
        mockExists();
        login();
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName(EXISTING_CHANNEL);
        Response response = service.startChannel(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).startChannel(any(MqQueueManager.class), any(MqChannel.class));
        //verify(fasit).updateResource(eq(channelInFasit.getId()), any(ResourceElement.class), anyString());
        Assertions.assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.START, order.getOrderOperation());
    }

    @Test
    public void testRemove() {
        mockExists();
        when(fasit.deleteFasitResource(anyString(), anyString(), anyString())).thenReturn(Response.noContent().build());
        login();
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setEnvironment("u1");
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName(EXISTING_CHANNEL);
        Response response = service.removeChannel(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).deleteChannel(any(MqQueueManager.class), any(MqChannel.class));
        Assertions.assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        Assertions.assertEquals(OrderType.MQ, order.getOrderType());
        Assertions.assertEquals(OrderStatus.SUCCESS, order.getStatus());
        Assertions.assertEquals(OrderOperation.DELETE, order.getOrderOperation());
    }

}
