package no.nav.aura.basta.backend.vmware;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import no.nav.aura.basta.backend.vmware.orchestrator.WorkflowExecutor;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.response.OrchestratorResponse;
import no.nav.aura.basta.backend.vmware.orchestrator.response.Vm;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.util.XmlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    private final URL provisionUrl;
    private final URL decomissionUrl;
    private final URL startstopUrl;
    private final URL modifyUrl;
    private WorkflowExecutor workflowExecutor;

    @Autowired
    public OrchestratorService(
            @Value("${rest.orchestrator.provision.url}") URL provisionUrl,
            @Value("${rest.orchestrator.decomission.url}") URL decomissionUrl,
            @Value("${rest.orchestrator.startstop.url}") URL startstopUrl,
            @Value("${rest.orchestrator.modify.url}" ) URL modifyUrl,
            WorkflowExecutor workflowExecutor) {


        this.provisionUrl = provisionUrl;
        this.decomissionUrl = decomissionUrl;
        this.startstopUrl = startstopUrl;
        this.modifyUrl = modifyUrl;
        this.workflowExecutor = workflowExecutor;
    }

	public Optional<String> provision(OrchestatorRequest request) {
        return workflowExecutor.executeWorkflow(provisionUrl, request);
    }



/*
    public WorkflowToken decommission(DecomissionRequest decomissionRequest) {
        return workflowExecutor.executeWorkflow("Multiple Decomission vSphere VM", decomissionRequest, false);
    }

    public WorkflowToken stop(StopRequest stopRequest) {
        return workflowExecutor.executeWorkflow("Power on or off VM - basta", stopRequest, false);
    }
*/
/*
    public WorkflowToken start(StartRequest startRequest) {
        return workflowExecutor.executeWorkflow("Power on or off VM - basta", startRequest, false);
    }*/

    private OrchestratorResponse getOrchestratorResponse(String orchestratorOrderId) {
//        List<WorkflowTokenAttribute> status = workflowExecutor.getStatus(orchestratorOrderId);
//        for (WorkflowTokenAttribute attribute : status) {
//            if (attribute == null) {
//                throw new RuntimeException("Empty response");
//            } else if ("XmlResponse".equalsIgnoreCase(attribute.getName())) {
//                if (attribute.getValue() == null) {
//                    // Strange value that appearently means:
//                    // We've received your order so we'll put this empty answer XML in the reply and then later inexplainably
//                    // remove it.
//                    return null;
//                }
//                return XmlUtils.parseXmlString(OrchestratorResponse.class, attribute.getValue());
//            }
//        }
//        logger.debug("Reply for orchestrator order id " + orchestratorOrderId + ": " + toString(status));
        return null;
    }

    public Tuple<OrderStatus, String> getOrderStatus(String orchestratorOrderId) {
        try {
            OrderStatus status;
            String errorMessage = null;
            OrchestratorResponse response = getOrchestratorResponse(orchestratorOrderId);

            /*
            * kall url/state
            * */
            if (response == null) {
                status = OrderStatus.PROCESSING;
//            } else if (isDecommissionResponse(response)) {
//                String message = "";
//                for (Vm vm : response.getVms()) {
//                    message += comma(message) + getMessageFor(vm);
//                }
//                status = message.isEmpty() ? OrderStatus.SUCCESS : OrderStatus.FAILURE;
//                errorMessage = message.isEmpty() ? null : message;
            }
            else {
                status = response.isDeploymentSuccess() ? OrderStatus.SUCCESS : OrderStatus.FAILURE;
                errorMessage = response.getErr();
            }
            return Tuple.of(status, errorMessage);
        } catch (Exception e) {
            logger.error("Unable to retrieve order status for orchestrator order id " + orchestratorOrderId, e);
            return Tuple.of(OrderStatus.ERROR, e.getMessage());
        }
    }

/*    private boolean isDecommissionResponse(OrchestratorResponse response) {
        return response.getFinishTime() != null && response.getVms() != null;
    }*/

    /*private String getMessageFor(Vm vm) {
        String message = "";
        message += comma(message) + createNotRemovedMessage(vm.getRemovedFromAd(), "removed from AD ");
        message += comma(message) + createNotRemovedMessage(vm.getRemovedFromPuppet(), "removed from Puppet ");
        message += comma(message) + createNotRemovedMessage(vm.getRemovedFromSatellite(), "removed from Satellite ");
        if (!message.isEmpty()) {
            message = "Failure on " + vm.getName() + ": " + message;
        }
        return message;
    }*/

    /*private String comma(String message) {
        return message.isEmpty() ? "" : ", ";
    }
*/
  /*  private String createNotRemovedMessage(Boolean removed, String notRemovedResponseMessagePrefix) {
        if (!Boolean.TRUE.equals(removed)) {
            return notRemovedResponseMessagePrefix + "[" + removed + "]";
        }
        return "";
    }
*/
    /*public static String toString(List<WorkflowTokenAttribute> status) {
        if (status == null) {
            return "<Not found>";
        }
        String string = "";
        for (WorkflowTokenAttribute attr : status) {
            if (attr == null) {
                string += "<Empty response>";
            } else {
                string += "Status: name = " + ToStringBuilder.reflectionToString(attr);
            }
        }
        return string;
    }
*/
}
