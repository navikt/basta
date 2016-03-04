package no.nav.aura.basta.rest.mq;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
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
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

@Component
@Path("/orders/mq/queue")
@Transactional
public class MqQueueRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqQueueRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasit;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @Inject
    private MqService mq;

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
    @Path("validation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Boolean> exists(Map<String, String> request) {
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        validateInput(request);
        HashMap<String, Boolean> result = new HashMap<>();
        result.putAll(existsInMQ(input));
        result.put("fasit", findQueueInFasit(input).isPresent());
        return result;
    }

    @GET
    @Path("clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getClusters(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        HashMap<String, String> request = new HashMap<>();
        for (String key : queryParameters.keySet()) {
            request.put(key, queryParameters.getFirst(key));
        }
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER);

        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        return mq.getClusterNames(queueManager);
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
