package no.nav.aura.basta.rest.serviceuser;

import java.util.Collection;
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
import no.nav.aura.basta.domain.input.serviceuser.ServiceUserOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.serviceuser.ServiceUserResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.security.User;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/orders/serviceuser/credential")
@Transactional
public class ServiceUserCredentialRestService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUserCredentialRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasit;

    @Inject
    private ActiveDirectory activeDirectory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createServiceUserCredential(Map<String, String> map, @Context UriInfo uriInfo) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(ResourceTypeDO.Credential);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.CREATE, input);
        logger.info("Create credential order {} with input {}", order.getId(), map);
        order.setExternalId("N/A");
        ServiceUserAccount userAccount = input.getUserAccount();

        order.getStatusLogs().add(
                new OrderStatusLog("Credential", "Creating new credential for " + userAccount.getUserAccountName() + " in ad " + userAccount.getSecurityDomainFqdn(), "ldap", StatusLogLevel.success));
        ServiceUserAccount user = activeDirectory.createOrUpdate(userAccount);

        ResourceElement resource = putCredentialInFasit(order, user);

        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
        result.add(userAccount, resource);

        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    private ResourceElement putCredentialInFasit(Order order, ServiceUserAccount userAccount) {
        order.getStatusLogs().add(new OrderStatusLog("Fasit", "Registering credential in Fasit", "fasit"));
        ResourceElement fasitResource = null;
        fasit.setOnBehalfOf(User.getCurrentUser().getName());
        if (existsInFasit(userAccount, ResourceTypeDO.Credential)) {
            fasitResource = getResource(userAccount, ResourceTypeDO.Credential);
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Credential already exists in fasit with id " + fasitResource.getId(), "fasit"));
            populateFasitParams(userAccount, fasitResource);
            fasitResource = fasit.updateResource(fasitResource.getId(), fasitResource, "Updating service user for application " + userAccount.getApplicationName() + " in " + userAccount.getEnvironmentClass());
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Updated credential with alias " + fasitResource.getAlias() + " and  id " + fasitResource.getId(), "fasit"));
        } else {

            fasitResource = new ResourceElement(ResourceTypeDO.Credential, userAccount.getAlias());
            populateFasitParams(userAccount, fasitResource);
            fasitResource = fasit.registerResource( fasitResource, "Creating service user for application " + userAccount.getApplicationName() + " in " + userAccount.getEnvironmentClass());
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Created new credential with alias " + fasitResource.getAlias() + " and  id " + fasitResource.getId(), "fasit"));
        }
        return fasitResource;
    }

    private void populateFasitParams(ServiceUserAccount userAccount, ResourceElement fasitResource) {
        fasitResource.setEnvironmentClass(userAccount.getEnvironmentClass().name());
        fasitResource.setApplication(userAccount.getApplicationName());
        fasitResource.addProperty(new PropertyElement("username", userAccount.getUserAccountName()));
        fasitResource.addProperty(new PropertyElement("password", userAccount.getPassword()));
    }

    @GET
    @Path("existInFasit")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existsInFasit(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, application);
        return existsInFasit(serviceUserAccount, ResourceTypeDO.Credential);
    }

    private boolean existsInFasit(ServiceUserAccount serviceUserAccount, ResourceTypeDO type) {
        return fasit.resourceExists(EnvClass.valueOf(serviceUserAccount.getEnvironmentClass().name()), null, DomainDO.fromFqdn(serviceUserAccount.getDomainFqdn()), serviceUserAccount.getApplicationName(),
                type, serviceUserAccount.getAlias());
    }

    @GET
    @Path("existInAD")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existInAD(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, application);
        boolean userExists = activeDirectory.userExists(serviceUserAccount);
        if (userExists) {
            logger.info("bruker {} eksisterer i AD for {}", serviceUserAccount.getUserAccountName(), serviceUserAccount.getSecurityDomainFqdn());
        }
        return userExists;
    }

    private ResourceElement getResource(ServiceUserAccount serviceUserAccount, ResourceTypeDO type) {
        Collection<ResourceElement> resoruces = fasit.findResources(EnvClass.valueOf(serviceUserAccount.getEnvironmentClass().name()), null, DomainDO.fromFqdn(serviceUserAccount.getDomainFqdn()),
                serviceUserAccount.getApplicationName(),
                type, serviceUserAccount.getAlias());
        if (resoruces.size() != 1) {
            throw new RuntimeException("Found more than one or zero resources");
        }
        return resoruces.iterator().next();
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
