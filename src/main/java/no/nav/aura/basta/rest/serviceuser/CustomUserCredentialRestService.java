package no.nav.aura.basta.rest.serviceuser;

import com.bettercloud.vault.VaultException;
import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.VaultUpdateService;
import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.CustomServiceUserAccount;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.serviceuser.ServiceUserOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.serviceuser.CustomServiceUserResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.ValidationHelper;
import no.nav.aura.envconfig.client.ResourceTypeDO;
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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
@Path("/orders/serviceuser/customcredential")
@Transactional
public class CustomUserCredentialRestService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserCredentialRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private VaultUpdateService vaultUpdateService;

    @Inject
    private ActiveDirectory activeDirectory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createServiceUserCredential(Map<String, String> map, @Context UriInfo uriInfo) throws VaultException {
        ValidationHelper.validateRequest("/validation/createCustomServiceUserSchema.json", map);
        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        logger.info("Received customServiceUser creation request " + map.entrySet().stream().map(e ->e.getKey() + ": " + e.getValue()));

        CustomServiceUserAccount userAccount = input.getCustomUserAccount();
        final String userAccountName = userAccount.getUserAccountName();

        if (existInAD(userAccountName, userAccount.getEnvironmentClass(), input.getZone())) {
            throw new IllegalArgumentException("User " + userAccountName +
                    " already exists in AD. Overwrite of custom service users is not supported. " +
                    "If you want to recreate this user, first delete the existing user in the Operations menu");
        }

        logger.info("We passed validation for " + userAccountName +  " " + userAccount.getEnvironmentClass() + " " + input.getZone());

        input.setResultType(ResourceTypeDO.Credential);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        logger.info("We passed access check for " + userAccountName +  " " + userAccount.getEnvironmentClass() + " " + input.getZone());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.CREATE, input);

        order.setExternalId("N/A");
        order.getStatusLogs().add(
                new OrderStatusLog("Credential", "Creating new credential for " + userAccount.getUserAccountName() + " in ad " + userAccount.getDomainFqdn(), "ldap", StatusLogLevel.success));
        CustomServiceUserAccount user = activeDirectory.createOrUpdate(userAccount);

        SortedMap<String, Object> creds = new TreeMap<>();
        creds.put("username", userAccountName);
        creds.put("password", user.getPassword());

        final String vaultCredentialsPath = user.getVaultCredsPath();

        logger.info("Writing service user credentials to vault at " + vaultCredentialsPath);
        vaultUpdateService.writeSecrets(vaultCredentialsPath, creds);

        final Map<String, String> properties = new HashMap<>();
        properties.put("username", userAccountName);

        CustomServiceUserResult result = order.getResultAs(CustomServiceUserResult.class);
        result.add(userAccount);

        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);
        logger.info("Created credential order {} with input {}", order.getId(), map);
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }


    @GET
    @Path("existInAD")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existInAD(@QueryParam("username") String username, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        CustomServiceUserAccount serviceUserAccount = new CustomServiceUserAccount(envClass, zone, username);
        return activeDirectory.userExists(serviceUserAccount);
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
