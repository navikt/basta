package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponse;
import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponseVm;
import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponseVm.ResultType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.HostnamesInput;
import no.nav.aura.basta.domain.result.vm.ResultStatus;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.security.User;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.inject.Inject;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;

@Component
@RestController
@RequestMapping("/rest/vm/operations")
@Transactional
public class VmOperationsRestService {
    private static final Logger logger = LoggerFactory.getLogger(VmOperationsRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private OrchestratorClient orchestratorClient;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @PostMapping("/decommission")
    public ResponseEntity<Map<String, Long>> decommission(@RequestBody String... hostnames) {
        checkAccessFromHostName(hostnames);
        HostnamesInput input = new HostnamesInput(hostnames);
        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.DELETE, input));
        logger.info("created new decommission order {} for hosts {} ", order.getId(), hostnames);
        URI statuslogUri = VmOrdersRestApi.apiLogCallbackUri(order.getId());
        URI decommissionUri = VmOrdersRestApi.apiDecommissionCallbackUri(order.getId());
        DecomissionRequest request = new DecomissionRequest(hostnames, decommissionUri, statuslogUri);
        order.addStatuslogInfo("Calling Orchestrator for decommissioning");

        Optional<String> decomissionUrl = orchestratorClient.decomission(request);

        decomissionUrl.ifPresent(s -> order.setExternalId(s.toString()));

        if (!decomissionUrl.isPresent()) {
            order.setStatus(FAILURE);
        }
        orderRepository.save(order);

        HashMap<String, Long> result = new HashMap<>();
        result.put("orderId", order.getId());
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Long>> stop(@RequestBody String... hostnames) {
        checkAccessFromHostName(hostnames);
        HostnamesInput input = new HostnamesInput(hostnames);
        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.STOP, input));
        URI statuslogUri = VmOrdersRestApi.apiLogCallbackUri(order.getId());
        URI stopUri = VmOrdersRestApi.apiStopCallbackUri(order.getId());

        StopRequest request = new StopRequest(hostnames, stopUri, statuslogUri);
        order.addStatuslogInfo("Calling Orchestrator for stopping");
        Optional<String> runningWorkflowUrl = orchestratorClient.stop(request);

        if (!runningWorkflowUrl.isPresent()) {
            order.setStatus(FAILURE);
        } else {
            order.setExternalId(runningWorkflowUrl.get().toString());
        }

        orderRepository.save(order);

        Map<String, Long> result = new HashMap<>();
        result.put("orderId", order.getId());
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Long>> start(@RequestBody String... hostnames) {
        checkAccessFromHostName(hostnames);
        HostnamesInput input = new HostnamesInput(hostnames);
        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.START, input));
        URI resultUri = VmOrdersRestApi.apiLogCallbackUri(order.getId());
        URI startUri = VmOrdersRestApi.apiStartCallbackUri(order.getId());

        StartRequest request = new StartRequest(hostnames, startUri, resultUri);
        order.addStatuslogInfo("Calling Orchestrator for starting");

        Optional<String> runningWorkflowUrl = orchestratorClient.start(request);

        if (!runningWorkflowUrl.isPresent()) {
            order.setStatus(FAILURE);
        } else {
            order.setExternalId(runningWorkflowUrl.get().toString());
        }

        orderRepository.save(order);

        HashMap<String, Long> result = new HashMap<>();
        result.put("orderId", order.getId());
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/rest/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    public void deleteVmCallback(Long orderId, OrchestratorNodeDO vm) {
        logger.info("Received callback delete order {} , {} ", orderId, ReflectionToStringBuilder.toString(vm));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found " + orderId));
        order.getResultAs(VMOrderResult.class).addHostnameWithStatus(vm.getHostName(), ResultStatus.DECOMMISSIONED);

        orderRepository.save(order);
        fasitUpdateService.removeFasitEntity(order, vm.getHostName());
    }

    public void vmOperationCallback(Long orderId, OperationResponse response) {
        logger.info("Received operation callback  order {} , {} ", orderId, response);
        Order order = orderRepository.findById(orderId).orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found " + orderId));
        for (OperationResponseVm vm : response.getVms()) {
            String hostname = vm.getHostname();
            if (vm.getResult() == ResultType.off) {
                order.getResultAs(VMOrderResult.class).addHostnameWithStatus(hostname, ResultStatus.STOPPED);
                fasitUpdateService.stopFasitEntity(order, hostname);
            }
            if (vm.getResult() == ResultType.on) {
                order.getResultAs(VMOrderResult.class).addHostnameWithStatus(hostname, ResultStatus.ACTIVE);
                fasitUpdateService.startFasitEntity(order, hostname);
            }
            if (vm.getResult() == ResultType.error || vm.getResult() == null) {
                logger.info("Errorcallback from orchestrator for hostname {} with result {}", hostname, vm.getResult());
                order.addStatuslogError("Orchestrator callback: Error with host :" + hostname + " check this");
            }
            orderRepository.save(order);
        }
    }

    private void checkAccessFromHostName(String... hostnames) {
        for (String hostname : hostnames) {
            EnvironmentClass environmentClass = findEnvironmentFromHostame(hostname);
            if (!User.getCurrentUser().hasAccess(environmentClass)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                        "User " + User.getCurrentUser().getName() + " does not have access to decommission node: " + hostname);
            }
        }
    }

    private EnvironmentClass findEnvironmentFromHostame(String hostname) {
        if (hostname.startsWith("a") || hostname.startsWith("c")) {
            return EnvironmentClass.p;
        }
        if (hostname.startsWith("b")) {
            return EnvironmentClass.q;
        }
        if (hostname.startsWith("d")) {
            return EnvironmentClass.t;
        }
        if (hostname.startsWith("e")) {
            return EnvironmentClass.u;
        }
        logger.info("Unknown hostnamepattern {} Expecting environmentClass p", hostname);
        return EnvironmentClass.p;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}