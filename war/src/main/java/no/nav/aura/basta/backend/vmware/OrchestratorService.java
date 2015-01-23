package no.nav.aura.basta.backend.vmware;

import no.nav.aura.basta.rest.OrderStatus;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
import no.nav.generated.vmware.ws.WorkflowToken;

public interface OrchestratorService {

    WorkflowToken send(Object request);

    Tuple<OrderStatus, String> getOrderStatus(String orchestratorOrderId);

    WorkflowToken decommission(DecomissionRequest decomissionRequest);

    WorkflowToken stop(StopRequest stopRequest);

    WorkflowToken start(StartRequest startRequest);

}
