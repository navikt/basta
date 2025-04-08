package no.nav.aura.basta.rest.adgroups;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.serviceuser.*;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.serviceuser.GroupOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.serviceuser.GroupResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Map;

@Component
@Path("/operation/adgroup")
@Transactional
public class AdGroupOperationRestService {

    private static final Logger logger = LoggerFactory.getLogger(AdGroupOperationRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private ActiveDirectory activeDirectory;

    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAdGroup(Map<String, String> map, @Context UriInfo uriInfo) {
        GroupOrderInput input = new GroupOrderInput(map);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.AdGroup, OrderOperation.DELETE, input);
        order.setExternalId("N/A");

        GroupAccount groupAccount = new GroupAccount(input.getEnvironmentClass(), input.getZone(), input.getApplication());
        groupAccount.setGroupUsage(input.getGroupUsage());
        groupAccount.setName(input.getApplication());
        ServiceUserAccount userAccount = new GroupServiceUserAccount(input.getEnvironmentClass(), input.getZone(), input.getApplication());

        logger.info("Delete group order with input {}", map);
        GroupResult result = order.getResultAs(GroupResult.class);
        result.add(groupAccount);

        if (!activeDirectory.groupExists(userAccount, groupAccount.getGroupFqdn())) {
            order.getStatusLogs().add(new OrderStatusLog("AD Group", groupAccount.getGroupFqdn() + " not found in AD", "AD", StatusLogLevel.warning));
            return Response
                    .status(404, "Group '" + groupAccount.getName() + "' not found in AD.")
                    .build();
        }

        try {
            activeDirectory.deleteGroup(userAccount, groupAccount.getGroupFqdn());
            order.getStatusLogs().add(new OrderStatusLog("AD Group", "Deleted group " +  groupAccount.getGroupFqdn() + " in AD", "AD"));
            order.setStatus(OrderStatus.SUCCESS);
        } catch (Exception e) {
            logger.error("Something is wrong with AD for order " + order.getId(), e);
            order.getStatusLogs().add(new OrderStatusLog("AD Group", "Error occurred " + e.getMessage(), "ldap", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }

        orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity("{\"id\":" + order.getId() + "}").build();
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
