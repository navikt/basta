package no.nav.aura.basta.rest.serviceuser;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.naming.directory.SearchResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import no.nav.aura.basta.domain.result.serviceuser.ServiceUserResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.security.User;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.LifeCycleStatusDO;
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
                new OrderStatusLog("Credential", "Creating new credential for " + userAccount.getUserAccountName() + " in ad " + userAccount.getDomainFqdn(), "ldap", StatusLogLevel.success));
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
        ResourceElement fasitResource = createFasitResourceWithParams(userAccount);
        fasit.setOnBehalfOf(User.getCurrentUser().getName());
        Collection<ResourceElement> resources = findInFasit(userAccount);
        if (resources.isEmpty()) {
            fasitResource = fasit.registerResource(fasitResource, "Creating service user for application " + userAccount.getApplicationName() + " in " + userAccount.getEnvironmentClass());
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Created new credential with alias " + fasitResource.getAlias() + " and  id " + fasitResource.getId(), "fasit"));
        } else {
            if(resources.size()!=1){
                throw new RuntimeException("Found more than one or zero resources" + resources);
            }
            ResourceElement storedResource = resources.iterator().next();
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Credential already exists in fasit with id " + storedResource.getId(), "fasit"));
            fasitResource.setApplication(storedResource.getApplication());
            fasitResource = fasit.updateResource(storedResource.getId(), fasitResource, "Updating service user for application " + userAccount.getApplicationName() + " in " + userAccount.getEnvironmentClass());
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Updated credential with alias " + fasitResource.getAlias() + " and  id " + fasitResource.getId(), "fasit"));
        }
        return fasitResource;
    }

    private ResourceElement createFasitResourceWithParams(ServiceUserAccount userAccount) {
        ResourceElement fasitResource = new ResourceElement(ResourceTypeDO.Credential, userAccount.getAlias());
        fasitResource.setEnvironmentClass(userAccount.getEnvironmentClass().name());
        fasitResource.setDomain(DomainDO.fromFqdn(userAccount.getDomainFqdn()));
        fasitResource.setApplication(userAccount.getApplicationName());
        fasitResource.addProperty(new PropertyElement("username", userAccount.getUserAccountName()));
        fasitResource.addProperty(new PropertyElement("password", userAccount.getPassword()));

        return fasitResource;

    }


    @POST
    @Path("stop")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopServiceUserCredential(Map<String, String> map, @Context UriInfo uriInfo) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(ResourceTypeDO.Credential);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.STOP, input);
        order.setExternalId("N/A");
        ServiceUserAccount userAccount = input.getUserAccount();

        order.getStatusLogs().add(new OrderStatusLog("Credential", "Disableing user" + userAccount.getServiceUserDN() + " in AD for " + userAccount.getDomainFqdn(), "ldap"));
        logger.info("Stop credential order with input {}", order, map);
        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
        result.add(userAccount);
        try {
            activeDirectory.disable(userAccount);

        Collection<ResourceElement> resources = findInFasit(userAccount);
        if (resources.isEmpty()) {
            order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getAlias() + " not found in Fasit", "fasit", StatusLogLevel.warning));
        } else {
            if (resources.size() != 1) {
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Unable to stop resource in Fasit. Found multiple resources: " + resources, "fasit", StatusLogLevel.error));
            }
            ResourceElement resource = resources.iterator().next();
            order.getStatusLogs().add(new OrderStatusLog("Credential", "Updating credential " + resource.getAlias() + "(" + resource.getId() + ") in fasit", "fasit"));
            ResourceElement updateObject= new ResourceElement(resource.getType(), resource.getAlias());
            updateObject.setLifeCycleStatus(LifeCycleStatusDO.STOPPED);
            fasit.updateResource(resource.getId(), updateObject, "Credential is disabled");

            result.add(userAccount, resource);
        }
        order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Something is wrong with AD for order " + order.getId(),e);
            order.getStatusLogs().add(new OrderStatusLog("Credential", "Error occured " + e.getMessage(), "ldap", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity("{\"id\":" + order.getId() + "}").build();

    }

  
    @GET
    @Path("userdn")
    @Produces(MediaType.TEXT_PLAIN)
    public String getUser(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, zone, application);
        return serviceUserAccount.getServiceUserDN();
    }

    @GET
    @Path("existInAD")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existInAD(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, zone, application);
        Optional<SearchResult> user = activeDirectory.getUser(serviceUserAccount);
        if (user.isPresent()) {
            logger.info("bruker {} eksisterer i AD for {}", serviceUserAccount.getUserAccountName(), serviceUserAccount.getDomainFqdn());
            logger.info("ldap bruker {} ",user.get().getAttributes().get("userAccountControl"));
        }
        return user.isPresent();
    }

    @GET
    @Path("existInFasit")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existsInFasit(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        return !findInFasit(application, envClass, zone).isEmpty();
    }

    @GET
    @Path("fasit")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ResourceElement> findInFasit(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, zone, application);
        return findInFasit(serviceUserAccount);
    }

    private Collection<ResourceElement> findInFasit(ServiceUserAccount serviceUserAccount) {
        return fasit.findResources(EnvClass.valueOf(serviceUserAccount.getEnvironmentClass().name()), null, DomainDO.fromFqdn(serviceUserAccount.getDomainFqdn()),
                serviceUserAccount.getApplicationName(),
                ResourceTypeDO.Credential, serviceUserAccount.getAlias());
    }


    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
