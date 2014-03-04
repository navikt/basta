package no.nav.aura.basta.persistence;

import no.nav.aura.basta.spring.SpringUnitTestConfig;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringUnitTestConfig.class)
@TransactionConfiguration
@Transactional
public class OrderRepositoryTest {

    @Inject
    private OrderRepository orderRepository;


    @Test
    public void testOrchestratorOrderIdNotNull () throws Exception{
        Order with = createOrder("1");
        Order without = createOrder(null);
        Iterable<Order> all = orderRepository.findByOrchestratorOrderIdNotNull();
        assertThat(all, contains(Matchers.hasProperty("id", equalTo(with.getId()))));

    }

    private Order createOrder(String id) {
        Order order = new Order(NodeType.APPLICATION_SERVER);
        order.setOrchestratorOrderId(id);
        return orderRepository.save(order);
    }
}
