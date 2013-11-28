package no.nav.aura.basta.backend;

import no.nav.generated.vmware.ws.WorkflowToken;

public interface OrchestratorService {

    WorkflowToken send(Object request);

}
