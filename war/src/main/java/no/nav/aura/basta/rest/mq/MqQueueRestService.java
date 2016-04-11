package no.nav.aura.basta.rest.mq;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author v137023
 *
 */
@Component
@Path("/v1/mq/order/queue")
@Transactional
public class MqQueueRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqQueueRestService.class);

    private OrderRepository orderRepository;
    private FasitRestClient fasit;
    private FasitUpdateService fasitUpdateService;
    private MqService mq;

    // for cglib
    protected MqQueueRestService() {
    }

    @Inject
    public MqQueueRestService(OrderRepository orderRepository, FasitRestClient fasit, FasitUpdateService fasitUpdateService, MqService mq) {
        this.orderRepository = orderRepository;
        this.fasit = fasit;
        this.fasitUpdateService = fasitUpdateService;
        this.mq = mq;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMqQueue(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Create mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        validateInput(request);

        MqQueue mqQueue = input.getQueue();

        Order order = new Order(OrderType.MQ, OrderOperation.CREATE, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Creating queue " + mqQueue + " on " + input.getQueueManagerUri(), "mq"));
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

        try {
            if (input.shouldCreateBQ()) {
                if (mq.queueExists(queueManager, mqQueue.getBackoutQueueName())) {
                    order.getStatusLogs().add(new OrderStatusLog("MQ", "Backout queue " + mqQueue.getBackoutQueueName() + " already exists", "mq", StatusLogLevel.warning));
                } else {
                    mq.createBackoutQueue(queueManager, mqQueue);
                    order.getStatusLogs().add(new OrderStatusLog("MQ", "Backout queue " + mqQueue.getBackoutQueueName() + " created", "mq", StatusLogLevel.success));
                }
            }

            if (mq.queueExists(queueManager, mqQueue.getName())) {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue " + mqQueue.getName() + " already exists", "mq", StatusLogLevel.warning));
            } else {
                mq.createQueue(queueManager, mqQueue);
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue " + mqQueue.getName() + " created", "mq", StatusLogLevel.success));
            }

            if (mq.queueExists(queueManager, mqQueue.getAlias())) {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Alias " + mqQueue.getAlias() + " already exists", "mq", StatusLogLevel.warning));
            } else {
                mq.createAlias(queueManager, mqQueue);
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Alias " + mqQueue.getAlias() + " created", "mq", StatusLogLevel.success));
            }
            result.add(mqQueue);

            Optional<ResourceElement> queue = findQueueInFasit(input);
            if (queue.isPresent()) {
                order.getStatusLogs().add(new OrderStatusLog("Fasit", "Queue " + input.getAlias() + " already exists", "fasit", StatusLogLevel.warning));
            }

            ResourceElement fasitQueue = queue.orElseGet(() -> new ResourceElement(ResourceTypeDO.Queue, input.getAlias()));
            fasitQueue.setEnvironmentClass(input.getEnvironmentClass().name());
            fasitQueue.setEnvironmentName(input.getEnvironmentName());
            fasitQueue.addProperty(new PropertyElement("queueName", mqQueue.getAlias()));
            fasitQueue.addProperty(new PropertyElement("queueManager", input.getQueueManagerUri().toString()));
            Optional<ResourceElement> createdResource = fasitUpdateService.createResource(fasitQueue, order);
            if (createdResource.isPresent()) {
                result.add(createdResource.get());
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Queue creation failed", e);
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue creation failed: " + e.getMessage(), "mq", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    @PUT
    @Path("validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Boolean> validate(Map<String, String> request) {
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        validateInput(request);
        HashMap<String, Boolean> result = new HashMap<>();
        result.putAll(existsInMQ(input));
        result.put("fasit", findQueueInFasit(input).isPresent());
        return result;
    }

    @Path("stop")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopQueue(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Stop mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_QUEUE_NAME);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        Optional<MqQueue> queue = mq.getQueue(queueManager, input.getMqQueueName());

        Collection<ResourceElement> fasitResources;
        Order order = new Order(OrderType.MQ, OrderOperation.STOP, input);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Stopping  queue " + input.getMqQueueName() + " on " + input.getQueueManagerUri(), "mq"));
        order = orderRepository.save(order);

        fasitResources = findQueueInFasit(input, queue, order);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);

        try {
            if (queue.isPresent()) {
                MqQueue mqQueue = queue.get();
                mq.disableQueue(queueManager, mqQueue);
                order.getStatusLogs().add(new OrderStatusLog("MQ", mqQueue.getName() + " disabled", "mq", StatusLogLevel.success));
                result.add(mqQueue);

            }
            for (ResourceElement resource : fasitResources) {
                fasitUpdateService.updateResource(resource, LifeCycleStatusDO.STOPPED, order);
                result.add(resource);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Queue disabling failed", e);
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue disabling failed: " + e.getMessage(), "mq", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    @Path("start")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startQueue(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Start mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_QUEUE_NAME);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        Optional<MqQueue> queue = mq.getQueue(queueManager, input.getMqQueueName());

        Collection<ResourceElement> fasitResources;
        Order order = new Order(OrderType.MQ, OrderOperation.START, input);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Start queue " + input.getMqQueueName() + " on " + input.getQueueManagerUri(), "mq"));
        order = orderRepository.save(order);

        fasitResources = findQueueInFasit(input, queue, order);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);

        try {
            if (queue.isPresent()) {
                MqQueue mqQueue = queue.get();
                mq.enableQueue(queueManager, mqQueue);
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue " + mqQueue.getName() + " enabled", "mq", StatusLogLevel.success));

            }

            for (ResourceElement resource : fasitResources) {
                fasitUpdateService.updateResource(resource, LifeCycleStatusDO.STARTED, order);
                result.add(resource);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Queue enable failed", e);
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue enable failed: " + e.getMessage(), "mq", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    @Path("remove")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeQueue(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("remove mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_QUEUE_NAME);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        Optional<MqQueue> queue = mq.getQueue(queueManager, input.getMqQueueName());

        Collection<ResourceElement> fasitResources;
        Order order = new Order(OrderType.MQ, OrderOperation.DELETE, input);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Delete  queue " + input.getMqQueueName() + " on " + input.getQueueManagerUri(), "mq"));
        order = orderRepository.save(order);

        fasitResources = findQueueInFasit(input, queue, order);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);

        try {
            if (queue.isPresent()) {
                MqQueue mqQueue = queue.get();
                if (mq.deleteQueue(queueManager, mqQueue.getAlias())) {
                    order.getStatusLogs().add(new OrderStatusLog("MQ", mqQueue.getAlias() + " deleted", "mq", StatusLogLevel.success));
                }
                if (mq.deleteQueue(queueManager, mqQueue.getName())) {
                    order.getStatusLogs().add(new OrderStatusLog("MQ", mqQueue.getName() + " deleted", "mq", StatusLogLevel.success));
                }
                if (mq.deleteQueue(queueManager, mqQueue.getBackoutQueueName())) {
                    order.getStatusLogs().add(new OrderStatusLog("MQ", mqQueue.getBackoutQueueName() + " deleted", "mq", StatusLogLevel.success));
                }
            }
            for (ResourceElement resource : fasitResources) {
                if (fasitUpdateService.deleteResource(resource.getId(), "deleted with basta order with id " + order.getId(), order)) {
                    result.add(resource);
                }
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Queue deletion failed", e);
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue deletion failed: " + e.getMessage(), "mq", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    private Collection<ResourceElement> findQueueInFasit(MqOrderInput input, Optional<MqQueue> queue, Order order) {
        Collection<ResourceElement> fasitResources;
        if (queue.isPresent()) {
            MqQueue mqQueue = queue.get();
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Found queue " + mqQueue, "mq"));
            fasitResources = findFasitResourcesWithQueueName(input.getEnvironmentClass(), mqQueue.getAlias(), mqQueue.getName());
        } else {
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue " + input.getMqQueueName() + " not found", "mq"));
            fasitResources = findFasitResourcesWithQueueName(input.getEnvironmentClass(), input.getMqQueueName());
        }
        if (fasitResources.isEmpty()) {
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Found no queues with queuename" + input.getMqQueueName(), "fasit"));
        }

        return fasitResources;
    }

    private Collection<ResourceElement> findFasitResourcesWithQueueName(EnvironmentClass environmentClass, String... queueNames) {
        List<String> queueNameList = Arrays.asList(queueNames);
        Collection<ResourceElement> resources = fasit.findResources(EnvClass.valueOf(environmentClass.name()), null, null, null, ResourceTypeDO.Queue, null);
        return resources.stream()
                .filter(resource -> queueNameList.contains(resource.getPropertyString("queueName")))
                .collect(Collectors.toList());
    }

    private Map<String, Boolean> existsInMQ(MqOrderInput input) {
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        HashMap<String, Boolean> result = new HashMap<>();
        MqQueue queue = input.getQueue();
        result.put("local_queue", mq.queueExists(queueManager, queue.getName()));
        result.put("backout_queue", mq.queueExists(queueManager, queue.getBackoutQueueName()));
        result.put("alias_queue", mq.queueExists(queueManager, queue.getAlias()));

        return result;
    }

    private Optional<ResourceElement> findQueueInFasit(MqOrderInput input) {
        Collection<ResourceElement> resources = fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, input.getAppliation(), ResourceTypeDO.Queue,
                input.getAlias());
        return resources.stream().findFirst();
    }

    public static void validateInput(Map<String, String> request) {
        ValidationHelper.validateRequest("/validation/mqQueueSchema.json", request);
    }

}
