package no.nav.aura.basta.rest.mq;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.mq.MqQueue;
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
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

public class MqQueueRestServiceTest extends AbstractRestServiceTest {

    private MqService mq;
    private MqQueueRestService service;

    @Before
    public void setup() {
        System.setProperty("mqadmin_u_username", "mqadmin");
        System.setProperty("mqadmin_u_password", "secret");
        mq = mock(MqService.class);
        FasitUpdateService fasitUpdateService = new FasitUpdateService(fasit, null);
        service = new MqQueueRestService(orderRepository, fasit, fasitUpdateService, mq);

        when(fasit.registerResource(any(ResourceElement.class), anyString())).thenReturn(new ResourceElement(ResourceTypeDO.Queue, "alias"));
        when(fasit.updateResource(anyInt(), any(ResourceElement.class), anyString())).thenReturn(new ResourceElement(ResourceTypeDO.Queue, "alias"));

        when(mq.getQueue(any(MqQueueManager.class), anyString())).thenReturn(Optional.of(new MqQueue("myQueue", 1, 1, "mockup queue for test")));
    }

    @Test
    public void testCreateQueue() {
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setApplication("myApp");
        input.setEnvironment("myenv");
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqQueueName("MYENV_MYAPP_SOMEQUEUE");
        input.setAlias("myapp_somequeue");
        input.setMaxMessageSize(1);
        input.setQueueDepth(500);
        input.setCreateBQ(true);
        input.setBackoutThreshold(4);
        Response response = service.createMqQueue(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).createQueue(any(MqQueueManager.class), any(MqQueue.class));
        verify(mq).createAlias(any(MqQueueManager.class), any(MqQueue.class));
        verify(mq).createBackoutQueue(any(MqQueueManager.class), any(MqQueue.class));
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.CREATE, order.getOrderOperation());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateQueueNoAccess() {
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.p);
        service.createMqQueue(input.copy(), RestServiceTestUtils.createUriInfo());
    }

    @Test
    public void testStop() {
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqQueueName("MYENV_MYAPP_SOMEQUEUE");
        Response response = service.stopQueue(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).disableQueue(any(MqQueueManager.class), any(MqQueue.class));
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.STOP, order.getOrderOperation());
    }
    
    @Test
    public void testStart() {
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqQueueName("MYENV_MYAPP_SOMEQUEUE");
        Response response = service.startQueue(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).enableQueue(any(MqQueueManager.class), any(MqQueue.class));
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.START, order.getOrderOperation());
    }
    
    @Test
    public void testRemove() {
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setMqQueueName("MYENV_MYAPP_SOMEQUEUE");
        Response response = service.removeQueue(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq, times(3)).deleteQueue(any(MqQueueManager.class), anyString());
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.DELETE, order.getOrderOperation());
    }

}