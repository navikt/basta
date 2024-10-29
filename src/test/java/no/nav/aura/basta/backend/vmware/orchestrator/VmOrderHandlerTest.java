package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import org.hamcrest.MatcherAssert;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringUnitTestConfig.class})
@Transactional
@Rollback
public class VmOrderHandlerTest {

    @Inject
    OrchestratorClient orchestratorClient;

    private VmOrderHandler vmOrderHandler;

    @Inject
    private OrderRepository orderRepository;


    private static String getStatusLogs(Order order) {
        return order.getStatusLogs().
                stream().
                map(OrderStatusLog::getStatusText).collect(Collectors.joining("\n"));

    }

    private static List<String> errorLogs() {
        List<String> errorLogs = new ArrayList<>();
        errorLogs.add("Error from Orchestrator");
        return errorLogs;
    }

    @BeforeEach
    public void setup() {
        this.vmOrderHandler = new VmOrderHandler(orderRepository, orchestratorClient);
    }

    @Test
    public void orderGetsErrorStateIfNoOrchestratorExecutionIdIsFound() {
        final Order order = createOrder("N/A");

        vmOrderHandler.handleIncompleteOrder(order.getId());

        Order failedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId()));
        MatcherAssert.assertThat(failedOrder.getStatus(), is(OrderStatus.ERROR));
    }

    @Test
    public void orderGetsErrorStateIfWorkflowExecutionIsInFailedState() {
        final Order order = createOrder("http://some.orchestrator.externalid");
        when(orchestratorClient.getWorkflowExecutionState(anyString())).thenReturn(WorkflowExecutionStatus.FAILED);
        when(orchestratorClient.getWorkflowExecutionErrorLogs(anyString())).thenReturn(errorLogs());

        vmOrderHandler.handleIncompleteOrder(order.getId());

        Order failedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId()));
        MatcherAssert.assertThat(failedOrder.getStatus(), is(OrderStatus.ERROR));
        MatcherAssert.assertThat(getStatusLogs(failedOrder), containsString("Error from Orchestrator"));
        MatcherAssert.assertThat(failedOrder.getUpdatedBy(), is("VmOrderHandler"));
    }

    @Test
    public void whenOrderIsInWaitingStateOnOrchestratorStatusIsLoggedAndOrderStatusIsUnchanged() {
        final Order order = createOrder("http://some.orchestrator.externalid");

        when(orchestratorClient.getWorkflowExecutionState(anyString())).thenReturn(WorkflowExecutionStatus.WAITING);
        when(orchestratorClient.getWorkflowExecutionErrorLogs(anyString())).thenReturn(errorLogs());

        vmOrderHandler.handleIncompleteOrder(order.getId());

        Order waitingOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId()));
        MatcherAssert.assertThat(waitingOrder.getStatus(), is(OrderStatus.WARNING));
        MatcherAssert.assertThat(getStatusLogs(waitingOrder), containsString("Orchestrator execution is in waiting state"));
    }

    @Test
    public void orderGetsErrorStateWhenOrderIsNotCompleteAndItsMoreThanTwelveHoursSinceOrderWasCreated() {
        final Order order = createOrder("http://some.orchestrator.externalid");
        order.setCreated(new DateTime(now().minus(standardHours(13))));
        orderRepository.save(order);

        when(orchestratorClient.getWorkflowExecutionState(anyString())).thenReturn(WorkflowExecutionStatus.RUNNING);

        vmOrderHandler.handleIncompleteOrder(order.getId());

        Order timedOutOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId()));
        MatcherAssert.assertThat(timedOutOrder.getStatus(), is(OrderStatus.ERROR));
        MatcherAssert.assertThat(getStatusLogs(timedOutOrder), containsString("Orchestator execution has been processing for more than 12 hours. Aborting"));
    }

    private Order createOrder(String externalId) {
        final Map input = new HashMap<>();
        final Order order = new Order(OrderType.VM, OrderOperation.CREATE, input);
        order.setExternalId(externalId);
        return orderRepository.save(order);
    }
}

