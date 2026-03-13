package no.nav.aura.basta.rest.adgroups;

import com.bettercloud.vault.VaultException;

import no.nav.aura.basta.backend.VaultUpdateService;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.GroupAccount;
import no.nav.aura.basta.backend.serviceuser.GroupServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.AdGroupUsage;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.serviceuser.GroupOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.serviceuser.GroupResult;
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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
@RestController
@RequestMapping("/rest/orders/adgroups")
@Transactional
public class AdGroupRestService {

    private static final Logger logger = LoggerFactory.getLogger(AdGroupRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private ActiveDirectory activeDirectory;

    @Inject
    private VaultUpdateService vaultUpdateService;

    @PostMapping
    public ResponseEntity<?> adCreateGroup(@RequestBody Map<String, String> map) throws RuntimeException, VaultException {
        logger.debug("inputMAP: ");
        logger.debug(map.toString());
        try {
            ValidationHelper.validateRequest("/validation/createGroupSchema.json", map);
        } catch (Exception e) {
            logger.error("Could not validate input");
            e.printStackTrace();
            logger.info(e.getStackTrace().toString());

            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("object has missing required properties ([\"application\",\"environmentClass\",\"groupUsage\",\"zone\"])");
        }
        
        ValidationHelper.validateRequest("/validation/createGroupSchema.json", map);
        GroupOrderInput input = new GroupOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.AdGroup, OrderOperation.CREATE, input);
        order.setExternalId("N/A");

        GroupAccount groupAccount = new GroupAccount(input.getEnvironmentClass(), input.getZone(), input.getApplication());
        groupAccount.setGroupUsage(input.getGroupUsage());
        groupAccount.setName(input.getApplication());

        GroupServiceUserAccount groupServiceUserAccount = new GroupServiceUserAccount(input.getEnvironmentClass(), input.getZone(), input.getApplication());
        String userAccountName = groupServiceUserAccount.getUserAccountName();
        order.getStatusLogs().add(
                new OrderStatusLog("AD Group", "Creating new group for " + groupAccount.getName() + " in AD domain " + groupAccount.getDomain(), "adgroup", StatusLogLevel.success));

        GroupServiceUserAccount user = activeDirectory.createOrUpdate(groupServiceUserAccount);
        SortedMap<String, Object> creds = new TreeMap<>();
        creds.put("username", userAccountName);
        creds.put("password", user.getPassword());

        final String vaultCredentialsPath = user.getVaultCredsPath();

        logger.info("Writing service user credentials to vault at " + vaultCredentialsPath);
        vaultUpdateService.writeSecrets(vaultCredentialsPath, creds);

        activeDirectory.ensureUserInAdGroup(groupServiceUserAccount, groupAccount);
        order.getStatusLogs().add(
                new OrderStatusLog("User", "User " + groupServiceUserAccount.getUserAccountName() + " has been added to AD group " + groupAccount.getName() + " in " + groupServiceUserAccount.getDomainFqdn(), "serviceuser", StatusLogLevel.success));

        GroupResult result = order.getResultAs(GroupResult.class);
        result.add(groupAccount);

        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);
        logger.info("Created group order {} with input {}", order.getId(), map);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/rest/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(Map.of("id", order.getId()));
    }

    @GetMapping("/existInAD")
    public ResponseEntity<Boolean> existInAD(
            @RequestParam String application,
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone,
            @RequestParam AdGroupUsage groupUsage) {
        ServiceUserAccount userAccount = new GroupServiceUserAccount(environmentClass, zone, application);
        GroupAccount groupAccount = new GroupAccount(environmentClass, zone, application);
        groupAccount.setGroupUsage(groupUsage);
        groupAccount.setName(application);
        return ResponseEntity.ok(activeDirectory.groupExists(userAccount, groupAccount.getGroupFqdn()));
    }
}