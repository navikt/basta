package no.nav.aura.basta.rest.mq;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

import org.jboss.resteasy.spi.BadRequestException;
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
import no.nav.aura.basta.util.JsonHelper;
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
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Creating queue " + mqQueue + " on " + input.getQueueManager(), "mq"));

        MqQueueManager queueManager = getQueueManager(fasit, input);

        boolean queueOk = false;
        try {
            if (mq.queueExists(queueManager, mqQueue.getName())) {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue " + mqQueue.getName() + " already exists", "mq", StatusLogLevel.warning));
            } else {
                mq.createQueue(queueManager, mqQueue);
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue " + mqQueue.getName() + " created", "mq", StatusLogLevel.success));
            }

            if (mq.queueExists(queueManager, mqQueue.getBoqName())) {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Backout queue " + mqQueue.getBoqName() + " already exists", "mq", StatusLogLevel.warning));
            } else {
                MqQueue backoutQueue = new MqQueue();
                backoutQueue.setName(mqQueue.getBoqName());
                backoutQueue.setDescription(mqQueue.getName() + " backout queue");
                backoutQueue.setMaxDepth(mqQueue.getMaxDepth());
                backoutQueue.setMaxSizeInBytes(mqQueue.getMaxSizeInBytes());
                mq.createQueue(queueManager, backoutQueue);
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Backout queue " + mqQueue.getBoqName() + " created", "mq", StatusLogLevel.success));
            }
            if (mq.queueExists(queueManager, mqQueue.getAlias())) {
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Alias " + mqQueue.getAlias() + " already exists", "mq", StatusLogLevel.warning));
            } else {
                mq.createAlias(queueManager, mqQueue);
                order.getStatusLogs().add(new OrderStatusLog("MQ", "Alias " + mqQueue.getAlias() + " created", "mq", StatusLogLevel.success));
            }
            queueOk = true;
        } catch (Exception e) {
            logger.error("Queue creation failed", e);
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue creation failed: " + e.getMessage(), "mq", StatusLogLevel.error));
        }
        result.add(mqQueue);

        if (queueOk) {
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Registering queue in Fasit", "fasit"));
            
           Optional<ResourceElement> queue = findInFasit(input);
            if (!queue.isPresent()) {
                ResourceElement fasitQueue = new ResourceElement(ResourceTypeDO.Queue, input.getAlias());
                fasitQueue.addProperty(new PropertyElement("queueName", mqQueue.getAlias()));
                Optional<ResourceElement> createdResource = fasitUpdateService.createResource(fasitQueue, order);
                if (createdResource.isPresent()) {
                    result.add(createdResource.get());
                }
            } else {
                order.getStatusLogs().add(new OrderStatusLog("Fasit", "Queue " + input.getAlias() + " already exists", "fasit", StatusLogLevel.warning));
            }
        }

        order.setStatus(queueOk ? OrderStatus.SUCCESS : OrderStatus.FAILURE);
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }
    @PUT
    @Path("validation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Boolean> exists(Map<String, String> request){
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        validateInput(request);
        HashMap<String, Boolean> result = new HashMap<>();
        result.putAll(existsInMQ(input));
        result.put("fasit", findInFasit(input).isPresent());
        return result;
    }
    
    private Map<String, Boolean> existsInMQ( MqOrderInput input) {
        MqQueueManager queueManager = getQueueManager(fasit, input);
        HashMap<String, Boolean> result = new HashMap<>();
        MqQueue queue= input.getQueue();
        result.put("local_queue", mq.queueExists(queueManager, queue.getName()));
        result.put("backout_queue", mq.queueExists(queueManager, queue.getBoqName()));
        result.put("alias_queue", mq.queueExists(queueManager, queue.getAlias()));
        
        return result;
    }

    private Optional<ResourceElement> findInFasit(MqOrderInput input) {
        Collection<ResourceElement> resources = fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, input.getAppliation(), ResourceTypeDO.Queue,
                input.getAlias());
        return resources.stream().findFirst();
    }

    protected static MqQueueManager getQueueManager(FasitRestClient fasit, MqOrderInput input) {
        Collection<ResourceElement> resources = fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, input.getAppliation(), ResourceTypeDO.QueueManager,
                input.getQueueManager());
        if (resources.isEmpty()) {
            throw new BadRequestException("Queue manager " + input.getQueueManager() + " not found in environment " + input.getEnvironmentName());
        }
        if (resources.size() > 1) {
            throw new BadRequestException("Found more than one Queue manager  " + input.getQueueManager() + " in fasit for environment " + input.getEnvironmentName());
        }
        ResourceElement fasitQueueManager = resources.iterator().next();
        return new MqQueueManager(fasitQueueManager.getPropertyString("hostname"), Integer.parseInt(fasitQueueManager.getPropertyString("port")), fasitQueueManager.getPropertyString("name"),
                input.getEnvironmentClass());
    }

    public static void validateInput(Map<String, String> request) {
        JsonHelper.validateRequest("/validation/mqQueueSchema.json", request);
    }

}