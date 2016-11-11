package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;

import java.util.Optional;

import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;

public abstract  class AbstractVmOrderRestService {
    protected OrderRepository orderRepository;
    protected OrchestratorService orchestratorService;


    public AbstractVmOrderRestService(OrderRepository orderRepository, OrchestratorService orchestratorService) {
        this.orderRepository = orderRepository;
        this.orchestratorService = orchestratorService;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    protected Order executeProvisonOrder(final Order order, OrchestatorRequest request) {
        order.addStatuslogInfo("Calling Orchestrator for provisioning");
        Optional<String> runningWorkflowUrl = orchestratorService.provision(request);
        runningWorkflowUrl.ifPresent(s -> order.setExternalId(s.toString()));

        if(!runningWorkflowUrl.isPresent()) {
            order.setStatus(FAILURE);
        }

        System.out.println("Setting external id to URL from orch  " + runningWorkflowUrl);
        return orderRepository.save(order);
    }
}
