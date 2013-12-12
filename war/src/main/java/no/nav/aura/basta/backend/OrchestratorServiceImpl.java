package no.nav.aura.basta.backend;

import java.util.UUID;

import no.nav.aura.basta.vmware.orchestrator.WorkflowExecutor;
import no.nav.generated.vmware.ws.WorkflowToken;

public class OrchestratorServiceImpl implements OrchestratorService {

    private WorkflowExecutor workflowExecutor;

    public OrchestratorServiceImpl(WorkflowExecutor workflowExecutor) {
        this.workflowExecutor = workflowExecutor;
    }

    @Override
    public WorkflowToken send(Object request) {
        // return workflowExecutor.executeWorkflow("Provision vApp - new xml", (OrchestatorRequest) request, false);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WorkflowToken workflowToken = new WorkflowToken();
        workflowToken.setId(UUID.randomUUID().toString());
        return workflowToken;
    }

}
