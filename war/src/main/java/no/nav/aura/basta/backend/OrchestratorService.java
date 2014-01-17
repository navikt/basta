package no.nav.aura.basta.backend;

import no.nav.aura.basta.vmware.orchestrator.response.OrchestratorResponse;
import no.nav.generated.vmware.ws.WorkflowToken;

public interface OrchestratorService {

    WorkflowToken send(Object request);

    OrchestratorResponse getStatus(String orchestratorOrderId);

}
