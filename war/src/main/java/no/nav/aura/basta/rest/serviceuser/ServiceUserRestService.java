package no.nav.aura.basta.rest.serviceuser;

import static no.nav.aura.envconfig.client.ResourceTypeDO.Certificate;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
@Path("/orders/serviceuser")
@Transactional
public class ServiceUserRestService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUserRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasit;

    @POST
    @Path("certificate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrUpdateCertificate(Map<String, String> map, @Context UriInfo uriInfo) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(Certificate);

        Order order = new Order(OrderType.ServiceUser, OrderOperation.CREATE, input);
        order.setExternalId("N/A");
        ServiceUserAccount userAccount = input.getUserAccount();

        if (existsInFasit(userAccount, ResourceTypeDO.Credential)) {
            throw new RuntimeException("brukern finnes ikke i Fasit. ");
        }
        if (existsInFasit(userAccount, Certificate)) {
            logger.info("update certificate {} ", map);
            // Cservice create jks
            // oppdater i fasit
        } else {
            // Cservice create jks
            // register i fasit
            logger.info("create new certificate {} ", map);
        }
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
    @Path("{resourceType}/resourceExists")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existsInFasit(@PathParam("resourceType") ResourceTypeDO resourceType, @QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass,
            @QueryParam("zone") Zone zone) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, zone, application);
        return existsInFasit(serviceUserAccount, resourceType);
    }

    private boolean existsInFasit(ServiceUserAccount serviceUserAccount, ResourceTypeDO type) {
        return fasit.resourceExists(EnvClass.valueOf(serviceUserAccount.getEnvironmentClass().name()), null, DomainDO.fromFqdn(serviceUserAccount.getDomainFqdn()), serviceUserAccount.getApplicationName(),
                type, serviceUserAccount.getAlias());
    }

    protected OrderDO createRichOrderDO(final UriInfo uriInfo, Order order) {
        OrderDO orderDO = new OrderDO(order, uriInfo);
        return orderDO;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
