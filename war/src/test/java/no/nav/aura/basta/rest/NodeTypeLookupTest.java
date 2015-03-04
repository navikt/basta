package no.nav.aura.basta.rest;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.repository.OrderRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by j116592 on 04.03.2015.
 */
public class NodeTypeLookupTest {

    @Mock
    private OrderRepository orderRepository;
            ;

    private OrdersRestService ordersRestService;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        ordersRestService = new OrdersRestService();
        ordersRestService.setOrderRepository(orderRepository);

    }
    @Test
    public void testFindNodeTypeBasedOnHistory_simple() throws Exception {
        when(orderRepository.findRelatedOrders(anyString())).thenReturn(
            Arrays.asList(Order.newProvisionOrderUsedOnlyForTestingPurposesRefactorLaterIPromise_yeahright(NodeType.JBOSS)));
        assertThat(ordersRestService.findNodeTypeInHistory("any"),is(equalTo(NodeType.JBOSS)));
    }


    @Test
    public void testFindNodeTypeBasedOnHistory() throws Exception {
        when(orderRepository.findRelatedOrders(anyString())).thenReturn(
                Arrays.asList(Order.newProvisionOrderUsedOnlyForTestingPurposesRefactorLaterIPromise_yeahright(NodeType.JBOSS), Order.newStartOrder("any")));
        assertThat(ordersRestService.findNodeTypeInHistory("any"),is(equalTo(NodeType.JBOSS)));
    }


}
