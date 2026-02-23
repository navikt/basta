package no.nav.aura.basta.rest.serviceuser;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.payload.LifeCycleStatus;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.FasitServiceUserAccount;
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
import no.nav.aura.basta.rest.adgroups.AdGroupRestService;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.inject.Inject;
import javax.naming.directory.SearchResult;
import java.util.Map;
import java.util.Optional;

@Component
@RestController
@RequestMapping("/rest/operation/serviceuser/credential")
@Transactional
public class ServiceUserCredentialOperationRestService {

//    private final AdGroupRestService adGroupRestService;

    private static final Logger logger = LoggerFactory.getLogger(ServiceUserCredentialOperationRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private RestClient restClient;
    
//    @Inject
//    private FasitUpdateService fasitUpdateService;
    
    @Value("${fasit_base_url}")
    private String fasitBaseURL;

    @Inject
    private ActiveDirectory activeDirectory;

//    ServiceUserCredentialOperationRestService(AdGroupRestService adGroupRestService) {
//        this.adGroupRestService = adGroupRestService;
//    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopServiceUserCredential(@RequestBody Map<String, String> map) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(ResourceType.Credential);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.STOP, input);
        order.setExternalId("N/A");
        try {
	        FasitServiceUserAccount userAccount = input.getUserAccount();
	
	        order.getStatusLogs().add(new OrderStatusLog("Credential", "Disabling user" + userAccount.getServiceUserDN() + " in AD for " + userAccount.getDomainFqdn(), "ldap"));
	        logger.info("Stop credential order with input {}", map);
	        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
	        result.add(userAccount);
	        
        	if(activeDirectory.userExists(userAccount)){
                activeDirectory.disable(userAccount);
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Disabled credential " +  userAccount.getServiceUserDN() + " in AD", "AD"));
            }else{
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getServiceUserDN() + " not found in AD", "AD", StatusLogLevel.warning));
            }
            

