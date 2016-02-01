package no.nav.aura.basta.rest.mq;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
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
import no.nav.aura.basta.backend.mq.MqAdminUser;
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


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMqQueue(Map<String, String> request, @Context UriInfo uriInfo) {
    	logger.info("Create mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        String mqName = input.getMqName();
    	if(!isValidMqName(mqName)) {
    		throw new IllegalArgumentException("Invalid mqName " + mqName);
    	}

    	Order order = new Order(OrderType.MQ, OrderOperation.CREATE, input);
        order.setExternalId("N/A");
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Creating queue "+mqName+" on "+input.getQueueManager(), "mq"));
             
        MqQueueManager queueManager = getQueueManager(input);
        MqAdminUser mqAdminUser = getMqAdminUser(input.getEnvironmentClass()); 
        MqQueue mqQueue = new MqQueue(mqName, input.getMaxMessageSize(), input.getQueueDepth(), input.getDescription());

        boolean queueOk = false;
        try(MqService mq = new MqService(queueManager, mqAdminUser)) {
        	MqQueue existingQueue = mq.getQueue(mqName);
        	if(existingQueue != null) {
        		order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue "+mqName+" already exists", "mq", StatusLogLevel.warning));
        	} else {
        		mq.createQueue(mqQueue);
        		order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue "+mqName+" created", "mq", StatusLogLevel.success));
        	}
        	if(mqQueue.getBoqName() != null) {
        		MqQueue existingBoqQueue = mq.getQueue(mqQueue.getBoqName());
        		if(existingBoqQueue != null) {
        			order.getStatusLogs().add(new OrderStatusLog("MQ", "Backout queue "+mqQueue.getBoqName()+" already exists", "mq", StatusLogLevel.warning));
        		} else {
        			MqQueue backoutQueue = new MqQueue();
        			backoutQueue.setName(mqQueue.getBoqName());
        			backoutQueue.setDescription(mqQueue.getName()+" backout queue");
        			backoutQueue.setMaxDepth(mqQueue.getMaxDepth());
        			backoutQueue.setMaxSizeInBytes(mqQueue.getMaxSizeInBytes());
        			mq.createQueue(backoutQueue);
        			order.getStatusLogs().add(new OrderStatusLog("MQ", "Backout queue "+mqQueue.getBoqName()+" created", "mq", StatusLogLevel.success));
        		}
        	}
        	if(mqQueue.getAlias() != null) {
        		if(mq.exists(mqQueue.getAlias())) {
        			order.getStatusLogs().add(new OrderStatusLog("MQ", "Alias "+mqQueue.getAlias()+" already exists", "mq", StatusLogLevel.warning));
        		} else {
        			mq.createAlias(mqQueue);
        			order.getStatusLogs().add(new OrderStatusLog("MQ", "Alias "+mqQueue.getAlias()+" created", "mq", StatusLogLevel.success));
        		}
        	}
        	queueOk = true;
        } catch(Exception e) {
        	logger.error("Queue creation failed", e);
        	order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue creation failed: "+e.getMessage(), "mq", StatusLogLevel.error));
        }
        
        if(queueOk) {
        	order.getStatusLogs().add(new OrderStatusLog("Fasit", "Registering queue in Fasit", "fasit"));
        	Collection<ResourceElement> resources = fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, input.getAppliation(), ResourceTypeDO.Queue, input.getAlias());
        	if (resources == null || resources.size() == 0) {
        		ResourceElement fasitQueue = new ResourceElement(ResourceTypeDO.Queue, input.getAlias());
        		fasitQueue.addProperty(new PropertyElement("queueName", mqQueue.getAlias()));
        		fasitUpdateService.createResource(fasitQueue, order);
        	} else {
        		order.getStatusLogs().add(new OrderStatusLog("Fasit", "Queue "+input.getAlias()+" already exists", "fasit", StatusLogLevel.warning));
        	}
        }
        
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        result.put("alias", input.getAlias());
        result.put("queueName", mqQueue.getName());
        result.put("queueAlias", mqQueue.getAlias());
        result.put("backoutQueue", mqQueue.getBoqName());
        result.put("queueManager", input.getQueueManager());
        
        order.setStatus(queueOk ? OrderStatus.SUCCESS : OrderStatus.FAILURE);
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }
    
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMqQueue(Map<String, String> request, @Context UriInfo uriInfo) {
    	logger.info("Delete mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        String mqName = input.getMqName();
    	if(!isValidMqName(mqName)) {
    		throw new IllegalArgumentException("Invalid mqName " + mqName);
    	}

    	Order order = new Order(OrderType.MQ, OrderOperation.DELETE, input);
        order.setExternalId("N/A");
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Deleting queue "+mqName+" on "+input.getQueueManager(), "mq"));
             
        MqQueueManager queueManager = getQueueManager(input);
        MqAdminUser mqAdminUser = getMqAdminUser(input.getEnvironmentClass()); 
        MqQueue mqQueue = new MqQueue(mqName, 0, 0, null);

        boolean deleteOk = false;
        try(MqService mq = new MqService(queueManager, mqAdminUser)) {
        	if(!mq.exists(mqQueue.getName())) {
        		order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue "+mqName+" not found", "mq", StatusLogLevel.warning));
        	} else {
        		mq.delete(mqQueue);
        		order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue deleted", "mq", StatusLogLevel.success));
        	}
        	deleteOk = true;
        } catch(Exception e) {
        	logger.error("Queue deletion failed", e);
        	order.getStatusLogs().add(new OrderStatusLog("MQ", "Queue deletion failed: "+e.getMessage(), "mq", StatusLogLevel.error));
        }

        // TODO: fasit?
        
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        result.put("alias", input.getAlias());
        result.put("queueName", mqQueue.getName());
        result.put("queueAlias", mqQueue.getAlias());
        result.put("backoutQueue", mqQueue.getBoqName());
        result.put("queueManager", input.getQueueManager());

        order.setStatus(deleteOk ? OrderStatus.SUCCESS : OrderStatus.FAILURE);
        order = orderRepository.save(order);
        
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }
    
    private MqAdminUser getMqAdminUser(EnvironmentClass envClass) {
    	String usernameProperty = "mqadmin."+envClass.name()+".username";
    	String username = System.getProperty(usernameProperty);
    	if(username == null) throw new IllegalArgumentException("Environment property not defined: " + usernameProperty);

    	String passwordProperty = "mqadmin."+envClass.name()+".password";
    	String password = System.getProperty(passwordProperty);
    	if(password == null) throw new IllegalArgumentException("Environment property not defined: " + passwordProperty);

    	String channelProperty = "mqadmin."+envClass.name()+".channel";
    	String channel = System.getProperty(channelProperty);
    	if(channel == null) throw new IllegalArgumentException("Environment property not defined: " + channelProperty);

    	return new MqAdminUser(username, password, channel);
    }

    private MqQueueManager getQueueManager(MqOrderInput input) {
    	Collection<ResourceElement> resources = 
        	fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, input.getAppliation(), ResourceTypeDO.QueueManager, input.getQueueManager());
        if (resources == null || resources.size() != 1) {
        	throw new IllegalArgumentException("Queue manager "+input.getQueueManager()+" not found");
        }
        ResourceElement fasitQueueManager = resources.iterator().next();
        return new MqQueueManager(fasitQueueManager.getPropertyString("hostname"), Integer.parseInt(fasitQueueManager.getPropertyString("port")),  fasitQueueManager.getPropertyString("name"));
    }
    
    private boolean isValidMqName(String mqName) {
    	return mqName != null && mqName.matches("^[A-Z0-9][A-Z0-9._]{1,42}[A-Z0-9]$");
    }

}
