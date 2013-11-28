package no.nav.aura.basta.backend;

import java.util.UUID;

import no.nav.generated.vmware.ws.WorkflowToken;

import org.springframework.stereotype.Component;

@Component
public class OrchestratorServiceMock implements OrchestratorService {

    @Override
    public WorkflowToken send(Object request) {
        WorkflowToken workflowToken = new WorkflowToken();
        workflowToken.setId(UUID.randomUUID().toString());
        return workflowToken;
    }

}
