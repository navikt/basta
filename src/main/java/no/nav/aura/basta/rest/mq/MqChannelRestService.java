package no.nav.aura.basta.rest.mq;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.deprecated.payload.LifeCycleStatus;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ScopePayload;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Path("/v1/mq/order/channel")
@Transactional
public class MqChannelRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqChannelRestService.class);

    private FasitUpdateService fasitUpdateService;
    private OrderRepository orderRepository;
    private RestClient fasitClient;
    private MqService mq;

    public MqChannelRestService() {
    }

    @Inject
    public MqChannelRestService(OrderRepository orderRepository, RestClient restClient, FasitUpdateService fasitUpdateService, MqService mq) {
        super();
        this.fasitUpdateService = fasitUpdateService;

        this.orderRepository = orderRepository;
        this.fasitClient = restClient;
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
            MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());

            if (channelExists(queueManager, channel)) {
                order.addStatuslogWarning("Channel " + channel.getName() + " already exists in Mq");
            } else {
                if (input.isTlsEnabled()) {
                    channel.setTlsEnabled(true);
                }
                mq.createChannel(queueManager, channel);
                order.addStatuslogInfo("Created channel " + channel.getName() + " on " + queueManager);
                order.addStatuslogInfo("Setting authentication on channel" + channel.getName() + " for user " + channel.getUserName());
                result.add(channel);
            }

            ResourcesListPayload foundChannel = findInFasitByAlias(input);
            if (!foundChannel.isEmpty()) {
                order.addStatuslogWarning("Channel " + input.getAlias() + " already exists in Fasit");
            } else {
                ResourcePayload fasitChannel = new ResourcePayload()
                        .withType(ResourceType.channel)
                        .withAlias(input.getAlias())
                        .withScope(new ScopePayload(input.getEnvironmentClass().toString())
                                .environment(input.getEnvironmentName()))
                        .withProperty("name", channel.getName())
                        .withProperty("queueManager", input.getQueueManagerUri().toString());
                Optional<String> fasitID = fasitUpdateService.createResource(fasitChannel, order);

                if (fasitID.isPresent()) {
                    result.add(fasitChannel);
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
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());
        Map<String, String> errorResult = new HashMap<>();
        if (channelExists(queueManager, input.getChannel())) {
            errorResult.put(MqOrderInput.MQ_CHANNEL_NAME, "Channel " + input.getMqChannelName() + " already exist in QueueManager");
        }
        if (!findInFasitByAlias(input).isEmpty()) {
            errorResult.put(MqOrderInput.ALIAS, "Alias " + input.getAlias() + " already exist in Fasit");
        }

        if (errorResult.isEmpty()) {
            return Response.ok(errorResult).build();
        }
        return Response.status(Status.CONFLICT).entity(errorResult).build();
    }

    private ResourcesListPayload findInFasitByAlias(MqOrderInput input) {
        final ScopePayload searchScope = new ScopePayload(input.getEnvironmentClass().toString())
                .environment(input.getEnvironmentName());
        return fasitClient.findFasitResources(ResourceType.channel, input.getAlias(), searchScope);

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
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());

        try {
            if (channelExists(queueManager, channel)) {
                mq.stopChannel(queueManager, channel);
                order.addStatuslogSuccess("Channel " + channel.getName() + " stopped");
            } else {
                order.addStatuslogWarning("Channel " + channel.getName() + " do not exist in MQ");
            }
            result.add(channel);

            ResourcesListPayload foundChannels = findInFasitByChannelName(input);
            if (foundChannels.isEmpty()) {
                order.addStatuslogWarning("Channel " + channel.getName() + " not found in Fasit");
            } else {
                for (ResourcePayload channelResource : foundChannels.getResources()) {
                    fasitUpdateService.setLifeCycleStatus(channelResource, LifeCycleStatus.STOPPED, order);
                    result.add(channelResource);
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
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());

        try {
            if (channelExists(queueManager, channel)) {
                mq.startChannel(queueManager, channel);
                order.addStatuslogSuccess("Channel " + channel.getName() + " stopped");
            } else {
                order.addStatuslogWarning("Channel " + channel.getName() + " do not exist in MQ");
            }
            result.add(channel);

            ResourcesListPayload foundChannels = findInFasitByChannelName(input);
            if (foundChannels.isEmpty()) {
                order.addStatuslogWarning("Channel " + channel.getName() + " not found in Fasit");
            } else {
                for (ResourcePayload channelResource : foundChannels.getResources()) {
                    fasitUpdateService.setLifeCycleStatus(channelResource, LifeCycleStatus.RUNNING, order);
                    result.add(channelResource);
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
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.ENVIRONMENT_NAME, MqOrderInput.QUEUE_MANAGER, MqOrderInput.MQ_CHANNEL_NAME);

        Order order = new Order(OrderType.MQ, OrderOperation.DELETE, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        MqChannel channel = input.getChannel();
        order.addStatuslogInfo("Deleting channel " + channel.getName() + " on " + input.getQueueManagerUri());
        order = orderRepository.save(order);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());

        try {
            if (channelExists(queueManager, channel)) {
                mq.deleteChannel(queueManager, channel);
                order.addStatuslogSuccess("Channel " + channel.getName() + " deleted in MQ");
            } else {
                order.addStatuslogWarning("Channel " + channel.getName() + " do not exist in MQ");
            }
            result.add(channel);

            ResourcesListPayload foundChannels = findInFasitByChannelName(input);
            if (foundChannels.isEmpty()) {
                order.addStatuslogWarning("Channel " + channel.getName() + " not found in Fasit");
            } else {

                for (ResourcePayload resource : foundChannels.getResources()) {
                    fasitUpdateService.deleteResource(resource.id, "deleted with basta order with id " + order.getId(), order);
                    result.add(resource);
                }
            }

            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Channel remove failed", e);
            order.addStatuslogError("Channel remove failed: " + e.getMessage());
            order.setStatus(OrderStatus.ERROR);
        }
        order = orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    private boolean channelExists(MqQueueManager queueManager, MqChannel channel) {
        Collection<String> channels = mq.findChannelNames(queueManager, channel.getName());
        return !channels.isEmpty();
    }

    private ResourcesListPayload findInFasitByChannelName(MqOrderInput input) {
        ScopePayload searchScope = new ScopePayload(input.getEnvironmentClass().toString())
                .environment(input.getEnvironmentName());
        ResourcesListPayload fasitResources = fasitClient.findFasitResources(ResourceType.channel, null, searchScope);

        return fasitResources.filter(resource -> resource.getProperty("name").equals(input.getMqChannelName()));
    }
}
