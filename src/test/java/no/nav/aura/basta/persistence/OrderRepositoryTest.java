package no.nav.aura.basta.persistence;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringUnitTestConfig.class)
@Rollback
@Transactional
public class OrderRepositoryTest {

    @Inject
    private OrderRepository orderRepository;


    @BeforeAll
    public static void setFasitBaseUrl(){
        System.setProperty("fasit_rest_api_url", "http://e34apsl00136.devillo.no:8080/conf");
    }


    @Test
    public void testOrchestratorOrderIdNotNull () {
        Order with = createOrder();
        Iterable<Order> all = orderRepository.findByExternalIdNotNullOrderByIdDesc(PageRequest.of(0, 1));
        MatcherAssert.assertThat(all, contains(Matchers.hasProperty("id", equalTo(with.getId()))));

    }

    private Order createOrder() {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.setExternalId("1");
        return orderRepository.save(order);
    }
}
