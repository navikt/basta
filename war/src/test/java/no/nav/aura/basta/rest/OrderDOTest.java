/*
package no.nav.aura.basta.rest;



import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.Order;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OrderDOTest {

    @Test
    public void should_findNodeTypeOfProvisionedOrder() throws Exception {
        Order order = Order.newDecommissionOrder(null);

        Node node1 = new Node();
        node1.setNodeType(NodeType.APPLICATION_SERVER);
        order.addNode(node1);
        assertThat(new OrderDO().findNodeTypeOfProvisionedOrder(order), is(NodeType.APPLICATION_SERVER));
        Node node2 = new Node();
        node2.setNodeType(NodeType.PLAIN_LINUX);
        order.addNode(node2);
        assertThat(new OrderDO().findNodeTypeOfProvisionedOrder(order), is(NodeType.MULTIPLE));
    }

}
*/
