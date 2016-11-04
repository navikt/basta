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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static java.lang.String.*;

@Component
public class WorkflowExecutor {
    private static Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);
    private final String username;
    private final String password;
    private final RestClient restClient;
    private final String ORCHESTRATOR_REQUEST_TEMPLATE = "{\"parameters\": [{\"name\": \"XmlRequest\",\"type\": \"string\",\"value\": {\"string\": {\"value\":\"%s\"}} }]}";

    @Autowired
    public WorkflowExecutor(@Value("${user.orchestrator.username}") String username, @Value("${user.orchestrator.password}") String password) {
        this.restClient = new RestClient(username, password);
        this.username = username;
        this.password = password;
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

    public Optional<String> getRunningWorkflowUrl(Response response) {
        List<Object> runningWorkflow = response.getHeaders().get("Location");

        if (runningWorkflow != null && runningWorkflow.size() > 0) {
            String locationUrl = runningWorkflow.get(0).toString();
            return Optional.of(locationUrl);
        }
        return Optional.empty();
    }

    public void getStatus() {
    }

    private boolean workflowCompleted() {
        return true;
    }
}