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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.msg.client.wmq.compat.base.internal.MQManagedConnectionJ11;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.mq.MqAdminUser;
import no.nav.aura.basta.backend.mq.MqChannel;
import no.nav.aura.basta.backend.mq.MqQueue;
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
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.JsonHelper;
import no.nav.aura.envconfig.client.FasitRestClient;

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
    private  MqService mq;
    
    

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMqChannel(Map<String, String> request, @Context UriInfo uriInfo) {
    	logger.info("Create mq queue request with input {}", request);
        MqOrderInput input = new MqOrderInput(request, MQObjectType.Channel);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        validateInput(request);

        MqChannel mqChannel = new MqChannel(input.getMqChannelName(), input.getUserName(),input.getDescription());

    	Order order = new Order(OrderType.MQ, OrderOperation.CREATE, input);
        order.getStatusLogs().add(new OrderStatusLog("MQ", "Creating channel "+mqChannel.getName()+" on "+input.getQueueManager(), "mq"));
      

       
        
//        order.setStatus(queueOk ? OrderStatus.SUCCESS : OrderStatus.FAILURE);
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }



    public static void validateInput(Map<String, String> request) {
        JsonHelper.validateRequest("/validation/mqChannelSchema.json", request);
    }
   
    
   

}