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

@Component
public class OrchestratorClient {
    private static Logger log = LoggerFactory.getLogger(OrchestratorClient.class);
    private final RestClient restClient;
    private final String ORCHESTRATOR_REQUEST_TEMPLATE = "{\"parameters\": [{\"name\": \"XmlRequest\",\"type\": \"string\",\"value\": {\"string\": {\"value\":\"%s\"}} }]}";

    @Autowired
    public OrchestratorClient(@Value("${user.orchestrator.username}") String username, @Value("${user.orchestrator.password}") String password) {
        this.restClient = new RestClient(username, password);
    }

    public Optional<String> executeWorkflow(URL orchestratorUrl, OrchestatorRequest request) {
        String xmlRequest = null;
        try {
            xmlRequest = XmlUtils.generateXml(request).replaceAll("\n", "").replaceAll("\"", "\\\\\"");
        } catch (RuntimeException je) {
            log.error("Unable to marshall xml from OrchestratorRequest");
            throw new RuntimeException(je);
        }
        log.info("Starting");

        String payload = format(ORCHESTRATOR_REQUEST_TEMPLATE, xmlRequest);
        System.out.println("xmlRequest = " + xmlRequest);
        Response response = restClient.post(orchestratorUrl.toString(), payload);
        System.out.println("Location = " + response.getHeaders().get("Location"));
        return getRunningWorkflowUrl(response);
    }

    public WorkflowExecutionStatus getWorkflowExecutionState(String executionUrl) {
        Optional<Map> executionState = restClient.get(executionUrl + "state", Map.class);

        if(executionState.isPresent()) {
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