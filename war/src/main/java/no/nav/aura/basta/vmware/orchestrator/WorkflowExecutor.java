package no.nav.aura.basta.vmware.orchestrator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;

import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.generated.vmware.ws.VSOWebControl;
import no.nav.generated.vmware.ws.VSOWebControlService;
import no.nav.generated.vmware.ws.Workflow;
import no.nav.generated.vmware.ws.WorkflowToken;
import no.nav.generated.vmware.ws.WorkflowTokenAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorkflowExecutor {
    private static Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);
    private static final long MAX_WAITTIME = 120 * 60 * 1000; // 2hrs, long timeout to give Orhcestrator time to finish. Should
                                                              // anyway use async mode, ie. waitForWorkflow=true

    private VSOWebControl ws;
    private String orcUsername;
    private String orcPassword;

    @Autowired
    public WorkflowExecutor(@Value("${ws.orchestrator.url}") String orcUrl, @Value("${user.orchestrator.username}") String orcUsername, @Value("${user.orchestrator.password}") String orcPassword) {
        this.orcUsername = orcUsername;
        this.orcPassword = orcPassword;

        // validate
        try {
            new URL(orcUrl);
        } catch (MalformedURLException mue) {
            throw new RuntimeException("Error resolving URL " + orcUrl, mue);
        }
        VSOWebControlService service = new VSOWebControlService(getClass().getResource("/vmware.wsdl"));
        ws = service.getWebservice();
        BindingProvider bp = (BindingProvider) ws;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, orcUrl);
    }

    /**
     * 
     * @param workflowName
     *            Name of workflow that will be called in Orchestrator
     * @param request
     *            OrchestratorRequest object containing the required parameters to provision or decommision a vApp
     * @param waitForWorkflow
     * 
     */
    public void executeWorkflow(String workflowName, OrchestatorRequest request, boolean waitForWorkflow) {
        String xmlRequest = null;
        try {
            xmlRequest = XmlUtils.generateXml(request);
        } catch (JAXBException je) {
            log.error("Unable to marshall xml from OrchestratorRequest");
            throw new RuntimeException(je);
        }
        executeWorkflow(workflowName, xmlRequest, waitForWorkflow);
    }

    /**
     * 
     * @param workflowName
     *            Name of workflow that will be called in Orchestrator
     * @param request
     *            OrchestratorRequest object containing the required parameters to provision or decommision a vApp
     * @param waitForWorkflow
     * 
     */
    public void executeWorkflow(String workflowName, String xmlRequest, boolean waitForWorkflow) {
        log.info("Starting");

        List<Workflow> workFlows = ws.getWorkflowsWithName(workflowName, this.orcUsername, this.orcPassword);
        if (workFlows.size() != 1) {
            throw new RuntimeException("Found " + workFlows.size() + " with name " + workflowName + " expected 1 ");
        }
        Workflow workflow = workFlows.get(0);
        log.info("Found workflow " + workflow.getName());

        List<WorkflowTokenAttribute> tokenAttributes = new ArrayList<WorkflowTokenAttribute>();
        WorkflowTokenAttribute attr = new WorkflowTokenAttribute();
        log.info("Setting attribute XmlRequest");
        attr.setName("XmlRequest");
        attr.setType("string");
        attr.setValue(xmlRequest);
        tokenAttributes.add(attr);

        log.info("Executing workflow " + workflowName + " with the following XML\n" + XmlUtils.prettyFormat(xmlRequest, 4));
        WorkflowToken executeResult = ws.executeWorkflow(workflow.getId(), orcUsername, orcPassword, tokenAttributes);

        // Will wait unti workflow is complete if this flag is set. Useful for tracking status in Jenkins
        if (waitForWorkflow) {
            waitForWorkflow(executeResult.getId());
            System.out.println("Workflow done, got the following response from Orchestrator");
            List<WorkflowTokenAttribute> result = ws.getWorkflowTokenResult(executeResult.getId(), orcUsername, orcPassword);

            for (WorkflowTokenAttribute wta : result) {
                String attrName = wta.getName();
                log.info("Attribute name " + attrName + " with value:");
                log.info("\n" + (attrName.equalsIgnoreCase("xmlresponse") ? XmlUtils.prettyFormat(wta.getValue(), 4) :
                        wta.getValue()));
            }
        }
        else {
            log.info("Workflow is now " + executeResult.getCurrentItemState());
        }
    }

    private void waitForWorkflow(String tokenId) {
        final long waitTime = 10 * 1000;
        long timeUsed = 0;
        while (!workflowCompleted(tokenId) && timeUsed < MAX_WAITTIME) {
            try {
                Thread.sleep(waitTime);
                timeUsed += waitTime;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (timeUsed > MAX_WAITTIME) {
            throw new RuntimeException("Workflow is not completed in " + timeUsed / 1000 + " seconds");
        }
    }

    private boolean workflowCompleted(String tokenId) {
        String status = ws.getWorkflowTokenStatus(Arrays.asList(tokenId), orcUsername, orcPassword).get(0);
        log.info(status);
        if ("completed".equalsIgnoreCase(status)) {
            return true;
        }
        if ("failed".equalsIgnoreCase(status)) {
            throw new RuntimeException("Workflow " + tokenId + "has status " + status);
        }
        return false;
    }
}