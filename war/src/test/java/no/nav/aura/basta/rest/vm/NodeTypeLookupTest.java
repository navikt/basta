package no.nav.aura.basta.rest.vm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.vm.HostnamesInput;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NodeTypeLookupTest {

    @Mock
    private OrderRepository orderRepository;

    private VmOperationsRestService ordersRestService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ordersRestService = new VmOperationsRestService();
        ordersRestService.setOrderRepository(orderRepository);

    }

    private Order createOrder(OrderOperation operation, NodeType nodeType) {
        Input input = null;
        if (operation == OrderOperation.CREATE) {
            VMOrderInput vminput = new VMOrderInput();
            vminput.setNodeType(nodeType);
            input = vminput;
        } else {
            HostnamesInput hostnaminput = new HostnamesInput("anyhost");
            hostnaminput.setNodeType(nodeType);
            input = hostnaminput;
        }
        return new Order(OrderType.VM, operation, input);
    }

    @Test
    public void testFindNodeTypeBasedOnHistory_simple() throws Exception {
        when(orderRepository.findRelatedOrders(anyString())).thenReturn(
                Arrays.asList(createOrder(OrderOperation.CREATE, NodeType.JBOSS)));
        assertThat(ordersRestService.findNodeTypeInHistory("any"), is(equalTo(NodeType.JBOSS)));
    }

    @Test
    public void testFindNodeTypeBasedOnHistory() throws Exception {
        when(orderRepository.findRelatedOrders(anyString())).thenReturn(
                Arrays.asList(createOrder(OrderOperation.CREATE, NodeType.JBOSS), createOrder(OrderOperation.START, NodeType.JBOSS)));
        assertThat(ordersRestService.findNodeTypeInHistory("any"), is(equalTo(NodeType.JBOSS)));
    }

    @Test
    public void singleHostShouldReturnJboss() {
        when(orderRepository.findRelatedOrders("host1")).thenReturn(
                Arrays.asList(createOrder(OrderOperation.CREATE, NodeType.JBOSS)));
        assertThat(ordersRestService.findTypeFromHistory("host1"), is(equalTo(NodeType.JBOSS)));
    }

    @Test
    public void multipleJbossNodesShouldReturnJboss() {
        when(orderRepository.findRelatedOrders("host1")).thenReturn(
                Arrays.asList(createOrder(OrderOperation.CREATE, NodeType.JBOSS)));
        when(orderRepository.findRelatedOrders("host2")).thenReturn(
                Arrays.asList(createOrder(OrderOperation.CREATE, NodeType.JBOSS)));
        assertThat(ordersRestService.findTypeFromHistory("host1", "host2"), is(equalTo(NodeType.JBOSS)));
    }

    @Test
    public void differentNodesShouldReturnJboss() {
        when(orderRepository.findRelatedOrders("host1")).thenReturn(
                Arrays.asList(createOrder(OrderOperation.CREATE, NodeType.JBOSS)));
        when(orderRepository.findRelatedOrders("host2")).thenReturn(
                Arrays.asList(createOrder(OrderOperation.CREATE, NodeType.WAS_NODES)));
        assertThat(ordersRestService.findTypeFromHistory("host1", "host2"), is(equalTo(NodeType.MULTIPLE)));
    }

    @Test
    public void noNodesinHistoryShouldReturnUnknown() {
        when(orderRepository.findRelatedOrders("host1")).thenReturn(new ArrayList<Order>());
        assertThat(ordersRestService.findTypeFromHistory("host1"), is(equalTo(NodeType.UNKNOWN)));
    }

}
