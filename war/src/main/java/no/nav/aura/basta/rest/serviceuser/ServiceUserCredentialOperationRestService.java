package no.nav.aura.basta.rest.serviceuser;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.naming.directory.SearchResult;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
import no.nav.aura.envconfig.client.rest.ResourceElement;

@Component
@Path("/operation/serviceuser/credential")
@Transactional
public class ServiceUserCredentialOperationRestService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUserCredentialOperationRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasit;

    @Inject
    private ActiveDirectory activeDirectory;

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

        order.getStatusLogs().add(new OrderStatusLog("Credential", "Disabling user" + userAccount.getServiceUserDN() + " in AD for " + userAccount.getDomainFqdn(), "ldap"));
        logger.info("Stop credential order with input {}", map);
        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
        result.add(userAccount);
        try {
            if(activeDirectory.userExists(userAccount)){
                activeDirectory.disable(userAccount);
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Disabled credential " +  userAccount.getServiceUserDN() + " in AD", "AD"));
            }else{
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getServiceUserDN() + " not found in AD", "AD", StatusLogLevel.warning));
            }
            

            Collection<ResourceElement> resources = findInFasit(userAccount);
            if (resources.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getAlias() + " not found in Fasit", "fasit", StatusLogLevel.warning));
            } else {
                if (resources.size() != 1) {
                    order.getStatusLogs().add(new OrderStatusLog("Credential", "Unable to stop resource in Fasit. Found multiple resources: " + resources, "fasit", StatusLogLevel.error));
                }
                ResourceElement resource = resources.iterator().next();
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Updating credential " + resource.getAlias() + "(" + resource.getId() + ") in fasit", "fasit"));
                ResourceElement updateObject = new ResourceElement(resource.getType(), resource.getAlias());
                updateObject.setLifeCycleStatus(LifeCycleStatusDO.STOPPED);
                fasit.setOnBehalfOf(User.getCurrentUser().getName());
                fasit.updateResource(resource.getId(), updateObject, "Credential is disabled from Basta");

                result.add(userAccount, resource);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Something is wrong with AD for order " + order.getId(), e);
            order.getStatusLogs().add(new OrderStatusLog("Credential", "Error occured " + e.getMessage(), "ldap", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity("{\"id\":" + order.getId() + "}").build();
    }

    @POST
    @Path("start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startServiceUserCredential(Map<String, String> map, @Context UriInfo uriInfo) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(ResourceTypeDO.Credential);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.START, input);
        order.setExternalId("N/A");
        ServiceUserAccount userAccount = input.getUserAccount();

        order.getStatusLogs().add(new OrderStatusLog("Credential", "Enabling user" + userAccount.getServiceUserDN() + " in AD for " + userAccount.getDomainFqdn(), "ldap"));
        logger.info("Start credential order with input {}", map);
        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
        result.add(userAccount);
        try {
            if(activeDirectory.userExists(userAccount)){
                activeDirectory.enable(userAccount);
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Enabeled credential " +  userAccount.getServiceUserDN() + " in AD", "AD"));
            }else{
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getServiceUserDN() + " not found in AD", "AD", StatusLogLevel.warning));
            }

            Collection<ResourceElement> resources = findInFasit(userAccount);
            if (resources.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getAlias() + " not found in Fasit", "fasit", StatusLogLevel.warning));
            } else {
                if (resources.size() != 1) {
                    order.getStatusLogs().add(new OrderStatusLog("Credential", "Unable to start resource in Fasit. Found multiple resources: " + resources, "fasit", StatusLogLevel.error));
                }
                ResourceElement resource = resources.iterator().next();
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Updating credential " + resource.getAlias() + "(" + resource.getId() + ") in fasit", "fasit"));
                ResourceElement updateObject = new ResourceElement(resource.getType(), resource.getAlias());
                updateObject.setLifeCycleStatus(LifeCycleStatusDO.STARTED);
                fasit.setOnBehalfOf(User.getCurrentUser().getName());
                fasit.updateResource(resource.getId(), updateObject, "Credential is started in Basta");

                result.add(userAccount, resource);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Something is wrong with AD for order " + order.getId(), e);
            order.getStatusLogs().add(new OrderStatusLog("Credential", "Error occured " + e.getMessage(), "ldap", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity("{\"id\":" + order.getId() + "}").build();
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteServiceUser(Map<String, String> map, @Context UriInfo uriInfo) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(ResourceTypeDO.Credential);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.DELETE, input);
        order.setExternalId("N/A");
        ServiceUserAccount userAccount = input.getUserAccount();

        order.getStatusLogs().add(new OrderStatusLog("Credential", "Deleting user" + userAccount.getServiceUserDN() + " in AD for " + userAccount.getDomainFqdn(), "ldap"));
        logger.info("Delete credential order with input {}", map);
        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
        result.add(userAccount);
        try {
            if(activeDirectory.userExists(userAccount)){
                activeDirectory.delete(userAccount);
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Deleted credential " +  userAccount.getServiceUserDN() + " in AD", "AD"));
            }else{
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getServiceUserDN() + " not found in AD", "AD", StatusLogLevel.warning));
            }

            Collection<ResourceElement> resources = findInFasit(userAccount);
            if (resources.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getAlias() + " not found in Fasit", "fasit", StatusLogLevel.warning));
            } else {
                if (resources.size() != 1) {
                    order.getStatusLogs().add(new OrderStatusLog("Credential", "Unable to delete resource in Fasit. Found multiple resources: " + resources, "fasit", StatusLogLevel.error));
                }
                ResourceElement resource = resources.iterator().next();
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Deleting credential " + resource.getAlias() + "(" + resource.getId() + ") in fasit", "fasit"));
                fasit.setOnBehalfOf(User.getCurrentUser().getName());
                fasit.deleteResource(resource.getId(), "Credential is deleted in Basta");

                result.add(userAccount, resource);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Something is wrong with AD for order " + order.getId(), e);
            order.getStatusLogs().add(new OrderStatusLog("Credential", "Error occured " + e.getMessage(), "ldap", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        orderRepository.save(order);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity("{\"id\":" + order.getId() + "}").build();
    }

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceUserAccount getUser(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, zone, application);
        return serviceUserAccount;
    }

    @GET
    @Path("existInAD")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existInAD(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, zone, application);
        Optional<SearchResult> user = activeDirectory.getUser(serviceUserAccount);
        if (user.isPresent()) {
            logger.info("bruker {} eksisterer i AD for {}", serviceUserAccount.getUserAccountName(), serviceUserAccount.getDomainFqdn());
            logger.info("ldap bruker {} ", user.get().getAttributes().get("userAccountControl"));
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
