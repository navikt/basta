package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorEnvironmentClass;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.security.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.inject.Inject;
import java.net.URI;
import java.util.Map;

import static no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType.*;

@Component
@RestController
@RequestMapping("/rest/vm/orders")
@Transactional
public class LinuxOrderRestService extends AbstractVmOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(LinuxOrderRestService.class);

    public LinuxOrderRestService() {}

    @Inject
    public LinuxOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient) {
        super(orderRepository, orchestratorClient);
    }

    @PostMapping("/linux")
    public ResponseEntity<?> createNewPlainLinux(@RequestBody Map<String, String> map) {
        return createNode(map, linux);
    }

    @PostMapping("/devtools")
    public ResponseEntity<?> createNewDevToolsServer(@RequestBody Map<String, String> map) {
        return createNode(map, devtools);
    }

    @PostMapping("/flatcarlinux")
    public ResponseEntity<?> createNewFlatcarLinux(@RequestBody Map<String, String> map) {
        return createNode(map, flatcarlinux);
    }

    private boolean isIAppDevToolsServer(Map<String, String> map, MiddlewareType middlewareType) {
        return devtools.equals(middlewareType) && map.get("zone").equals("iapp");
    }

    private boolean isFlatcarLinux(MiddlewareType middlewareType) { 
        return flatcarlinux.equals(middlewareType); 
    }

    public ResponseEntity<?> createNode(Map<String, String> map, MiddlewareType middlewareType) {
        VMOrderInput input = new VMOrderInput(map);
        boolean iAppDevToolsServer = isIAppDevToolsServer(map, middlewareType);
        boolean flatcarLinuxServer = isFlatcarLinux(middlewareType);

        // need to handle iapp devtools server as a special case. We want all develpopers to be allowed to order these servers without needing prod access.
        // need to set middlewaretype as linux to keep orchestrator from creating this as a devillo dev server.
        if (iAppDevToolsServer) {
            Guard.checkAccessToEnvironmentClass(EnvironmentClass.u);
            input.setMiddlewareType(linux);
        }
        else if (flatcarLinuxServer) {
            Guard.checkAccessToEnvironmentClass(input);
            input.setMiddlewareType(containerlinux);
        } else {
            Guard.checkAccessToEnvironmentClass(input);
            input.setMiddlewareType(middlewareType);
        }

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new linux order {} with input {}", order.getId(), map);
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(order.getId());
        URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(order.getId());
        ProvisionRequest request = new ProvisionRequest(OrchestratorEnvironmentClass.convert(input), input, vmcreateCallbackUri, logCallabackUri);

        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact(FactType.cloud_vm_ibmsw, input.hasIbmSoftware());

            if (iAppDevToolsServer) {
                logger.info("Setting puppet fact cloud_vm_type to iapp_utv");
                vm.addPuppetFact("vpn_accessible", "true");
            }
            vm.setClassification(Classification.custom);
            request.addVm(vm);
        }

        order = executeProvisionOrder(order, request);
        
        URI location = URI.create("/rest/orders/" + order.getId());
        return ResponseEntity.created(location).body(order.asOrderDO());
    }
}