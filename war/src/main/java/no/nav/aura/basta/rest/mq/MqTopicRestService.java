package no.nav.aura.basta.rest.mq;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.mq.MqTopic;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

            if (topicExists(queueManager, topic.getTopicString())) {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + topic.getName() + " already exists", "mq", StatusLogLevel.warning));
            } else {
                mq.createTopic(queueManager, topic);
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + topic.getName() + " created", "mq", StatusLogLevel.success));
            }
            result.add(topic);

            Collection<ResourceElement> foundTopic = findInFasitByAlias(input);
            if (!foundTopic.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Fasit", "Topic " + input.getAlias() + " already exists", "fasit", StatusLogLevel.warning));
            } else {
                ResourceElement fasitTopic = new ResourceElement(ResourceTypeDO.Topic, input.getAlias());
                fasitTopic.setEnvironmentClass(input.getEnvironmentClass().name());
                fasitTopic.setEnvironmentName(input.getEnvironmentName());
                fasitTopic.addProperty(new PropertyElement("topicString", topic.getTopicString()));
                fasitTopic.addProperty(new PropertyElement("queueManager", input.getQueueManagerUri().toString()));
                Optional<ResourceElement> createdResource = fasitUpdateService.createResource(fasitTopic, order);
                if (createdResource.isPresent()) {
                    result.add(createdResource.get());
                }
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

    /**
     * Returns 200 if ok, 400 if format failure and 409 if conflict with existing resources
     */
    @PUT
    @Path("validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateTopic(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Validate topic request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Topic);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        validateInput(request);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        Map<String, String> errorResult = new HashMap<>();
        if (topicExists(queueManager, input.getTopicString())) {
            errorResult.put(MqOrderInput.TOPIC_STRING, "TopicString " + input.getTopicString() + " allready exist in QueueManager");
        }
        if (!findInFasitByAlias(input).isEmpty()) {
            errorResult.put(MqOrderInput.ALIAS, "Alias " + input.getAlias() + " allready exist in Fasit");
        }

        if (errorResult.isEmpty()) {
            return Response.ok(errorResult).build();
        }
        return Response.status(Status.CONFLICT).entity(errorResult).build();
    }

    @PUT
    @Path("stop")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopTopic(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Stop mq topic request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Topic);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.TOPIC_STRING);

        MqTopic topic = new MqTopic("", input.getTopicString());

        Order order = new Order(OrderType.MQ, OrderOperation.STOP, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Stopping topic " + topic + " on " + input.getQueueManagerUri(), "mq"));
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

        try {
            Optional<MqTopic> mqTopic = findTopic(queueManager, topic.getTopicString());
            if (mqTopic.isPresent()) {
                mq.disableTopic(queueManager, mqTopic.get());
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + mqTopic.get().getName() + " stopped", "mq", StatusLogLevel.success));
            } else {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + topic.getTopicString() + " do not exist", "mq", StatusLogLevel.warning));
            }
            result.add(topic);

            Collection<ResourceElement> foundTopic = findInFasitByTopicString(input);
            if (foundTopic.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Fasit", "Topic " + input.getTopicString() + " not found", "fasit", StatusLogLevel.warning));
            } else {
                for (ResourceElement resourceElement : foundTopic) {
                    fasitUpdateService.updateResource(resourceElement, LifeCycleStatusDO.STOPPED, order);
                    result.add(resourceElement);
                }
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Topic stop failed", e);
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic stop failed: " + e.getMessage(), "mq", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    @PUT
    @Path("start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startTopic(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Start mq topic request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Topic);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.TOPIC_STRING);

        MqTopic topic = new MqTopic("", input.getTopicString());

        Order order = new Order(OrderType.MQ, OrderOperation.START, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Starting topic " + topic + " on " + input.getQueueManagerUri(), "mq"));
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

        try {
            Optional<MqTopic> mqTopic = findTopic(queueManager, topic.getTopicString());
            if (mqTopic.isPresent()) {
                mq.enableTopic(queueManager, mqTopic.get());
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + mqTopic.get().getName() + " started", "mq", StatusLogLevel.success));
            } else {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + topic.getTopicString() + " do not exist", "mq", StatusLogLevel.warning));
            }
            result.add(topic);

            Collection<ResourceElement> foundTopic = findInFasitByTopicString(input);
            if (foundTopic.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Fasit", "Topic " + input.getTopicString() + " not found", "fasit", StatusLogLevel.warning));
            } else {
                for (ResourceElement resourceElement : foundTopic) {
                    fasitUpdateService.updateResource(resourceElement, LifeCycleStatusDO.STARTED, order);
                    result.add(resourceElement);
                }
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Topic start failed", e);
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic start failed: " + e.getMessage(), "mq", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    @PUT
    @Path("remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeTopic(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Stop mq topic request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Topic);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.TOPIC_STRING);

        MqTopic topic = new MqTopic("", input.getTopicString());
        Order order = new Order(OrderType.MQ, OrderOperation.DELETE, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Removing topic " + topic + " on " + input.getQueueManagerUri(), "mq"));
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

        try {
            Optional<MqTopic> mqTopic = findTopic(queueManager, topic.getTopicString());
            if (mqTopic.isPresent()) {
                mq.deleteTopic(queueManager, mqTopic.get());
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + mqTopic.get().getName() + " deleted", "mq", StatusLogLevel.success));
            } else {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic " + topic.getTopicString() + " do not exist", "mq", StatusLogLevel.warning));
            }
            result.add(topic);

            Collection<ResourceElement> foundTopic = findInFasitByTopicString(input);
            if (foundTopic.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Fasit", "Topic " + input.getTopicString() + " not found", "fasit", StatusLogLevel.warning));
            } else {
                for (ResourceElement resourceElement : foundTopic) {
                    fasitUpdateService.deleteResource(resourceElement.getId(), "deleted with basta order with id " + order.getId(), order);
                    result.add(resourceElement);
                }
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Topic delete failed", e);
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Topic delete failed: " + e.getMessage(), "mq", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    private Optional<MqTopic> findTopic(MqQueueManager queueManager, String topicString) {
        return mq.getTopics(queueManager).stream()
                .filter(topic -> topic.getTopicString().equals(topicString))
                .findFirst();
    }

    private boolean topicExists(MqQueueManager queueManager, String topicString) {
        return findTopic(queueManager, topicString).isPresent();
    }

    private Collection<ResourceElement> findInFasitByAlias(MqOrderInput input) {
        return fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, null, ResourceTypeDO.Topic, input.getAlias());  
    }

    private Collection<ResourceElement> findInFasitByTopicString(MqOrderInput input) {
        Collection<ResourceElement> resources = fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, null, ResourceTypeDO.Topic, null);
        return resources.stream()
                .filter(topic -> topic.getPropertyString("topicString").equals(input.getTopicString()))
                .collect(Collectors.toSet());
    }

    public static void validateInput(Map<String, String> request) {
        ValidationHelper.validateRequest("/validation/mqTopicSchema.json", request);
    }

}
