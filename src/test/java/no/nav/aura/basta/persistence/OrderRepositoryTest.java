package no.nav.aura.basta.persistence;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringUnitTestConfig.class)
@Rollback
@Transactional
public class OrderRepositoryTest {

    @Inject
    private OrderRepository orderRepository;


    @BeforeClass
    public static void setFasitBaseUrl(){
        System.setProperty("fasit_rest_api_url", "http://e34apsl00136.devillo.no:8080/conf");
    }


    @Test
    public void testOrchestratorOrderIdNotNull () throws Exception{
        Order with = createOrder("1");
        Order without = createOrder(null);
        Iterable<Order> all = orderRepository.findByExternalIdNotNullOrderByIdDesc(PageRequest.of(0, 1));
        assertThat(all, contains(Matchers.hasProperty("id", equalTo(with.getId()))));

    }

    private Order createOrder(String id) {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.setExternalId(id);
        return orderRepository.save(order);
    }
}
