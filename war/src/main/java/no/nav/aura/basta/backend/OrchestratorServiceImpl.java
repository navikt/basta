package no.nav.aura.basta.backend;

import java.util.UUID;

import no.nav.generated.vmware.ws.WorkflowToken;

public class OrchestratorServiceImpl implements OrchestratorService {

    @Override
    public WorkflowToken send(Object request) {
        WorkflowToken workflowToken = new WorkflowToken();
        workflowToken.setId(UUID.randomUUID().toString());
        return workflowToken;
    }

}
