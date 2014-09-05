/*
package no.nav.aura.basta.persistence;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import no.nav.aura.basta.spring.SpringUnitTestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringUnitTestConfig.class)
@TransactionConfiguration
@Transactional
public class NodeRepositoryTest {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private NodeRepository nodeRepository;

    @Before
    public void addData() {
        createNode("a", "bya", null);
        createNode("b", "byb", orderRepository.save(new Order(NodeType.DECOMMISSIONING)));
    }

    @Test
    public void findByMaybeUserAndMaybeDecommissioned_decommissionedAndOnlyByUserA() {
        assertThat(nodeRepository.findBy("bya", true), contains(hasProperty("hostname", equalTo("a"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findByMaybeUserAndMaybeDecommissioned_decommissionedAndAnyUser() {
        assertThat(nodeRepository.findBy(null, true), containsInAnyOrder(hasProperty("hostname", equalTo("a")), hasProperty("hostname", equalTo("b"))));
    }

    @Test
    public void findByMaybeUserAndMaybeDecommissioned_notDecommissionedAndAnyUser() {
        assertThat(nodeRepository.findBy(null, false), contains(hasProperty("hostname", equalTo("a"))));
    }

    @Test
    public void findByHostnameAndDecommisionOrderIdIsNull(){
        Node n = createNode("newOne", "userA", null);
        createNode("newOne", "userB", orderRepository.save(new Order(NodeType.DECOMMISSIONING)));
        assertThat(nodeRepository.findByHostnameAndDecommissionOrderIdIsNull("newOne"), contains(hasProperty("id", equalTo(n.getId()))));
    }

    private Node createNode(String hostname, String user, Order decommissionOrder) {
        Node node = new Node();
        Order order = orderRepository.save(Order.newProvisionOrder((NodeType.APPLICATION_SERVER, settings));
        node.setNodeType(NodeType.APPLICATION_SERVER);
        node.setOrder(order);
        node.setHostname(hostname);
        node.setDecommissionOrder(decommissionOrder);
        nodeRepository.save(node);
        order.setCreatedBy(user);
        orderRepository.save(order);
        return node;
    }

}
*/