            ResourcesListPayload resources = findInFasit(userAccount);
            if (resources.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getAlias() + " not found in Fasit", "fasit", StatusLogLevel.warning));
            } else {
                if (resources.getResources().size() != 1) {
                    order.getStatusLogs().add(new OrderStatusLog("Credential", "Unable to stop resource in Fasit. Found multiple resources: " + resources, "fasit", StatusLogLevel.error));
                }
                ResourcePayload resource = resources.getResources().iterator().next();
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Updating credential " + resource.getAlias() + "(" + resource.id + ") in fasit", "fasit"));
                ResourcePayload updateObject = new ResourcePayload(resource.getType(), resource.getAlias());
                updateObject.setLifeCycleStatus(LifeCycleStatus.STOPPED);
                
                final String url = fasitResourcesUrl() + "/" + resource.id;
                final String comment = "Credential is deleted in Basta";
                restClient.updateFasitResource(url, toJson(updateObject), User.getCurrentUser().getName(), comment);

                result.add(userAccount, resource);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Something is wrong with AD for order " + order.getId(), e);
            order.getStatusLogs().add(new OrderStatusLog("Credential", "Error occured " + e.getMessage(), "ldap", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
            orderRepository.save(order);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", order.getId()));
    }

    @PostMapping("/start")
    public ResponseEntity<?> startServiceUserCredential(@RequestBody Map<String, String> map) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(ResourceType.Credential);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.START, input);
        order.setExternalId("N/A");
        FasitServiceUserAccount userAccount = input.getUserAccount();

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

            ResourcesListPayload resources = findInFasit(userAccount);
            if (resources.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getAlias() + " not found in Fasit", "fasit", StatusLogLevel.warning));
            } else {
                if (resources.getResources().size() != 1) {
                    order.getStatusLogs().add(new OrderStatusLog("Credential", "Unable to start resource in Fasit. Found multiple resources: " + resources, "fasit", StatusLogLevel.error));
                }
                ResourcePayload resource = resources.getResources().iterator().next();
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Updating credential " + resource.getAlias() + "(" + resource.id + ") in fasit", "fasit"));
                ResourcePayload updateObject = new ResourcePayload(resource.getType(), resource.getAlias());
                updateObject.setLifeCycleStatus(LifeCycleStatus.RUNNING);

                final String url = fasitResourcesUrl() + "/" + resource.id;
                final String comment = "Credential is deleted in Basta";
                restClient.updateFasitResource(url, toJson(updateObject), User.getCurrentUser().getName(), comment);
                
                result.add(userAccount, resource);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Something is wrong with AD for order " + order.getId(), e);
            order.getStatusLogs().add(new OrderStatusLog("Credential", "Error occured " + e.getMessage(), "ldap", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", order.getId()));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteServiceUser(@RequestBody Map<String, String> map) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(ResourceType.Credential);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.DELETE, input);
        order.setExternalId("N/A");
        FasitServiceUserAccount userAccount = input.getUserAccount();

        order.getStatusLogs().add(new OrderStatusLog("Credential", "Deleting user " + userAccount.getServiceUserDN()
                + " in AD for " + userAccount.getDomainFqdn(), "ldap"));
        logger.info("Delete credential order with input {}", map);
        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
        result.add(userAccount);
        try {
            if(activeDirectory.userExists(userAccount)){
                activeDirectory.deleteUser(userAccount);
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Deleted credential " +  userAccount.getServiceUserDN() + " in AD", "AD"));
            }else{
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getServiceUserDN() + " not found in AD", "AD", StatusLogLevel.warning));
            }

            ResourcesListPayload resources = findInFasit(userAccount);
            if (resources.isEmpty()) {
                order.getStatusLogs().add(new OrderStatusLog("Credential", userAccount.getAlias() + " not found in Fasit", "fasit", StatusLogLevel.warning));
            } else {
                if (resources.getResources().size() != 1) {
                    order.getStatusLogs().add(new OrderStatusLog("Credential", "Unable to delete resource in Fasit. Found multiple resources: " + resources, "fasit", StatusLogLevel.error));
                }
                ResourcePayload resource = resources.getResources().iterator().next();
                order.getStatusLogs().add(new OrderStatusLog("Credential", "Deleting credential " + resource.getAlias() + "(" + resource.id + ") in fasit", "fasit"));
                
                final String url = fasitResourcesUrl() + "/" + resource.id;
                final String comment = "Credential is deleted in Basta";
                restClient.deleteFasitResource(url, User.getCurrentUser().getName(), comment);

                //TODO check response code
                
                result.add(userAccount, resource);
            }
            order.setStatus(OrderStatus.SUCCESS);

        } catch (Exception e) {
            logger.error("Something is wrong with AD for order " + order.getId(), e);
            order.getStatusLogs().add(new OrderStatusLog("Credential", "Error occured " + e.getMessage(), "ldap", StatusLogLevel.error));
            order.setStatus(OrderStatus.ERROR);
        }
        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", order.getId()));
    }

    @GetMapping("/user")
    public ResponseEntity<ServiceUserAccount> getUser(
            @RequestParam String application,
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone) {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(environmentClass, zone, application);
        return ResponseEntity.ok(serviceUserAccount);
    }

    @GetMapping("/existInAD")
    public ResponseEntity<Boolean> existInAD(
            @RequestParam String application,
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone) {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(environmentClass, zone, application);
        Optional<SearchResult> user = activeDirectory.getUser(serviceUserAccount);
        if (user.isPresent()) {
            logger.info("bruker {} eksisterer i AD for {}", serviceUserAccount.getUserAccountName(), serviceUserAccount.getDomainFqdn());
            logger.info("ldap bruker {} ", user.get().getAttributes().get("userAccountControl"));
        }
        return ResponseEntity.ok(user.isPresent());
    }

    @GetMapping("/existInFasit")
    public ResponseEntity<Boolean> existsInFasit(
            @RequestParam String application,
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone) {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(environmentClass, zone, application);
        return ResponseEntity.ok(!findInFasit(serviceUserAccount).isEmpty());
    }

    @GetMapping("/fasit")
    public ResponseEntity<ResourcesListPayload> findInFasit(
            @RequestParam String application,
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone) {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(environmentClass, zone, application);
        return ResponseEntity.ok(findInFasit(serviceUserAccount));
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
    
    public static String toJson(Object payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Error serializing payload to JSON", jpe);
        }
    }
    
    public String fasitResourcesUrl() {
		return fasitBaseURL + "/api/v2/resources";
	}
}
