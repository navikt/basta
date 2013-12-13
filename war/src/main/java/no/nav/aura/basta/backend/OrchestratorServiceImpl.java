package no.nav.aura.basta.backend;

import no.nav.aura.basta.vmware.orchestrator.WorkflowExecutor;
import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.generated.vmware.ws.WorkflowToken;

public class OrchestratorServiceImpl implements OrchestratorService {

    private WorkflowExecutor workflowExecutor;

    public OrchestratorServiceImpl(WorkflowExecutor workflowExecutor) {
        this.workflowExecutor = workflowExecutor;
    }

    @Override
    public WorkflowToken send(Object request) {
        return workflowExecutor.executeWorkflow("Provision vApp - new xml and cleanup", (OrchestatorRequest) request, false);
    }

}
