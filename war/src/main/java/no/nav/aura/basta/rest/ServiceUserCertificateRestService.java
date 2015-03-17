package no.nav.aura.basta.rest;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.serviceuser.ServiceUserOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/orders/serviceusercertificate")
@Transactional
public class ServiceUserCertificateRestService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUserCertificateRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasit;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createServiceUser(Map<String, String> map, @Context UriInfo uriInfo) {

        logger.info("Receiving {} ", map);
        ServiceUserOrderInput input = new ServiceUserOrderInput(map);

        Order order = new Order(OrderType.ServiceUser, OrderOperation.CREATE, input);
        order.setExternalId("N/A");
        ServiceUserAccount userAccount = input.getUserAccount();
        // activeDirectory.userExists(userAccount);
        order.getStatusLogs().add(new OrderStatusLog("Active directory", "Hallo verden", "fase 1", "warning"));
        order.getStatusLogs().add(new OrderStatusLog("Active directory", "Doing something", "fase 2", "info"));
        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);
        System.out.println(orderRepository.findOne(order.getId()));

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity(createRichOrderDO(uriInfo, order)).build();
    }

    @GET
    @Path("resourceExists")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean certificateExistsInFasit(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, zone, application);
        return fasit.resourceExists(EnvClass.valueOf(envClass.name()), null, DomainDO.fromFqdn(serviceUserAccount.getDomainFqdn()), application, ResourceTypeDO.Certificate, serviceUserAccount.getAlias());
    }

    protected OrderDO createRichOrderDO(final UriInfo uriInfo, Order order) {
        OrderDO orderDO = new OrderDO(order, uriInfo);
        return orderDO;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
