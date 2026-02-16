package no.nav.aura.basta.rest.mq;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.payload.LifeCycleStatus;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
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
import no.nav.aura.basta.domain.result.mq.MqOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.inject.Inject;
import java.net.URI;
import java.util.*;

import static java.util.stream.Collectors.*;

@Component
@RestController
@RequestMapping("/rest/v1/mq/order/queue")
@Transactional
public class MqQueueRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqQueueRestService.class);

    @Inject
    private OrderRepository orderRepository;
    @Inject
    private RestClient restClient;
    @Inject
    private FasitUpdateService fasitUpdateService;
    @Inject
    private MqService mq;

    // for cglib
    public MqQueueRestService() {
    }

//    @Inject
//    public MqQueueRestService(OrderRepository orderRepository, RestClient restClient, FasitUpdateService fasitUpdateService, MqService mq) {
//        this.orderRepository = orderRepository;
//        this.restClient = restClient;
//        this.fasitUpdateService = fasitUpdateService;
//        this.mq = mq;
//    }

    @PostMapping
    public ResponseEntity<?> createMqQueue(@RequestBody Map<String, String> request) {
        logger.info("Create mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        validateInput(request);

        MqQueue mqQueue = input.getQueue();

        Order order = new Order(OrderType.MQ, OrderOperation.CREATE, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        order.addStatuslog("Creating queue " + mqQueue + " on " + input.getQueueManagerUri(), StatusLogLevel.info);
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());

        try {
            if (input.shouldCreateBQ()) {
                if (mq.queueExists(queueManager, mqQueue.getBackoutQueueName())) {
                    order.addStatuslog("Backout queue " + mqQueue.getBackoutQueueName() + " already exists in MQ", StatusLogLevel.warning);
                } else {
                    mq.createBackoutQueue(queueManager, mqQueue);
                    order.addStatuslog("Backout queue " + mqQueue.getBackoutQueueName() + " created in MQ", StatusLogLevel.success);
                }
            }

            if (mq.queueExists(queueManager, mqQueue.getName())) {
                order.addStatuslog("Queue " + mqQueue.getName() + " already exists in MQ", StatusLogLevel.warning);
            } else {
                mq.createQueue(queueManager, mqQueue);
                order.addStatuslog("Queue " + mqQueue.getName() + " createdin MQ", StatusLogLevel.success);
            }

            if (mq.queueExists(queueManager, mqQueue.getAlias())) {
                order.addStatuslog("Alias " + mqQueue.getAlias() + " already existsin MQ", StatusLogLevel.warning);
            } else {
                mq.createAlias(queueManager, mqQueue);
                order.addStatuslog("Alias " + mqQueue.getAlias() + " createdin MQ", StatusLogLevel.success);
            }
            result.add(mqQueue);

            Optional<ResourcePayload> maybeFasitQueue = findQueueInFasit(input);
            if (maybeFasitQueue.isPresent()) {
                order.addStatuslogWarning("Queue " + input.getAlias() + " already exists in Fasit");
            }

            Map<String, String> properties = new HashMap<>();
            properties.put("queueName", mqQueue.getName());
            properties.put("queueManager", input.getQueueManagerUri().toString());
            
            ResourcePayload fasitQueue = maybeFasitQueue.orElseGet(() -> {
                ResourcePayload resource = new ResourcePayload(ResourceType.Queue, input.getAlias());
                ScopePayload scopePayload = new ScopePayload()
                        .environmentClass(input.getEnvironmentClass())
                        .environment(input.getEnvironmentName())
                        .application(input.getAppliation());
                resource.setScope(scopePayload);
                resource.setProperties(properties);
                return resource;
            });
            
            Optional<String> fasitId = fasitUpdateService.createResource(fasitQueue, order);
            logger.info("Fasit queue id: {}", fasitId.orElse("not created"));
            if (fasitId.isPresent()) {
            	fasitQueue.id = Long.valueOf(fasitId.get());
                result.add(fasitQueue);
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Queue creation failed", e);
            order.addStatuslog("Queue creation failed: " + e.getMessage(), StatusLogLevel.error);
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        
        URI location = getURILocation(order);
        return ResponseEntity.created(location).body(Map.of("id", order.getId()));
    }

    @PutMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validate(@RequestBody Map<String, String> request) {
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        validateInput(request);
        HashMap<String, Boolean> result = new HashMap<>();
        result.putAll(existsInMQ(input));
        result.put("fasit", findQueueInFasit(input).isPresent());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/stop")
    public ResponseEntity<?> stopQueue(@RequestBody Map<String, String> request) {
        logger.info("Stop mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        input.setMQObjectType(MQObjectType.Queue);
        validateOrder(request, input);

        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());
        Optional<MqQueue> queue = mq.getQueue(queueManager, input.getMqQueueName());

        List<ResourcePayload> fasitResources;
        Order order = new Order(OrderType.MQ, OrderOperation.STOP, input);
        order.addStatuslogInfo("Stopping queue " + input.getMqQueueName() + " on " + input.getQueueManagerUri());
        order = orderRepository.save(order);

        order.addStatuslogInfo(queue
                .map(mqQueue -> "Found queue " + mqQueue + " in MQ")
                .orElseGet(() -> "Queue " + input.getMqQueueName() + " not found in MQ"));

        fasitResources = findQueueInFasit(input, order);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        logger.info("Found {} queues in Fasit matching queue name {}", fasitResources.size(), input.getMqQueueName());
        try {
            if (queue.isPresent()) {
                MqQueue mqQueue = queue.get();
                mq.disableQueue(queueManager, mqQueue);
                order.addStatuslog(mqQueue.getName() + " disabled in MQ", StatusLogLevel.success);
                result.add(mqQueue);

            }
            for (ResourcePayload resource : fasitResources) {
                fasitUpdateService.setLifeCycleStatus(resource, LifeCycleStatus.STOPPED, order);
                result.add(resource);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Queue disabling failed", e);
            order.addStatuslog("Queue disabling failed: " + e.getMessage(), StatusLogLevel.error);
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        
        URI location = getURILocation(order);
        return ResponseEntity.created(location).body(Map.of("id", order.getId()));
    }

    @PutMapping("/start")
    public ResponseEntity<?> startQueue(@RequestBody Map<String, String> request) {
        logger.info("Start mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        validateOrder(request, input);

        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());
        Optional<MqQueue> queue = mq.getQueue(queueManager, input.getMqQueueName());

        List<ResourcePayload> fasitResources;
        Order order = new Order(OrderType.MQ, OrderOperation.START, input);
        order.addStatuslogInfo("Start queue " + input.getMqQueueName() + " on " + input.getQueueManagerUri());
        order = orderRepository.save(order);

        order.addStatuslogInfo(queue
                .map(mqQueue -> "Found queue " + mqQueue + " in MQ")
                .orElseGet(() -> "Queue " + input.getMqQueueName() + " not found in MQ"));

        fasitResources = findQueueInFasit(input, order);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);

        try {
            if (queue.isPresent()) {
                MqQueue mqQueue = queue.get();
                mq.enableQueue(queueManager, mqQueue);
                order.addStatuslog("Queue " + mqQueue.getName() + " enabled in MQ", StatusLogLevel.success);

            }

            for (ResourcePayload fasitQueue : fasitResources) {
                fasitUpdateService.setLifeCycleStatus(fasitQueue, LifeCycleStatus.RUNNING, order);
                result.add(fasitQueue);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Queue enable failed", e);
            order.addStatuslogError("Queue enable failed: " + e.getMessage());
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        
        URI location = getURILocation(order);
        return ResponseEntity.created(location).body(Map.of("id", order.getId()));
    }

    @PutMapping("/remove")
    public ResponseEntity<?> removeQueue(@RequestBody Map<String, String> request) {
        logger.info("Remove mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        validateOrder(request, input);

        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());
        Order order = new Order(OrderType.MQ, OrderOperation.DELETE, input);

        Optional<MqQueue> queue = mq.getQueue(queueManager, input.getMqQueueName());
        order.addStatuslogInfo("Delete queue " + input.getMqQueueName() + " on " + input.getQueueManagerUri());
        order = orderRepository.save(order);

        order.addStatuslogInfo(queue
                .map(mqQueue -> "Found queue " + mqQueue + " in MQ")
                .orElseGet(() -> "Queue " + input.getMqQueueName() + " not found in MQ"));

        List<ResourcePayload> fasitResources = findQueueInFasit(input, order);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);

        try {
            if (queue.isPresent()) {
                MqQueue mqQueue = queue.get();
                if (!mq.isQueueEmpty(queueManager, mqQueue.getName())) {
                    order.addStatuslogError("Queue is not empty and could not be deleted");
                    order.setStatus(OrderStatus.ERROR);
                } else {
                    logger.info("Deleting empty queue {}", mqQueue.getName());
                    if (mq.deleteQueue(queueManager, mqQueue.getAlias())) {
                        order.addStatuslog(mqQueue.getAlias() + " deleted in MQ", StatusLogLevel.success);
                    }
                    if (mq.deleteQueue(queueManager, mqQueue.getName())) {
                        order.addStatuslog(mqQueue.getName() + " deleted in MQ", StatusLogLevel.success);
                    }
                    if (mq.deleteQueue(queueManager, mqQueue.getBackoutQueueName())) {
                        order.addStatuslog(mqQueue.getBackoutQueueName() + " deleted in MQ", StatusLogLevel.success);
                    }

                    for (ResourcePayload resource : fasitResources) {
                        if (fasitUpdateService.deleteResource(resource.id, "deleted with basta order with id " + order.getId(), order)) {
                            result.add(resource);
                        }
                    }
                    order.setStatus(OrderStatus.SUCCESS);
                }
            }

        } catch (Exception e) {
            logger.error("Queue deletion failed", e);
            order.addStatuslogError("Queue deletion failed: " + e.getMessage());
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        
        URI location = getURILocation(order);

        return ResponseEntity.created(location).body(Map.of("id", order.getId()));
    }

    private void validateOrder(Map<String, String> request, MqOrderInput input) {
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_QUEUE_NAME);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
    }

    private List<ResourcePayload> findQueueInFasit(MqOrderInput input, Order order) {
        List<ResourcePayload> queuesMatchingName = findFasitResourcesWithQueueName(input.getEnvironmentClass(), input.getMqQueueName());

        if (queuesMatchingName.isEmpty()) {
            order.addStatuslogInfo("Found no queues with queuename " + input.getMqQueueName() + " in Fasit");
        }
        return queuesMatchingName;
    }

    private List<ResourcePayload> findFasitResourcesWithQueueName(EnvironmentClass environmentClass, String queueName) {
        return restClient.searchFasit(queueName, "resource", ResourcePayload.class)
                .stream()
                .filter(resource -> resource.type.equals(ResourceType.Queue) && resource.scope.environmentclass.equals(environmentClass.toString()))
                .collect(toList());
    }

    private Map<String, Boolean> existsInMQ(MqOrderInput input) {
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());
        HashMap<String, Boolean> result = new HashMap<>();
        MqQueue queue = input.getQueue();
        result.put("local_queue", mq.queueExists(queueManager, queue.getName()));
        result.put("backout_queue", mq.queueExists(queueManager, queue.getBackoutQueueName()));
        result.put("alias_queue", mq.queueExists(queueManager, queue.getAlias()));

        return result;
    }

    private Optional<ResourcePayload> findQueueInFasit(MqOrderInput input) {
        ScopePayload searchScope = new ScopePayload()
                .environmentClass(input.getEnvironmentClass())
                .environment(input.getEnvironmentName())
                .application(input.getAppliation());
        ResourcesListPayload fasitResources = restClient.findFasitResources(ResourceType.Queue, input.getAlias(), searchScope);
        if (fasitResources == null) {
            return Optional.empty();
        }
        return fasitResources.getResources().stream().findFirst();
    }

    public static void validateInput(Map<String, String> request) {
        ValidationHelper.validateRequest("/validation/mqQueueSchema.json", request);
    }

	private URI getURILocation(Order order) {
		try {
			URI location = ServletUriComponentsBuilder
	                .fromCurrentContextPath()
	                .path("/orders/{id}")
	                .buildAndExpand(order.getId())
	                .toUri();
			return location;
		} catch (IllegalStateException e) {
			// Fallback for unit tests where ServletRequestAttributes are not available
			logger.debug("ServletRequestAttributes not available, using fallback URI construction");
			return URI.create("/orders/" + order.getId());
		}
	}

	
	@ExceptionHandler(SecurityException.class)
	private ResponseEntity<?> handleSecurityException(SecurityException e) {
		logger.warn("Access denied: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", "Access denied", "message", e.getMessage()));
	}
	
}