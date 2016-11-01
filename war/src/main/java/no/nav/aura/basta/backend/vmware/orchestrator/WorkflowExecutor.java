package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.ws.BindingProvider;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class WorkflowExecutor {
    private static Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);
    private static final long MAX_WAITTIME = 120 * 60 * 1000; // 2hrs, long timeout to give Orhcestrator time to finish. Should
    private final String workflowId;
    // anyway use async mode, ie. waitForWorkflow=true

//    private VSOWebControl ws;
    private String orcUsername;
    private String orcPassword;
    private final URL orchestratorUrl;

    @Autowired
    public WorkflowExecutor(@Value("${rest.orchestrator.url}") String orcUrl, @Value("${user.orchestrator.username}") String orcUsername, @Value("${user.orchestrator.password}") String orcPassword) {
        this.orcUsername = orcUsername;
        this.orcPassword = orcPassword;
        this.workflowId = "110abd83-455e-4aef-b141-fc4512bafec2";

        // validate
        try {
            orchestratorUrl = new URL(orcUrl + "/" + workflowId);
        } catch (MalformedURLException mue) {
            throw new RuntimeException("Error resolving URL " + orcUrl, mue);
        }
    }

    public void executeWorkflow(String workflowName, OrchestatorRequest request, boolean waitForWorkflow) {
        String xmlRequest = null;
        try {
            xmlRequest = XmlUtils.generateXml(request);
        } catch (RuntimeException je) {
            log.error("Unable to marshall xml from OrchestratorRequest");
            throw new RuntimeException(je);
        }
        executeWorkflow(xmlRequest, waitForWorkflow);
    }


    public void executeWorkflow(String xmlRequest, boolean waitForWorkflow) {
        log.info("Starting");

        // Will wait unti workflow is complete if this flag is set. Useful for tracking status in Jenkins
        if (waitForWorkflow) {
        }
    }

    public void getStatus() {
            }

    private void waitForWorkflow() {
        final long waitTime = 10 * 1000;
        long timeUsed = 0;
        while (!workflowCompleted() && timeUsed < MAX_WAITTIME) {
            try {
                Thread.sleep(waitTime);
                timeUsed += waitTime;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (timeUsed > MAX_WAITTIME) {
            throw new RuntimeException("Workflow is not completed in " + timeUsed / 1000 + " seconds");
        }
    }

    private boolean workflowCompleted() {
        return true;
    }
}