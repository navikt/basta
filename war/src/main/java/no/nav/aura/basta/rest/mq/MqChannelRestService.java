package no.nav.aura.basta.rest.mq;

import java.util.Collection;
import java.util.HashMap;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.mq.MqChannel;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.mq.MQObjectType;
import no.nav.aura.basta.domain.input.mq.MqOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.mq.MqOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.ValidationHelper;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.LifeCycleStatusDO;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

@Component
@Path("/v1/mq/order/channel")
@Transactional
public class MqChannelRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqChannelRestService.class);

    private FasitUpdateService fasitUpdateService;
    private OrderRepository orderRepository;
    private FasitRestClient fasit;
    private MqService mq;

    protected MqChannelRestService() {
    }

    @Inject
    public MqChannelRestService(FasitUpdateService fasitUpdateService, OrderRepository orderRepository, FasitRestClient fasit, MqService mq) {
        super();
        this.fasitUpdateService = fasitUpdateService;
        this.orderRepository = orderRepository;
        this.fasit = fasit;
        this.mq = mq;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMqChannel(Map<String, String> request, @Context UriInfo uriInfo) {
        validateInput(request);
        logger.info("Create mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Channel);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        MqChannel channel = input.getChannel();

        Order order = new Order(OrderType.MQ, OrderOperation.CREATE, input);
        try {
            MqOrderResult result = order.getResultAs(MqOrderResult.class);
            MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

            if (mq.channelExists(queueManager, channel)) {
                order.addStatuslogWarning("Channel " + channel.getName() + " already exists in Mq");
            } else {
                mq.createChannel(queueManager, channel);
                order.addStatuslogInfo("Created channel " + channel.getName() + " on " + queueManager);
                order.addStatuslogInfo("Setting authentication on channel" + channel.getName() + " for user " + channel.getUserName());
                result.add(channel);
            }

            Collection<ResourceElement> foundChannel = findInFasitByAlias(input);
            if (!foundChannel.isEmpty()) {
                order.addStatuslogWarning("Channel " + input.getAlias() + " already exists in Fasit");
            } else {
                ResourceElement fasitChannel = new ResourceElement(ResourceTypeDO.Channel, input.getAlias());
                fasitChannel.setEnvironmentClass(input.getEnvironmentClass().name());
                fasitChannel.setEnvironmentName(input.getEnvironmentName());
                fasitChannel.addProperty(new PropertyElement("name", channel.getName()));
                fasitChannel.addProperty(new PropertyElement("queueManager", input.getQueueManagerUri().toString()));
                Optional<ResourceElement> createdResource = fasitUpdateService.createResource(fasitChannel, order);
                if (createdResource.isPresent()) {
                    result.add(createdResource.get());
                }
            }
        } catch (Exception e) {
            logger.error("Channel creation failed", e);
            order.addStatuslogError("Channel creation failed: " + e.getMessage());
            order.setStatus(OrderStatus.ERROR);
        }
        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    public static void validateInput(Map<String, String> request) {
        ValidationHelper.validateRequest("/validation/mqChannelSchema.json", request);
    }

    @PUT
    @Path("validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateChannel(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Validate Channel request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Channel);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        validateInput(request);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        Map<String, String> errorResult = new HashMap<>();
        if (mq.channelExists(queueManager, input.getChannel())) {
            errorResult.put(MqOrderInput.MQ_CHANNEL_NAME, "Channel " + input.getMqChannelName() + " allready exist in QueueManager");
        }
        if (!findInFasitByAlias(input).isEmpty()) {
            errorResult.put(MqOrderInput.ALIAS, "Alias " + input.getAlias() + " allready exist in Fasit");
        }

        if (errorResult.isEmpty()) {
            return Response.ok(errorResult).build();
        }
        return Response.status(Status.CONFLICT).entity(errorResult).build();
    }

    private Collection<ResourceElement> findInFasitByAlias(MqOrderInput input) {
        return fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, null, ResourceTypeDO.Channel, input.getAlias());
    }

    @PUT
    @Path("stop")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopChannel(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Stop mq channel request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Channel);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_CHANNEL_NAME);

        Order order = new Order(OrderType.MQ, OrderOperation.STOP, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        MqChannel channel = input.getChannel();
        order.addStatuslogInfo("Stopping channel " + channel.getName() + " on " + input.getQueueManagerUri());
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

        try {
            if (mq.channelExists(queueManager, channel)) {
                mq.stopChannel(queueManager, channel);
                order.addStatuslogSuccess("Channel " + channel.getName() + " stopped");
            } else {
                order.addStatuslogWarning("Channel " + channel.getName() + " do not exist in MQ");
            }
            result.add(channel);

            Collection<ResourceElement> foundChannels = findInFasitByChannelName(input);
            if (foundChannels.isEmpty()) {
                order.addStatuslogWarning("Channel " + channel.getName() + " not found in Fasit");
            } else {
                for (ResourceElement resourceElement : foundChannels) {
                    fasitUpdateService.updateResource(resourceElement, LifeCycleStatusDO.STOPPED, order);
                    result.add(resourceElement);
                }
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Channel stop failed", e);
            order.addStatuslogError("Channel stop failed: " + e.getMessage());
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
    public Response startChannel(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("start mq channel request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Channel);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_CHANNEL_NAME);

        Order order = new Order(OrderType.MQ, OrderOperation.START, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        MqChannel channel = input.getChannel();
        order.addStatuslogInfo("Starting channel " + channel.getName() + " on " + input.getQueueManagerUri());
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

        try {
            if (mq.channelExists(queueManager, channel)) {
                mq.startChannel(queueManager, channel);
                order.addStatuslogSuccess("Channel " + channel.getName() + " stopped");
            } else {
                order.addStatuslogWarning("Channel " + channel.getName() + " do not exist in MQ");
            }
            result.add(channel);

            Collection<ResourceElement> foundChannels = findInFasitByChannelName(input);
            if (foundChannels.isEmpty()) {
                order.addStatuslogWarning("Channel " + channel.getName() + " not found in Fasit");
            } else {
                for (ResourceElement resourceElement : foundChannels) {
                    fasitUpdateService.updateResource(resourceElement, LifeCycleStatusDO.STARTED, order);
                    result.add(resourceElement);
                }
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Channel stop failed", e);
            order.addStatuslogError("Channel start failed: " + e.getMessage());
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
    public Response removeChannel(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("remove mq channel request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Channel);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_CHANNEL_NAME);

        Order order = new Order(OrderType.MQ, OrderOperation.DELETE, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        MqChannel channel = input.getChannel();
        order.addStatuslogInfo("Deleting channel " + channel.getName() + " on " + input.getQueueManagerUri());
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

        try {
            if (mq.channelExists(queueManager, channel)) {
                mq.deleteChannel(queueManager, channel);
                order.addStatuslogSuccess("Channel " + channel.getName() + " deleted in MQ");
            } else {
                order.addStatuslogWarning("Channel " + channel.getName() + " do not exist in MQ");
            }
            result.add(channel);

            Collection<ResourceElement> foundChannels = findInFasitByChannelName(input);
            if (foundChannels.isEmpty()) {
                order.addStatuslogWarning("Channel " + channel.getName() + " not found in Fasit");
            } else {
                for (ResourceElement resourceElement : foundChannels) {
                    fasitUpdateService.deleteResource(resourceElement.getId(), "deleted with basta order with id " + order.getId(), order);
                    result.add(resourceElement);
                }
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Channel stop failed", e);
            order.addStatuslogError("Channel start failed: " + e.getMessage());
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    private Collection<ResourceElement> findInFasitByChannelName(MqOrderInput input) {
        Collection<ResourceElement> resources = fasit.findResources(EnvClass.valueOf(input.getEnvironmentClass().name()), input.getEnvironmentName(), null, null, ResourceTypeDO.Channel, null);
        return resources.stream()
                .filter(resource -> resource.getPropertyString("channelName").equals(input.getMqChannelName()))
                .collect(Collectors.toSet());
    }

}
