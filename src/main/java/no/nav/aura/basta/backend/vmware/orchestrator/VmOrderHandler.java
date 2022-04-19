package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import java.time.Duration;

import static java.time.ZonedDateTime.now;


@Transactional
@Component
public class VmOrderHandler {

    private static final Logger log = LoggerFactory.getLogger(VmOrderHandler.class);
    private OrderRepository orderRepository;
    private OrchestratorClient orchestratorClient;

    @Inject
    public VmOrderHandler(OrderRepository orderRepository, OrchestratorClient orchestratorClient) {
        this.orderRepository = orderRepository;
        this.orchestratorClient = orchestratorClient;
    }


    public void handleIncompleteOrder(Long orderId) {
        try {
            Order vmOrder = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Entity not " +
                    "found " + orderId));
            if (vmOrder.getExternalId() == null || vmOrder.getExternalId().equals("N/A")) {
                setOrderToErrorState(vmOrder, "No execution ID from Orchestator. Unable to track order");
                return;
            }

            if (vmOrder.getExternalId().startsWith("http")) { // This order was created from new orchestrator
                WorkflowExecutionStatus workflowExecutionState = orchestratorClient.getWorkflowExecutionState(vmOrder.getExternalId());

                if (workflowExecutionState.isFailedState()) {
                    orchestratorClient.getWorkflowExecutionErrorLogs(vmOrder.getExternalId())
                            .forEach(errorMessage -> vmOrder.addStatuslogError("Orchestrator: " + errorMessage));

                    setOrderToErrorState(vmOrder, "Orchestator execution has state " + workflowExecutionState + ", but did not update Basta");
                } else if (workflowExecutionState.isWaiting()) {
                    vmOrder.addStatuslogWarning("Orchestrator execution is in waiting state. This usually requires human intervention");
                    orchestratorClient.getWorkflowExecutionErrorLogs(vmOrder.getExternalId())
                            .stream()
                            .forEach(errorMessage -> vmOrder.addStatuslogWarning("Orchestrator: " + errorMessage));

                    orderRepository.save(vmOrder);
                }
            }

            if (orderCreatedMoreThanTwelveHoursAgo(vmOrder)) {
                setOrderToErrorState(vmOrder, "Orchestator execution has been processing for more than 12 hours. Aborting");
            }


        } catch (Exception e) {
            log.error("Error occurred during handling of incomplete VM orders", e);
        }
    }

    private void setOrderToErrorState(Order order, String message) {
        order.setStatus(OrderStatus.ERROR);
        order.setUpdatedBy("VmOrderHandler");
        order.setUpdatedByDisplayName("VmOrderHandler scheduled task");
        order.addStatuslogError(message);
        orderRepository.save(order);
    }

    private boolean orderCreatedMoreThanTwelveHoursAgo(Order vmOrder) {
        return vmOrder.getCreated().isBefore(now().minus(Duration.ofHours(12)));
    }
}

