package no.nav.aura.basta.rest.mq;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
import no.nav.aura.envconfig.client.rest.ResourceElement;

@Component
@Path("/v1/mq/order")
@Transactional
public class MqOperationsRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqOperationsRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasit;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @Inject
    private MqService mq;

    @Path("queue/stop")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopQueue(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Stop mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_QUEUE_NAME);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        Optional<MqQueue> queue = mq.getQueue(queueManager, input.getMqName());

        Collection<ResourceElement> fasitResources;
        Order order = new Order(OrderType.MQ, OrderOperation.STOP, input);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Stopping  queue " + input.getMqName() + " on " + input.getQueueManagerUri(), "mq"));
        order = orderRepository.save(order);

        fasitResources = findQueueInFasit(input, queue, order);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);

        try {
            if (queue.isPresent()) {
                MqQueue mqQueue = queue.get();
                mq.disableQueue(queueManager, mqQueue);
                order.getStatusLogs().add(new OrderStatusLog("MQ", mqQueue.getName() + " disabled", "mq", StatusLogLevel.success));

            }
            for (ResourceElement resource : fasitResources) {
                fasitUpdateService.updateResource(order, resource, LifeCycleStatusDO.STOPPED);
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

    @Path("queue/start")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startQueue(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Start mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_QUEUE_NAME);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        Optional<MqQueue> queue = mq.getQueue(queueManager, input.getMqName());

        Collection<ResourceElement> fasitResources;
        Order order = new Order(OrderType.MQ, OrderOperation.START, input);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Start queue " + input.getMqName() + " on " + input.getQueueManagerUri(), "mq"));
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
                fasitUpdateService.updateResource(order, resource, LifeCycleStatusDO.STARTED);
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

    private Collection<ResourceElement> findQueueInFasit(MqOrderInput input, Optional<MqQueue> queue, Order order) {
        Collection<ResourceElement> fasitResources;
        if (queue.isPresent()) {
            MqQueue mqQueue = queue.get();
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Found queue " + mqQueue, "mq"));
            fasitResources = findFasitResourcesWithQueueName(input.getEnvironmentClass(), mqQueue.getAlias(), mqQueue.getName());
        } else {
            order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue " + input.getMqName() + " not found", "mq"));
            fasitResources = findFasitResourcesWithQueueName(input.getEnvironmentClass(), input.getMqName());
        }
        if (fasitResources.isEmpty()) {
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Found no queues with queuename" + input.getMqName(), "fasit"));
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

}
