package no.nav.aura.basta.backend;

import no.nav.aura.basta.rest.OrderStatus;
import no.nav.aura.basta.util.Tuple;
import no.nav.generated.vmware.ws.WorkflowToken;

public interface OrchestratorService {

    WorkflowToken send(Object request);

    Tuple<OrderStatus, String> getOrderStatus(String orchestratorOrderId);

}
