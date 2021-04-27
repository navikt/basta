package no.nav.aura.basta.rest.adgroups;

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
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.naming.directory.SearchResult;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Component
@Path("/operation/adgroups")
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
        ServiceUserAccount userAccount = new MqServiceUserAccount(input.getEnvironmentClass(), input.getZone(), input.getApplication());

        order.getStatusLogs().add(new OrderStatusLog("AD Group", "Deleting group " + groupAccount.getGroupFqdn()
                + " in AD for " + groupAccount.getDomain(), "ldap"));
        logger.info("Delete group order with input {}", map);
        GroupResult result = order.getResultAs(GroupResult.class);
        result.add(groupAccount);

        if (!activeDirectory.groupExists(userAccount, groupAccount.getGroupFqdn())) {
            order.getStatusLogs().add(new OrderStatusLog("AD Group", groupAccount.getGroupFqdn() + " not found in AD", "AD", StatusLogLevel.warning));
            return Response.status(404).build();
        }

        try {
           activeDirectory.deleteGroup(userAccount, groupAccount.getGroupFqdn());
        } catch (Exception e) {
            logger.error("Something is wrong with AD for order " + order.getId(), e);
            order.getStatusLogs().add(new OrderStatusLog("AD Group", "Error occurred " + e.getMessage(), "ldap", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }

        order.getStatusLogs().add(new OrderStatusLog("AD Group", "Deleted group " +  groupAccount.getGroupFqdn() + " in AD", "AD"));
        order.setStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity("{\"id\":" + order.getId() + "}").build();
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
