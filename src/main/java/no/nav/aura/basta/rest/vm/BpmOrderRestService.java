package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.security.Guard;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RestController
@RequestMapping("/rest/vm/orders/bpm")
@Transactional
public class BpmOrderRestService extends AbstractVmOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(BpmOrderRestService.class);

    public BpmOrderRestService() {}

    @Inject
    public BpmOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient, RestClient restClient) {
        super(orderRepository, orchestratorClient, restClient);
    }

    @PostMapping("/node")
    public ResponseEntity<?> createBpmNode(@RequestBody Map<String, String> map) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);
        ResponseEntity<List<String>> validationResponse = validateRequiredFasitResourcesForNode(input.getEnvironmentClass(), input.getZone(),
                input.getEnvironmentName(), input.getNodeType());
        List<String> validation = validationResponse.getBody();

        if (!validation.isEmpty()) {
            throw new IllegalArgumentException("Required fasit resources is not present " + validation);
        }

        input.setMiddlewareType(MiddlewareType.bpm_86);
        input.setClassification(Classification.standard);
        input.setApplicationMappingName("applikasjonsgruppe:esb");
        input.setExtraDisk(10);

        if (input.getDescription() == null) {
            input.setDescription("Bpm node in " + input.getEnvironmentName());
        }

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new bpm node order {} with input {}", order.getId(), map);


        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(order.getId());
        URI logCallbackUri = VmOrdersRestApi.apiLogCallbackUri(order.getId());
        
        int numberOfExistingNodes = restClient.getNodeCountFor(input.getEnvironmentName(), "bpm");
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallbackUri);
        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact(FactType.cloud_app_bpm_type, "node");
            // bpm facts
            vm.addPuppetFact(FactType.cloud_app_bpm_node_num, String.valueOf(i + 1 + numberOfExistingNodes));
            vm.addPuppetFact(FactType.cloud_app_bpm_mgr, getBpmDmgrHostname(input));

            vm.addPuppetFact(FactType.cloud_app_bpm_dburl, getCommonDb(input, "url"));

            if (getFailoverDb(input, "url").isPresent()) {
                vm.addPuppetFact(FactType.cloud_app_bpm_dbfailoverurl, getFailoverDb(input, "url"));
            }
            if (getRecoveryDb(input, "url").isPresent()) {
                vm.addPuppetFact(FactType.cloud_app_bpm_dbrecoveryurl, getRecoveryDb(input, "url"));
                vm.addPuppetFact(FactType.cloud_app_bpm_recpwd, getRecoveryDb(input, "password"));
            }

            addCommonFacts(input, vm);
            request.addVm(vm);
        }
        order = executeProvisionOrder(order, request);
        
        URI location = URI.create("/orders/" + order.getId());
        return ResponseEntity.created(location).body(order.asOrderDO());
    }

    @PostMapping("/dmgr")
    public ResponseEntity<?> createBpmDmgr(@RequestBody Map<String, String> map) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);
        logger.info("Received request to create bpm dmgr with input {}", map);
        ResponseEntity<List<String>> validationResponse = validateRequiredFasitResourcesForDmgr(input.getEnvironmentClass(), input.getZone(),
                input.getEnvironmentName(), input.getNodeType());
        List<String> validation = validationResponse.getBody();

        if (!validation.isEmpty()) {
            logger.error("Validation errors for bpm dmgr order: {}", String.join(", ", validation));
            throw new IllegalArgumentException("Required fasit resources is not present " + validation);
        }

        input.setMiddlewareType(MiddlewareType.bpm_86);
        input.setClassification(Classification.custom);
        input.setApplicationMappingName("bpm-dmgr");
        input.setExtraDisk(10);
        input.setServerCount(1);

        if (input.getDescription() == null) {
            input.setDescription("BPM deployment manager for " + input.getEnvironmentName());
        }

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new bpm dmgr order {} with input {}", order.getId(), map);
        
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(order.getId());
        URI logCallbackUri = VmOrdersRestApi.apiLogCallbackUri(order.getId());
        
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallbackUri);
        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact(FactType.cloud_app_bpm_type, "mgr");
            addCommonFacts(input, vm);

            vm.addPuppetFact(FactType.cloud_app_bpm_dburl, getCommonDb(input, "url"));
            vm.addPuppetFact(FactType.cloud_app_bpm_cmnpwd, getCommonDb(input, "password"));
            vm.addPuppetFact(FactType.cloud_app_bpm_cellpwd, getCellDb(input, "password"));

            request.addVm(vm);
        }
        order = executeProvisionOrder(order, request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(order.asOrderDO());
    }

    private void addCommonFacts(VMOrderInput input, Vm vm) {
        // bpm
        vm.addPuppetFact(FactType.cloud_app_bpm_adminpwd, getBpmAdminUser(input, "password"));
        // standard facts
        vm.addPuppetFact(FactType.cloud_app_was_adminuser, getWasAdminUser(input, "username"));
        vm.addPuppetFact(FactType.cloud_app_was_adminpwd, getWasAdminUser(input, "password"));
        vm.addPuppetFact(FactType.cloud_app_ldap_binduser, getLdapBindUser(input, "username"));
        vm.addPuppetFact(FactType.cloud_app_ldap_bindpwd, getLdapBindUser(input, "password"));
        if (input.getZone() == Zone.sbs) {
            vm.addPuppetFact(FactType.cloud_app_ldap_binduser_fss, getWasLdapBindUserForFss(input, "username"));
            vm.addPuppetFact(FactType.cloud_app_ldap_bindpwd_fss, getWasLdapBindUserForFss(input, "password"));
        }
    }

    @GetMapping("/dmgr/validation")
    public ResponseEntity<List<String>> validateRequiredFasitResourcesForDmgr(
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone,
            @RequestParam String environmentName,
            @RequestParam NodeType nodeType) {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(environmentClass);
        input.setZone(zone);
        input.setEnvironmentName(environmentName);
        input.setNodeType(nodeType);
        List<String> validations = commonValidations(input);
        String scope = String.format(" %s|%s|%s", environmentClass, environmentName, zone);

        if (getBpmDmgrHostname(input).isPresent()) {
            validations.add(String.format("Can not create more than one %s in %s", getBpmDmgrAlias(nodeType), scope));
        }

        if (!getCommonDb(input, "url").isPresent()) {
            validations.add(String.format("Missing requried fasit resource bpmCommonDb of type DataSource in scope %s", scope));
        }

        if (!getCellDb(input, "url").isPresent()) {
            validations.add(String.format("Missing requried fasit resource bpmCellDb of type DataSource in scope %s", scope));
        }

        return ResponseEntity.ok(validations);
    }

    @GetMapping("/node/validation")
    public ResponseEntity<List<String>> validateRequiredFasitResourcesForNode(
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone,
            @RequestParam String environmentName,
            @RequestParam NodeType nodeType) {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(environmentClass);
        input.setZone(zone);
        input.setEnvironmentName(environmentName);
        input.setNodeType(nodeType);
        List<String> validations = commonValidations(input);
        Domain domain = Domain.findBy(environmentClass, zone);
        String scope = String.format(" %s|%s|%s", environmentClass, environmentName, domain);

        if (!getBpmDmgrHostname(input).isPresent()) {
            validations.add(String.format("Missing requried fasit resource %s of type DeploymentManager in scope %s",
                    getBpmDmgrAlias(nodeType), scope));
        }

        if (!getCommonDb(input, "url").isPresent()) {
            validations.add(String.format("Missing requried fasit resource bpmCommonDb of type DataSource in scope %s", scope));
        }

        return ResponseEntity.ok(validations);
    }

    private List<String> commonValidations(VMOrderInput input) {
        List<String> validations = new ArrayList<>();
        String scope = String.format(" %s|%s|%s", input.getEnvironmentClass(), input.getEnvironmentName(), input.getDomain());
        if (!getBpmAdminUser(input, "username").isPresent()) {
            validations.add(String.format("Missing requried fasit resource srvBpm of type Credential in scope %s", scope));
        }

        if (!getWasAdminUser(input, "username").isPresent()) {
            validations.add(String.format("Missing requried fasit resource wsAdminUser of type Credential in scope %s", scope));
        }

        if (!getLdapBindUser(input, "username").isPresent()) {
            validations.add(String.format("Missing requried fasit resource wasLdapUser of type Credential in scope %s", scope));
        }
        if (input.getZone() == Zone.sbs && getWasLdapBindUserForFss(input, "username") == null) {
            validations.add(String.format("Missing requried fasit resource wasLdapUser of type Credential in FSS"));
        }
        return validations;
    }

    private Optional<String> getBpmDmgrHostname(VMOrderInput input) {
        String alias = getBpmDmgrAlias(input.getNodeType());
        Optional<ResourcePayload> dmgr = getFasitResource(ResourceType.DeploymentManager, alias, input);

        return resolveProperty(dmgr, "hostname");
    }

    private String getBpmDmgrAlias(NodeType nodeType) {
        return "bpm86Dmgr";
    }

    private Optional<String> getWasAdminUser(VMOrderInput input, String property) {
        Optional<ResourcePayload> wsAdminUser = getFasitResource(ResourceType.Credential, "wsadminUser", input);
        return resolveProperty(wsAdminUser, property);
    }

    private Optional<String> getBpmAdminUser(VMOrderInput input, String property) {
        Optional<ResourcePayload> user = getFasitResource(ResourceType.Credential, "srvBpm", input);
        return resolveProperty(user, property);
    }

    private Optional<String> getLdapBindUser(VMOrderInput input, String property) {
        Optional<ResourcePayload> ldapBindUser = getFasitResource(ResourceType.Credential, "wasLdapUser", input);
        return resolveProperty(ldapBindUser, property);
    }

    private Optional<String> getCommonDb(VMOrderInput input, String property) {
        Optional<ResourcePayload> database = getFasitResource(ResourceType.DataSource, "bpmCommonDb", input);
        return resolveProperty(database, property);
    }

    private Optional<String> getCellDb(VMOrderInput input, String property) {
        Optional<ResourcePayload> database = getFasitResource(ResourceType.DataSource, "bpmCellDb", input);
        return resolveProperty(database, property);
    }

    private Optional<String> getRecoveryDb(VMOrderInput input, String property) {
        Optional<ResourcePayload> database = getFasitResource(ResourceType.DataSource, "bpmRecoveryDb", input);
        return resolveProperty(database, property);
    }

    private Optional<String> getFailoverDb(VMOrderInput input, String property) {
        Optional<ResourcePayload> database = getFasitResource(ResourceType.DataSource, "bpmFailoverDb", input);
        return resolveProperty(database, property);
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
