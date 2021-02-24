package no.nav.aura.basta.rest.serviceuser;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.backend.serviceuser.*;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.serviceuser.GroupOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.serviceuser.GroupResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
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
import javax.ws.rs.core.UriInfo;
import java.util.Map;

@Component
@Path("/orders/adgroups")
@Transactional
public class AdGroupRestService {

    private static final Logger logger = LoggerFactory.getLogger(AdGroupRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private ActiveDirectory activeDirectory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response adCreateGroup(Map<String, String> map, @Context UriInfo uriInfo) throws RuntimeException {
        ValidationHelper.validateRequest("/validation/createGroupSchema.json", map);
        GroupOrderInput input = new GroupOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.Group, OrderOperation.CREATE, input);
        order.setExternalId("N/A");

        GroupAccount groupAccount = new GroupAccount(input.getEnvironmentClass(), input.getZone());
        groupAccount.setGroupUsage(input.getGroupUsage());
        groupAccount.setName(input.getApplication());

        MqServiceUserAccount userAccount = new MqServiceUserAccount(input.getEnvironmentClass(), input.getZone(), input.getApplication());
        MqServiceUserAccount mqServiceUserAccount = activeDirectory.createOrUpdate(userAccount);

        order.getStatusLogs().add(
                new OrderStatusLog("AD Group", "Creating new group for " + groupAccount.getName() + " in ad " + groupAccount.getGroupFqdn(), "group", StatusLogLevel.success));

        if (activeDirectory.createAdGroup(groupAccount, mqServiceUserAccount)) {
            logger.info("Created group {}", groupAccount.getName());
        }

        GroupResult result = order.getResultAs(GroupResult.class);
        result.add(groupAccount);

        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);
        logger.info("Created group order {} with input {}", order.getId(), map);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    @GET
    @Path("existInAD")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existInAD(@QueryParam("username") String username, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        ServiceUserAccount userAccount = new FasitServiceUserAccount(envClass, zone, username);
        return activeDirectory.userExists(userAccount);
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
