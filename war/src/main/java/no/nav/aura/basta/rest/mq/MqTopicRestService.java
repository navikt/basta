package no.nav.aura.basta.rest.mq;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.mq.MqTopic;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.mq.MQObjectType;
import no.nav.aura.basta.domain.input.mq.MqOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.mq.MqOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.ValidationHelper;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.LifeCycleStatusDO;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

/**
 * @author v137023
 *
 */
@Component
@Path("/v1/mq/order/topic")
@Transactional
public class MqTopicRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqTopicRestService.class);

    private OrderRepository orderRepository;
    private FasitRestClient fasit;
    private FasitUpdateService fasitUpdateService;
    private MqService mq;

    // for cglib
    protected MqTopicRestService() {
    }

    @Inject
    public MqTopicRestService(OrderRepository orderRepository, FasitRestClient fasit, FasitUpdateService fasitUpdateService, MqService mq) {
        this.orderRepository = orderRepository;
        this.fasit = fasit;
        this.fasitUpdateService = fasitUpdateService;
        this.mq = mq;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTopic(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Create mq topic request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Topic);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        validateInput(request);

        MqTopic topic = input.getTopic();

        Order order = new Order(OrderType.MQ, OrderOperation.CREATE, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Creating topic " + topic + " on " + input.getQueueManagerUri(), "mq"));
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

        try {
            

            if (mq.topicExists(queueManager, topic.getName())) {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + topic.getName() + " already exists", "mq", StatusLogLevel.warning));
            } else {
                mq.createTopic(queueManager, topic);
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + topic.getName() + " created", "mq", StatusLogLevel.success));
            }
            result.add(topic);

            Optional<ResourceElement> foundTopic = findInFasit(input);
            if (foundTopic.isPresent()) {
                order.getStatusLogs().add(new OrderStatusLog("Fasit", "Topic " + input.getAlias() + " already exists", "fasit", StatusLogLevel.warning));
            }

            ResourceElement fasitTopic = foundTopic.orElseGet(() -> new ResourceElement(ResourceTypeDO.Topic, input.getAlias()));
            fasitTopic.setEnvironmentClass(input.getEnvironmentClass().name());
            fasitTopic.setEnvironmentName(input.getEnvironmentName());
            fasitTopic.addProperty(new PropertyElement("topicString", topic.getTopicString()));
//            fasitQueue.addProperty(new PropertyElement("queueManager", input.getQueueManagerUri().toString()));
            Optional<ResourceElement> createdResource = fasitUpdateService.createResource(fasitTopic, order);
            if (createdResource.isPresent()) {
                result.add(createdResource.get());
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Topic creation failed", e);
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic creation failed: " + e.getMessage(), "mq", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }
    
    private Optional<ResourceElement> findInFasit(MqOrderInput input) {
        Collection<ResourceElement> resources = fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, input.getAppliation(), ResourceTypeDO.Topic,
                input.getAlias());
        return resources.stream().findFirst();
    }

  

    public static void validateInput(Map<String, String> request) {
        ValidationHelper.validateRequest("/validation/mqTopicSchema.json", request);
    }

}
