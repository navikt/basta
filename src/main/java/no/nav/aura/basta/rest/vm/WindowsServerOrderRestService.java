package no.nav.aura.basta.rest.vm;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.inject.Inject;
import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorEnvironmentClass;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.security.Guard;

@Component
@RestController
@RequestMapping("/rest/vm/orders/windows")
@Transactional
public class WindowsServerOrderRestService extends AbstractVmOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(WindowsServerOrderRestService.class);

    public WindowsServerOrderRestService() {}

    @Inject
    public WindowsServerOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient, FasitRestClient fasitRestClient) {
        super(orderRepository, orchestratorClient, fasitRestClient);
    }

    @PostMapping
    public ResponseEntity<?> createWindowsServer(@RequestBody Map<String, String> map) {
        VMOrderInput input = new VMOrderInput(map);

        if (input.getMiddlewareType() == MiddlewareType.windows_ap) {
            input.setNodeType(NodeType.WINDOWS_APPLICATIONSERVER);
        } else {
            input.setNodeType(NodeType.WINDOWS_INTERNET_SERVER);
        }
        Guard.checkAccessToEnvironmentClass(input);
        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new windows order {} with input {}", order.getId(), map);
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(order.getId());
        URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(order.getId());
        ProvisionRequest request = new ProvisionRequest(OrchestratorEnvironmentClass.convertWithoutMultisite(input.getEnvironmentClass()), input, vmcreateCallbackUri, logCallabackUri);
        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact(FactType.cloud_vm_ibmsw, input.hasIbmSoftware());
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
}