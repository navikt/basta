package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
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
                map(log -> log.getStatusText()).collect(Collectors.joining("\n"));

    }

    private static List<String> errorLogs() {
        List<String> errorLogs = new ArrayList<>();
        errorLogs.add("Error from Orchestrator");
        return errorLogs;
    }

    @Before
    public void setup() {
        this.vmOrderHandler = new VmOrderHandler(orderRepository, orchestratorClient);
        VmOrderHandler vmOrderHandler = this.vmOrderHandler;
    }

    @Test
    public void orderGetsErrorStateIfNoOrchestratorExecutionIdIsFound() {
        final Order order = createOrder("N/A");

        vmOrderHandler.handleIncompleteOrder(order.getId());

        Order failedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId()));
        assertThat(failedOrder.getStatus(), is(OrderStatus.ERROR));
    }

    @Test
    public void orderGetsErrorStateIfWorkflowExecutionIsInFailedState() {
        final Order order = createOrder("http://some.orchestrator.externalid");
        when(orchestratorClient.getWorkflowExecutionState(anyString())).thenReturn(WorkflowExecutionStatus.FAILED);
        when(orchestratorClient.getWorkflowExecutionErrorLogs(anyString())).thenReturn(errorLogs());

        vmOrderHandler.handleIncompleteOrder(order.getId());

        Order failedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId()));
        assertThat(failedOrder.getStatus(), is(OrderStatus.ERROR));
        assertThat(getStatusLogs(failedOrder), containsString("Error from Orchestrator"));
        assertThat(failedOrder.getUpdatedBy(), is("VmOrderHandler"));
    }

    @Test
    public void whenOrderIsInWaitingStateOnOrchestratorStatusIsLoggedAndOrderStatusIsUnchanged() {
        final Order order = createOrder("http://some.orchestrator.externalid");

        when(orchestratorClient.getWorkflowExecutionState(anyString())).thenReturn(WorkflowExecutionStatus.WAITING);
        when(orchestratorClient.getWorkflowExecutionErrorLogs(anyString())).thenReturn(errorLogs());

        vmOrderHandler.handleIncompleteOrder(order.getId());

        Order waitingOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId()));
        assertThat(waitingOrder.getStatus(), is(OrderStatus.WARNING));
        assertThat(getStatusLogs(waitingOrder), containsString("Orchestrator execution is in waiting state"));
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
        assertThat(timedOutOrder.getStatus(), is(OrderStatus.ERROR));
        assertThat(getStatusLogs(timedOutOrder), containsString("Orchestator execution has been processing for more than 12 hours. Aborting"));
    }

    private Order createOrder(String externalId) {
        final Map input = new HashMap<>();
        final Order order = new Order(OrderType.VM, OrderOperation.CREATE, input);
        order.setExternalId(externalId);
        return orderRepository.save(order);
    }
}

