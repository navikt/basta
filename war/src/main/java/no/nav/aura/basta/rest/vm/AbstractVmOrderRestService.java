package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;

import java.util.Optional;

import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;

public abstract  class AbstractVmOrderRestService {
    protected OrderRepository orderRepository;
    private OrchestratorClient orchestratorClient;


    public AbstractVmOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient) {
        this.orderRepository = orderRepository;
        this.orchestratorClient = orchestratorClient;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    protected Order executeProvisonOrder(final Order order, OrchestatorRequest request) {
        order.addStatuslogInfo("Calling Orchestrator for provisioning");
        Optional<String> runningWorkflowUrl = orchestratorClient.provision(request);
        runningWorkflowUrl.ifPresent(s -> order.setExternalId(s.toString()));

        if(!runningWorkflowUrl.isPresent()) {
            order.setStatus(FAILURE);
        }

        return orderRepository.save(order);
    }
}
