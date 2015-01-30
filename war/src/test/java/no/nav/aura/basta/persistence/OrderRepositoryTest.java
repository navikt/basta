package no.nav.aura.basta.persistence;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeStatus;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringUnitTestConfig.class)
@TransactionConfiguration
@Transactional
public class OrderRepositoryTest {

    @Inject
    private OrderRepository orderRepository;


    @BeforeClass
    public static void setFasitBaseUrl(){
        System.setProperty("fasit.rest.api.url", "http://e34apsl00136.devillo.no:8080/conf");
    }


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




    @Test
    public void findsRelevantOrders() throws Exception {
         createOrder("1").getResultAs(VMOrderResult.class).addHostnameWithStatus("foo.devillo.no", NodeStatus.ACTIVE);
         createOrder("2").getResultAs(VMOrderResult.class).addHostnameWithStatus("foo.devillo.no", NodeStatus.STOPPED);
         createOrder("3").getResultAs(VMOrderResult.class).addHostnameWithStatus("bar.devillo.no", NodeStatus.ACTIVE);
         createOrder("4").getResultAs(VMOrderResult.class).addHostnameWithStatus("bar.devillo.no", NodeStatus.ACTIVE);
         createOrder("5").getResultAs(VMOrderResult.class).addHostnameWithStatus("bar.devillo.no", NodeStatus.ACTIVE);
         createOrder("6").getResultAs(VMOrderResult.class).addHostnameWithStatus("bar.devillo.no", NodeStatus.ACTIVE);
         createOrder("7").getResultAs(VMOrderResult.class).addHostnameWithStatus("foo.devillo.no", NodeStatus.ACTIVE);
        Order order = createOrder("8");
        order.getResultAs(VMOrderResult.class).addHostnameWithStatus("foo.devillo.no", NodeStatus.DECOMMISSIONED);

        List<Order> orders = orderRepository.findRelatedOrders(VMOrderResult.getFirstHostName(order));

        assertThat(orders, hasSize(4));
    }



}
