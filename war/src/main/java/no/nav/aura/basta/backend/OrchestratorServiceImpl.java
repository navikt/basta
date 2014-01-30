package no.nav.aura.basta.backend;

import java.util.List;

import no.nav.aura.basta.rest.OrderStatus;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.WorkflowExecutor;
import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.vmware.orchestrator.response.OrchestratorResponse;
import no.nav.generated.vmware.ws.WorkflowToken;
import no.nav.generated.vmware.ws.WorkflowTokenAttribute;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorServiceImpl implements OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorServiceImpl.class);

    private WorkflowExecutor workflowExecutor;

    public OrchestratorServiceImpl(WorkflowExecutor workflowExecutor) {
        this.workflowExecutor = workflowExecutor;
    }

    @Override
    public WorkflowToken send(Object request) {
        return workflowExecutor.executeWorkflow("Provision vApp - new xml and cleanup", (OrchestatorRequest) request, false);
    }

    private OrchestratorResponse getOrchestratorResponse(String orchestratorOrderId) {
        List<WorkflowTokenAttribute> status = workflowExecutor.getStatus(orchestratorOrderId);
        System.out.println("It is: " + toString(status));
        for (WorkflowTokenAttribute attribute : status) {
            if (attribute == null) {
                throw new RuntimeException("Empty response; non-existing order id?");
            } else if ("XmlResponse".equalsIgnoreCase(attribute.getName())) {
                if (attribute.getValue() == null) {
                    // Strange value that appearently means:
                    // We've received your order so we'll put this empty answer XML in the reply and then later inexplainably
                    // remove it.
                    return null;
                }
                return XmlUtils.parseXmlString(OrchestratorResponse.class, attribute.getValue());
            }
        }
        logger.info("Reply for orchestrator order id " + orchestratorOrderId + ": " + toString(status));
        return null;
    }

    @Override
    public Tuple<OrderStatus, String> getOrderStatus(String orchestratorOrderId) {
        try {
            OrderStatus status;
            String errorMessage = null;
            OrchestratorResponse response = getOrchestratorResponse(orchestratorOrderId);
            if (response == null) {
                status = OrderStatus.PROCESSING;
            } else {
                status = response.isDeploymentSuccess() ? OrderStatus.SUCCESS : OrderStatus.FAILURE;
                errorMessage = response.getErr();
            }
            return Tuple.of(status, errorMessage);
        } catch (Exception e) {
            logger.error("Unable to retrieve order status for orchestrator order id " + orchestratorOrderId, e);
            return Tuple.of(OrderStatus.ERROR, e.getMessage());
        }
    }

    public static String toString(List<WorkflowTokenAttribute> status) {
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
}
