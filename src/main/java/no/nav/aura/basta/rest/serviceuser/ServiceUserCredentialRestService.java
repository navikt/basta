package no.nav.aura.basta.rest.serviceuser;

import com.bettercloud.vault.VaultException;
import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.VaultUpdateService;
import no.nav.aura.basta.backend.fasit.deprecated.FasitRestClient;
import no.nav.aura.basta.backend.fasit.deprecated.ResourceElement;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.DomainDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.ResourceTypeDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ScopePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.FasitServiceUserAccount;
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
import java.util.*;

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
    private FasitUpdateService fasitUpdateService;

    @Inject
    private VaultUpdateService vaultUpdateService;

    @Inject
    private ActiveDirectory activeDirectory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createServiceUserCredential(Map<String, String> map, @Context UriInfo uriInfo) throws VaultException {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(ResourceTypeDO.Credential);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.CREATE, input);
        logger.info("Create credential order {} with input {}", order.getId(), map);
        order.setExternalId("N/A");
        FasitServiceUserAccount userAccount = input.getUserAccount();

        order.getStatusLogs().add(
                new OrderStatusLog("Credential", "Creating new credential for " + userAccount.getUserAccountName() + " in ad " + userAccount.getDomainFqdn(), "ldap", StatusLogLevel.success));
        FasitServiceUserAccount user = activeDirectory.createOrUpdate(userAccount);

        SortedMap<String, Object> creds = new TreeMap<>();
        creds.put("username", user.getUserAccountName());
        creds.put("password", user.getPassword());


        final String vaultCredentialsPath = userAccount.getVaultCredsPath();

        logger.info("Writing service user credentials to vault at " + vaultCredentialsPath);
        vaultUpdateService.writeSecrets(vaultCredentialsPath, creds);

        ResourcePayload resource = putCredentialInFasit(order, user);

        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
        result.add(userAccount, resource);

        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }

    private ResourcePayload putCredentialInFasit(Order order, FasitServiceUserAccount userAccount) {
        order.getStatusLogs().add(new OrderStatusLog("Fasit", "Registering credential in Fasit", "fasit"));
        ResourcePayload fasitResource = createFasitResourceWithParams(userAccount);
        fasit.setOnBehalfOf(User.getCurrentUser().getName());
        Collection<ResourceElement> resources = findInFasit(userAccount);
        if (resources.isEmpty()) {
            fasitUpdateService.createResource(fasitResource, order);
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Created new credential with alias " + fasitResource.alias + " and  id " + fasitResource.id, "fasit"));
        } else {
            if (resources.size() != 1) {
                throw new RuntimeException("Found more than one or zero resources" + resources);
            }
            ResourceElement storedResource = resources.iterator().next();
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Credential already exists in fasit with id " + storedResource.getId(), "fasit"));
            fasitUpdateService.createResource(fasitResource, order);
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Updated credential with alias " + fasitResource.alias + " and  id " + fasitResource.id, "fasit"));
        }
        return fasitResource;
    }

    private ResourcePayload createFasitResourceWithParams(FasitServiceUserAccount userAccount) {
        final String vaultCredentialsPath = userAccount.getVaultCredsPath();
        final String adjustedCredentialsPath = vaultCredentialsPath.replace("serviceuser/", "serviceuser/data/");

        final Map<String, String> properties = new HashMap<>();
        properties.put("username", userAccount.getUserAccountName());

        ScopePayload scope = new ScopePayload(
                userAccount.getEnvironmentClass().name())
                // .environment(inputs.get(ENVIRONMENT_NAME))
                .application(userAccount.getApplicationName());

        ResourcePayload payload = new ResourcePayload()
                .withType(ResourceType.credential)
                .withAlias(userAccount.getAlias())
                .withProperties(properties)
                .withScope(scope)
                .withVaultSecret("password", adjustedCredentialsPath + "/password");

        return payload;
    }

    @GET
    @Path("existInAD")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existInAD(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(envClass, zone, application);
        return activeDirectory.userExists(serviceUserAccount);
    }

    @GET
    @Path("existInFasit")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existsInFasit(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        return !findInFasit(application, envClass, zone).isEmpty();
    }

    
    private Collection<ResourceElement> findInFasit(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(envClass, zone, application);
        return findInFasit(serviceUserAccount);
    }

    private Collection<ResourceElement> findInFasit(FasitServiceUserAccount serviceUserAccount) {
        return fasit.findResources(EnvClass.valueOf(serviceUserAccount.getEnvironmentClass().name()), null, DomainDO.fromFqdn(serviceUserAccount.getDomainFqdn()),
                serviceUserAccount.getApplicationName(),
                ResourceTypeDO.Credential, serviceUserAccount.getAlias());
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
