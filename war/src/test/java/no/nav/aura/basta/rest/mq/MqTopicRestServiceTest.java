package no.nav.aura.basta.rest.mq;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.mq.MqTopic;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.mq.MQObjectType;
import no.nav.aura.basta.domain.input.mq.MqOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.AbstractRestServiceTest;
import no.nav.aura.basta.rest.RestServiceTestUtils;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

public class MqTopicRestServiceTest extends AbstractRestServiceTest {

    private static final String EXISTING_TOPICSTRING = "existing/topic";
    private MqService mq;
    private MqTopicRestService service;
    private ResourceElement topicInFasit;

    @Before
    public void setup() {
        System.setProperty("mqadmin.u.username", "mqadmin");
        System.setProperty("mqadmin.u.password", "secret");

        topicInFasit = new ResourceElement(ResourceTypeDO.Topic, "alias");
        topicInFasit.setId(100L);
        topicInFasit.addProperty(new PropertyElement("topicString", EXISTING_TOPICSTRING));

        mq = mock(MqService.class);
        FasitUpdateService fasitUpdateService = new FasitUpdateService(fasit);
        service = new MqTopicRestService(orderRepository, fasit, fasitUpdateService, mq);

        when(fasit.registerResource(any(ResourceElement.class), anyString())).thenReturn(topicInFasit);
        when(fasit.updateResource(anyInt(), any(ResourceElement.class), anyString())).thenReturn(topicInFasit);

        when(mq.getTopics(any(MqQueueManager.class))).thenReturn(Arrays.asList(new MqTopic("existingTopic", EXISTING_TOPICSTRING)));
    }

    private void mockFasitFindTopic() {
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Topic), anyString())).thenReturn(Arrays.asList(topicInFasit));
    }

    @Test
    public void testCreateTopic() {
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setApplication("myApp");
        input.setEnvironment("myenv");
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setTopicString("u1/some/topic");
        input.setAlias("myapp_sometopic");
        Response response = service.createTopic(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).createTopic(any(MqQueueManager.class), any(MqTopic.class));
        verify(fasit).registerResource(any(ResourceElement.class), anyString());
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.CREATE, order.getOrderOperation());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateTopicNoAccess() {
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.p);
        service.createTopic(input.copy(), RestServiceTestUtils.createUriInfo());
    }

    @Test
    public void testStop() {
        mockFasitFindTopic();
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setTopicString(EXISTING_TOPICSTRING);
        Response response = service.stopTopic(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).disableTopic(any(MqQueueManager.class), any(MqTopic.class));
        verify(fasit).updateResource(eq(topicInFasit.getId()), any(ResourceElement.class), anyString());
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.STOP, order.getOrderOperation());
    }

    @Test
    public void testStart() {
        mockFasitFindTopic();
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setTopicString(EXISTING_TOPICSTRING);
        Response response = service.startTopic(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).enableTopic(any(MqQueueManager.class), any(MqTopic.class));
        verify(fasit).updateResource(eq(topicInFasit.getId()), any(ResourceElement.class), anyString());
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.START, order.getOrderOperation());
    }

    @Test
    public void testRemove() {
        mockFasitFindTopic();
        when(fasit.deleteResource(anyInt(), anyString())).thenReturn(Response.noContent().build());
        login("user", "user");
        MqOrderInput input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setQueueManager("mq://host:123/mdlclient03");
        input.setTopicString(EXISTING_TOPICSTRING);
        Response response = service.removeTopic(input.copy(), RestServiceTestUtils.createUriInfo());
        verify(mq).deleteTopic(any(MqQueueManager.class), any(MqTopic.class));
        verify(fasit).deleteResource(eq(topicInFasit.getId()), anyString());
        assertEquals(201, response.getStatus());
        Order order = getCreatedOrderFromResponseLocation(response);
        assertEquals(OrderType.MQ, order.getOrderType());
        assertEquals(OrderStatus.SUCCESS, order.getStatus());
        assertEquals(OrderOperation.DELETE, order.getOrderOperation());
    }

}
