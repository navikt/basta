package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static no.nav.aura.basta.backend.vmware.orchestrator.WorkflowExecutionStatus.fromExecutionState;

public class OrchestratorClient {
    private static Logger log = LoggerFactory.getLogger(OrchestratorClient.class);

    private final URL provisionUrl;
    private final URL decomissionUrl;
    private final URL startstopUrl;
    private final URL modifyUrl;

    private final RestClient restClient;
    private final String ORCHESTRATOR_REQUEST_TEMPLATE = "{\"parameters\": [{\"name\": \"XmlRequest\",\"type\": \"string\",\"value\": {\"string\": {\"value\":\"%s\"}} }]}";


/*    public WorkflowToken decommission(DecomissionRequest decomissionRequest) {
        return orchestratorClient.executeWorkflow("Multiple Decomission vSphere VM", decomissionRequest, false);
    }

    public WorkflowToken stop(StopRequest stopRequest) {
        return orchestratorClient.executeWorkflow("Power on or off VM - basta", stopRequest, false);
    }
*/
/*
    public WorkflowToken start(StartRequest startRequest) {
        return orchestratorClient.executeWorkflow("Power on or off VM - basta", startRequest, false);
    }*/

//    private OrchestratorResponse getOrchestratorResponse(String orchestratorOrderId) {
////        List<WorkflowTokenAttribute> status = orchestratorClient.getStatus(orchestratorOrderId);
////        for (WorkflowTokenAttribute attribute : status) {
////            if (attribute == null) {
////                throw new RuntimeException("Empty response");
////            } else if ("XmlResponse".equalsIgnoreCase(attribute.getName())) {
////                if (attribute.getValue() == null) {
////                    // Strange value that appearently means:
////                    // We've received your order so we'll put this empty answer XML in the reply and then later inexplainably
////                    // remove it.
////                    return null;
////                }
////                return XmlUtils.parseXmlString(OrchestratorResponse.class, attribute.getValue());
////            }
////        }
////        logger.debug("Reply for orchestrator order id " + orchestratorOrderId + ": " + toString(status));
//        return null;
//    }

//    public Tuple<OrderStatus, String> getOrderStatus(String executionUrl) {
////        try {
////            OrderStatus status;
//        String errorMessage = null;
////            OrchestratorResponse response = getOrchestratorResponse(orchestratorOrderId);
//
//        WorkflowExecutionStatus workflowExecutionState = orchestratorClient.getWorkflowExecutionState(executionUrl);
//
//        OrderStatus orderStatus = orderStatusFrom(workflowExecutionState);
//
//
//        if (workflowExecutionState.isFailedState()) {
//            orchestratorClient.getWorkflowExecutionLogs(executionUrl);
//        } else {
//            return Tuple.of(orderStatus, errorMessage);
//
//        }
////        }
//
//        return null;


//            if (response == null) {
//                status = OrderStatus.PROCESSING;
//            } else if (isDecommissionResponse(response)) {
//                String message = "";
//                for (Vm vm : response.getVms()) {
//                    message += comma(message) + getMessageFor(vm);
//                }
//                status = message.isEmpty() ? OrderStatus.SUCCESS : OrderStatus.FAILURE;
//                errorMessage = message.isEmpty() ? null : message;
//            }
//            else {
//                status = response.isDeploymentSuccess() ? OrderStatus.SUCCESS : OrderStatus.FAILURE;
//                errorMessage = response.getErr();
//            }
//            return Tuple.of(status, errorMessage);
//        } catch (Exception e) {
//            logger.error("Unable to retrieve order status for orchestrator order id " + orchestratorOrderId, e);
//            return Tuple.of(OrderStatus.ERROR, e.getMessage());
//        }
//    }

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
//}


    public OrchestratorClient(
            URL provisionUrl,
            URL decomissionUrl,
            URL startstopUrl,
            URL modifyUrl,
            String username,
            String password) {

        this.provisionUrl = provisionUrl;
        this.decomissionUrl = decomissionUrl;
        this.startstopUrl = startstopUrl;
        this.modifyUrl = modifyUrl;
        this.restClient = new RestClient(username, password);
    }

    public Optional<String> provision(OrchestatorRequest request) {
        return executeWorkflow(provisionUrl, request);
    }

    private Optional<String> executeWorkflow(URL orchestratorUrl, OrchestatorRequest request) {
        String xmlRequest = null;
        try {
            xmlRequest = XmlUtils.generateXml(request).replaceAll("\n", "").replaceAll("\"", "\\\\\"");
        } catch (RuntimeException je) {
            log.error("Unable to marshall xml from OrchestratorRequest");
            throw new RuntimeException(je);
        }
        log.info("Starting");

        String payload = format(ORCHESTRATOR_REQUEST_TEMPLATE, xmlRequest);
        Response response = restClient.post(orchestratorUrl.toString(), payload);
        return getRunningWorkflowUrl(response);
    }

    public WorkflowExecutionStatus getWorkflowExecutionState(String executionUrl) {
        Optional<Map> executionState = restClient.get(executionUrl + "state", Map.class);

        if (executionState.isPresent()) {
            return fromExecutionState(executionState.get().get("value").toString());
        } else {
            return WorkflowExecutionStatus.UNKNOWN;
        }
    }

    private Optional<String> getRunningWorkflowUrl(Response response) {
        List<Object> runningWorkflow = response.getHeaders().get("Location");

        if (runningWorkflow != null && runningWorkflow.size() > 0) {
            String locationUrl = runningWorkflow.get(0).toString();
            return Optional.of(locationUrl);
        }
        return Optional.empty();
    }

    public List<String> getWorkflowExecutionErrorLogs(String executionUrl) {
        Optional<OrchestratorLogPayload> executionState = restClient.get(executionUrl + "logs", OrchestratorLogPayload.class);

        List<String> severity = executionState.get().logs
                .stream()
                .filter(le -> le.entry.get("severity").equals("error"))
                .map(le -> le.entry.get("long-description"))
                .collect(toList());

        return severity;

//        if(executionState.isPresent()) {
//            return fromExecutionState(executionState.get().toString());
//        } else {
//            return WorkflowExecutionStatus.UNKNOWN;
//        }
    }

//    public void getStatus() {
//    }

//    private boolean workflowCompleted() {
//        return true;
//    }
}

class OrchestratorLogPayload {
    public List<OrchestratorLogEntry> logs;
}

class OrchestratorLogEntry {
    public Map<String, String> entry;
}