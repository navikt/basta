package no.nav.aura.basta.backend;

import no.nav.aura.basta.vmware.orchestrator.requestv1.ProvisionRequest;
import no.nav.generated.vmware.ws.WorkflowToken;

public interface OrchestratorService {

    WorkflowToken send(ProvisionRequest request);

}
