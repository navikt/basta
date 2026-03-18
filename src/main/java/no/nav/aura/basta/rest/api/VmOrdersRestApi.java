package no.nav.aura.basta.rest.api;

import java.net.URI;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.inject.Inject;
import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponse;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.vm.VmOperationsRestService;
import no.nav.aura.basta.rest.vm.VmOrderCallbackService;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;

@Component
@RestController
@RequestMapping("/rest/api/orders/vm")
@Transactional
public class VmOrdersRestApi {

    private static final Logger logger = LoggerFactory.getLogger(VmOrdersRestApi.class);

    @Inject
    private VmOrderCallbackService vmOrderCallbackService;

    @Inject
    private VmOperationsRestService vmOperationsRestService;

    private static final String callbackHost = System.getenv("ORCHESTRATOR_CALLBACK_HOST_URL");

    @PostMapping("/stop")
    public ResponseEntity<?> createStopVmOrder(@RequestBody String... hostnames) {
        logger.info("Creating stop order for hostnames {}", Arrays.asList(hostnames));
        return vmOperationsRestService.stop(hostnames);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> createDecommisionVmOrder(@RequestBody String... hostnames) {
        logger.info("Creating decommision order for hostnames {}", Arrays.asList(hostnames));
        return vmOperationsRestService.decommission(hostnames);
    }

    @PutMapping("/{orderId}/decommission" )
    public ResponseEntity<Void> removeCallback(@PathVariable Long orderId, @RequestBody OrchestratorNodeDO vm) {
    	logger.info("Creating decommission callback for orderId {} and vm {}", orderId, vm.getHostName());
        vmOperationsRestService.deleteVmCallback(orderId, vm);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{orderId}/stop")
    public ResponseEntity<Void> stopCallback(@PathVariable Long orderId, @RequestBody OperationResponse response) {
        vmOperationsRestService.vmOperationCallback(orderId, response);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{orderId}/start")
    public ResponseEntity<Void> startCallback(@PathVariable Long orderId, @RequestBody OperationResponse response) {
        vmOperationsRestService.vmOperationCallback(orderId, response);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{orderId}/vm")
    public ResponseEntity<Void> provisionCallback(@PathVariable Long orderId, @RequestBody OrchestratorNodeDOList vmList) {
        vmOrderCallbackService.createVmCallBack(orderId, vmList.getVms());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/statuslog")
    public ResponseEntity<Void> logCallback(@PathVariable Long orderId, @RequestBody OrderStatusLogDO orderStatusLogDO) {
        vmOrderCallbackService.updateStatuslog(orderId, orderStatusLogDO);
        return ResponseEntity.noContent().build();
    }

    // Static helper methods for callback URI generation
    public static URI apiCreateCallbackUri(Long entityId) {
        return generateUri(entityId, "provisionCallback");
    }

    public static URI apiStopCallbackUri(Long entityId) {
        return generateUri(entityId, "stopCallback");
    }

    public static URI apiStartCallbackUri(Long entityId) {
        return generateUri(entityId, "startCallback");
    }

    public static URI apiDecommissionCallbackUri(Long entityId) {
        return generateUri(entityId, "removeCallback");
    }

    public static URI apiLogCallbackUri(Long entityId) {
        return generateUri(entityId, "logCallback");
    }

    private static URI generateUri(Long entityId, String methodName) {
        if (callbackHost != null && !callbackHost.isEmpty()) {
            URI uri = URI.create(callbackHost + "/rest/api/orders/vm/" + entityId + "/" + getPathForMethod(methodName));
            logger.info("Creating callback uri: " + uri.toString());
            return uri;
        }
        logger.warn("ORCHESTRATOR_CALLBACK_HOST_URL is not set, using default localhost callback URI");
		return URI.create("http://localhost:1337/rest/api/orders/vm/" + entityId + "/" + getPathForMethod(methodName));
    }

    private static String getPathForMethod(String methodName) {
        switch (methodName) {
            case "provisionCallback":
                return "vm";
            case "stopCallback":
                return "stop";
            case "startCallback":
                return "start";
            case "removeCallback":
                return "decommission";
            case "logCallback":
                return "statuslog";
            default:
                return methodName;
        }
    }
}