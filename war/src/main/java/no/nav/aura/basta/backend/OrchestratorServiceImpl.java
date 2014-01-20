package no.nav.aura.basta.backend;

import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import no.nav.aura.basta.vmware.orchestrator.WorkflowExecutor;
import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.vmware.orchestrator.response.OrchestratorResponse;
import no.nav.generated.vmware.ws.WorkflowToken;
import no.nav.generated.vmware.ws.WorkflowTokenAttribute;

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

    @Override
    public OrchestratorResponse getStatus(String orchestratorOrderId) {
        List<WorkflowTokenAttribute> status = workflowExecutor.getStatus(orchestratorOrderId);
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
                try {
                    JAXBContext context = JAXBContext.newInstance(OrchestratorResponse.class);
                    return (OrchestratorResponse) context.createUnmarshaller().unmarshal(new StringReader(attribute.getValue()));
                } catch (JAXBException e) {
                    logger.error("Unable to parse string" + attribute.getValue());
                    throw new RuntimeException(e);
                }
            }
        }
        logger.info("Reply for orchestrator order id " + orchestratorOrderId + ": " + toString(status));
        return null;
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
                string += "Status: name = " + attr.getName() + ", type = " + attr.getType() + ", value = " + attr.getValue();
            }
        }
        return string;
    }
}
