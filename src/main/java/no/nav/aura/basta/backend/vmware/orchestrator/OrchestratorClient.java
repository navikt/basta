package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
import no.nav.aura.basta.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static no.nav.aura.basta.backend.vmware.orchestrator.WorkflowExecutionStatus.fromExecutionState;

public class OrchestratorClient {
    private static Logger log = LoggerFactory.getLogger(OrchestratorClient.class);

    private final URL provisionUrl;
    private final URL decomissionUrl;
    private final URL startstopUrl;

    private final RestClient restClient;
    private final String ORCHESTRATOR_REQUEST_TEMPLATE = "{\"parameters\": [{\"name\": \"XmlRequest\",\"type\": \"string\",\"value\": {\"string\": {\"value\":\"%s\"}} }]}";

    public OrchestratorClient(
            URL provisionUrl,
            URL decomissionUrl,
            URL startstopUrl,
            String username,
            String password) {

        this.provisionUrl = provisionUrl;
        this.decomissionUrl = decomissionUrl;
        this.startstopUrl = startstopUrl;
        this.restClient = new RestClient(username, password);
    }

    public Optional<String> provision(OrchestatorRequest request) {
        return executeWorkflow(provisionUrl, request);
    }

    public Optional<String> decomission(DecomissionRequest request) {
        return executeWorkflow(decomissionUrl, request);
    }

    public Optional<String> start(StartRequest request) {
        return executeWorkflow(startstopUrl, request);
    }

    public Optional<String> stop(StopRequest stopRequest) {
        return executeWorkflow(startstopUrl, stopRequest);
    }

    private Optional<String> executeWorkflow(URL orchestratorUrl, OrchestatorRequest request) {
        String xmlRequest;
        try {
            xmlRequest = XmlUtils.generateXml(request).replaceAll("\n", "").replaceAll("\"", "\\\\\"");
        } catch (RuntimeException je) {
            log.error("Unable to marshall xml from OrchestratorRequest");
            throw new RuntimeException(je);
        }
        log.info("Calling workflow " + orchestratorUrl.toString());

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

        List<String> errorLogs = new ArrayList();

        if (executionState.isPresent()) {
            executionState.get().logs
                    .stream()
                    .filter(le -> le.entry.get("severity").equals("error"))
                    .map(le -> le.entry.get("long-description"))
                    .collect(toList());
        }
        return errorLogs;
    }
}

class OrchestratorLogPayload {
    public List<OrchestratorLogEntry> logs;
}

class OrchestratorLogEntry {
    public Map<String, String> entry;
}