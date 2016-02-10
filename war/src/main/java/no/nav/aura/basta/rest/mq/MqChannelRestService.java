package no.nav.aura.basta.rest.mq;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
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
import no.nav.aura.basta.backend.mq.MqChannel;
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
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.ValidationHelper;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.rest.ResourceElement;

@Component
@Path("/orders/mq/channel")
@Transactional
public class MqChannelRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqChannelRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasit;

    @Inject
    private MqService mq;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMqChannel(Map<String, String> request, @Context UriInfo uriInfo) {
        logger.info("Create mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Channel);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        validateInput(request);

        MqChannel channel = new MqChannel(input.getMqChannelName(), input.getUserName(), input.getDescription());

        Order order = new Order(OrderType.MQ, OrderOperation.CREATE, input);
        MqOrderResult result = order.getResultAs(MqOrderResult.class);
        result.add(channel);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());

        if (mq.exists(queueManager, channel)) {
            throw new BadRequestException("Channel with name " + channel.getName() + " allready exist in " + queueManager);
        }
        // TODO sjekke i AD

        mq.create(queueManager, channel);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Created channel " + channel.getName() + " on " + queueManager, "mq"));
        mq.setChannelAuthorization(queueManager, channel);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Setting autentication on channel" + channel.getName() + " for user " + channel.getUserName(), "mq"));

        // TODO Lagre i fasit
        
       ResourceElement fasitResource= new ResourceElement();
       fasitResource.setAlias(input.getAlias());
       fasitResource.setId(100l);
        result.add(fasitResource);
        
        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    public static void validateInput(Map<String, String> request) {
        ValidationHelper.validateRequest("/validation/mqChannelSchema.json", request);
    }

}
