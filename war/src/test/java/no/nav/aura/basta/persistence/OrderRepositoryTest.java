package no.nav.aura.basta.persistence;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
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
        Iterable<Order> all = orderRepository.findByExternalIdNotNullOrderByIdDesc(new PageRequest(0, 1));
        assertThat(all, contains(Matchers.hasProperty("id", equalTo(with.getId()))));

    }

    private Order createOrder(String id) {
        Order order = Order.newProvisionOrder(NodeType.APPLICATION_SERVER);
        order.setExternalId(id);
        return orderRepository.save(order);
    }
}
