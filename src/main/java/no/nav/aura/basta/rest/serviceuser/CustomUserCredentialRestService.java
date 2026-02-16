package no.nav.aura.basta.rest.serviceuser;

import com.bettercloud.vault.VaultException;
import no.nav.aura.basta.backend.VaultUpdateService;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@RestController
@RequestMapping("/rest/orders/serviceuser/customcredential")
@Transactional
public class CustomUserCredentialRestService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserCredentialRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private VaultUpdateService vaultUpdateService;

    @Inject
    private ActiveDirectory activeDirectory;

    @PostMapping
    public ResponseEntity<?> createServiceUserCredential(@RequestBody Map<String, String> map) throws VaultException {
        try {
            ValidationHelper.validateRequest("/validation/createCustomServiceUserSchema.json", map);
        } catch (Exception e) {
            e.printStackTrace();
            String failureMessage = null;
            if (e.getMessage().contains("not found")) {
                failureMessage = "object has missing required properties ([\"environmentClass\",\"username\",\"zone\"])";
            } else {
                failureMessage = e.getMessage();
            }
            logger.debug(failureMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(failureMessage);
        }

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        String s = "Received customServiceUser creation request " + map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("  "));
        logger.info(s);

        CustomServiceUserAccount userAccount = input.getCustomUserAccount();
        final String userAccountName = userAccount.getUserAccountName();

        if (userExistsInAD(userAccountName, userAccount.getEnvironmentClass(), input.getZone())) {
            throw new IllegalArgumentException("User " + userAccountName +
                    " already exists in AD. Overwrite of custom service users is not supported. " +
                    "If you want to recreate this user, first delete the existing user in the Operations menu");
        }

        logger.info("We passed validation for " + userAccountName +  " " + userAccount.getEnvironmentClass() + " " + input.getZone());

        input.setResultType(ResourceType.Credential);
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
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(Map.of("id", order.getId()));
    }

    private boolean userExistsInAD(String username, EnvironmentClass environmentClass, Zone zone) {
        CustomServiceUserAccount serviceUserAccount = new CustomServiceUserAccount(environmentClass, zone, username);
        return activeDirectory.userExists(serviceUserAccount);
    }

    @GetMapping("/existInAD")
    public ResponseEntity<Boolean> existInAD(
            @RequestParam String username,
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone) {
        CustomServiceUserAccount serviceUserAccount = new CustomServiceUserAccount(environmentClass, zone, username);
        return ResponseEntity.ok(activeDirectory.userExists(serviceUserAccount));
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}