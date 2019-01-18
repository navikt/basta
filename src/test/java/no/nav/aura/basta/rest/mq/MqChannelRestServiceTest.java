package no.nav.aura.basta.rest.mq;

import no.nav.aura.basta.backend.FasitUpdateService;
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
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MqChannelRestServiceTest extends AbstractRestServiceTest {

    private static final String EXISTING_CHANNEL = "U1_MYAPP";
    private MqService mq;
    private MqChannelRestService service;
    private ResourceElement channelInFasit;
    private Map<EnvironmentClass, MqAdminUser> envCredMap = new HashMap<>();

    @Before
    public void setup() {
        channelInFasit = new ResourceElement(ResourceTypeDO.Channel, "alias");
        channelInFasit.setId(100L);
        channelInFasit.addProperty(new PropertyElement("name", EXISTING_CHANNEL));

        mq = mock(MqService.class);
        envCredMap.put(EnvironmentClass.u, new MqAdminUser("mqadmin", "secret", "SRVAURA.ADMIN"));
        FasitUpdateService fasitUpdateService = new FasitUpdateService(fasit, null);
        service = new MqChannelRestService(orderRepository, fasit, fasitUpdateService, mq);

        when(fasit.registerResource(any(ResourceElement.class), anyString())).thenReturn(channelInFasit);
        //when(fasit.updateResource(anyInt(), any(ResourceElement.class), anyString())).thenReturn(channelInFasit);

        when(mq.findChannelNames(any(MqQueueManager.class), eq(EXISTING_CHANNEL))).thenReturn(Arrays.asList(EXISTING_CHANNEL));
        when(mq.getCredentialMap()).thenReturn(envCredMap);
       
    }

    private void mockExists() {
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Channel), anyString())).thenReturn(Arrays.asList(channelInFasit));
    }

    @Test
    public void testCreateChannel() {
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setApplication("myApp");
        input.setEnvironment("myenv");
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName("U4_MYAPP");
        input.setAlias("myapp_channel");
        Response response = service.createMqChannel(input.copy(), RestServiceTestUtils.createUriInfo());
        //verify(mq).createChannel(any(MqQueueManager.class), any(MqChannel.class));
        verify(fasit).registerResource(any(ResourceElement.class), anyString());
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.CREATE, order.getOrderOperation());
    }

    @Test(expected = NotAuthorizedException.class)
    public void testCreateChannelNoAccess() {
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.p);
        input.setApplication("myApp");
        input.setEnvironment("myenv");
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName("U4_MYAPP");
        input.setAlias("myapp_channel");
        service.createMqChannel(input.copy(), RestServiceTestUtils.createUriInfo());
    }

    @Test
    public void testStop() {
        mockExists();
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName(EXISTING_CHANNEL);
        Response response = service.stopChannel(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).stopChannel(any(MqQueueManager.class), any(MqChannel.class));
        //verify(fasit).updateResource(eq(channelInFasit.getId()), any(ResourceElement.class), anyString());
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.STOP, order.getOrderOperation());
    }

    @Test
    public void testStart() {
        mockExists();
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName(EXISTING_CHANNEL);
        Response response = service.startChannel(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).startChannel(any(MqQueueManager.class), any(MqChannel.class));
        //verify(fasit).updateResource(eq(channelInFasit.getId()), any(ResourceElement.class), anyString());
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.START, order.getOrderOperation());
    }

    @Test
    public void testRemove() {
        mockExists();
        when(fasit.deleteResource(anyInt(), anyString())).thenReturn(Response.noContent().build());
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Channel);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqChannelName(EXISTING_CHANNEL);
        Response response = service.removeChannel(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).deleteChannel(any(MqQueueManager.class), any(MqChannel.class));
        //verify(fasit).deleteResource(eq(channelInFasit.getId()), anyString());
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.DELETE, order.getOrderOperation());
    }

}
