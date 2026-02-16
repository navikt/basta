package no.nav.aura.basta.rest.serviceuser;

import com.bettercloud.vault.VaultException;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.VaultUpdateService;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SecretPayload;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.inject.Inject;
import java.net.URI;
import java.util.*;

@Component
@RestController
@RequestMapping("/rest/orders/serviceuser/credential")
@Transactional
public class ServiceUserCredentialRestService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUserCredentialRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private RestClient restClient;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @Inject
    private VaultUpdateService vaultUpdateService;

    @Inject
    private ActiveDirectory activeDirectory;

    @PostMapping
    public ResponseEntity<?> createServiceUserCredential(@RequestBody Map<String, String> map) throws VaultException {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(ResourceType.Credential);

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

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/rest/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(Map.of("id", order.getId()));
    }

    private ResourcePayload putCredentialInFasit(Order order, FasitServiceUserAccount userAccount) {
        order.getStatusLogs().add(new OrderStatusLog("Fasit", "Registering credential in Fasit", "fasit"));
        ResourcePayload fasitResource = createFasitResourceWithParams(userAccount);
//        restClient.setOnBehalfOf(User.getCurrentUser().getName());
        
        
        
        ResourcesListPayload resources = findInFasit(userAccount);
        if (resources.isEmpty()) {
            fasitUpdateService.createResource(fasitResource, order);
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Created new credential with alias " + fasitResource.alias + " and  id " + fasitResource.id, "fasit"));
        } else {
            if (resources.getResources().size() != 1) {
                throw new RuntimeException("Found more than one or zero resources" + resources);
            }
            
            ResourcePayload storedResource = resources.getResources().iterator().next();
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Credential already exists in fasit with id " + storedResource.id, "fasit"));
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

        ScopePayload scope = new ScopePayload()
        		.environmentClass(userAccount.getEnvironmentClass())
                // .environment(inputs.get(ENVIRONMENT_NAME))
                .application(userAccount.getApplicationName());
        
        Map<String, SecretPayload> secrets = new HashMap<>();
        SecretPayload passwordSecret = new SecretPayload();
        passwordSecret.withVaultPath(adjustedCredentialsPath + "/password");
        secrets.put("password", passwordSecret);
        
        ResourcePayload payload = new ResourcePayload(ResourceType.Credential, userAccount.getAlias());
        payload.setProperties(properties);
        payload.setScope(scope);
		payload.setSecrets(secrets);
        return payload;
    }

    @GetMapping("/existInAD")
    public ResponseEntity<Boolean> existInAD(
            @RequestParam String application,
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone) {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(environmentClass, zone, application);
        return ResponseEntity.ok(activeDirectory.userExists(serviceUserAccount));
    }

    @GetMapping("/existInFasit")
    public ResponseEntity<Boolean> existsInFasit(
            @RequestParam String application,
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone) {
        return ResponseEntity.ok(!findInFasit(application, environmentClass, zone).isEmpty());
    }

    
    private ResourcesListPayload findInFasit(String application, EnvironmentClass environmentClass, Zone zone) {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(environmentClass, zone, application);
        return findInFasit(serviceUserAccount);
    }

    private ResourcesListPayload findInFasit(FasitServiceUserAccount serviceUserAccount) {
    	ScopePayload scope = new ScopePayload()
						.environmentClass(serviceUserAccount.getEnvironmentClass())
						.application(serviceUserAccount.getApplicationName());
    	
        return restClient.findFasitResources(ResourceType.Credential, serviceUserAccount.getAlias(), scope);
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
