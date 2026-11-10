package no.nav.aura.basta.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.inject.Inject;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringCacheConfig;
import no.nav.aura.basta.spring.SpringUnitTestConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class, SpringCacheConfig.class, OrderRepositoryCacheTest.CachingEnabled.class })
public class OrderRepositoryCacheTest {

    @Configuration
    @EnableCaching
    static class CachingEnabled {
    }

    @Inject
    private OrderRepository orderRepository;

    @AfterEach
    public void cleanUp() {
        orderRepository.deleteAll();
    }

    @Test
    public void cachedOrderListIsRefreshedAfterSaveAndDelete() {
        orderRepository.deleteAll();
        assertThat(orderRepository.getAllOrders(), empty());

        Order first = orderRepository.save(VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS));
        assertThat(orderRepository.getAllOrders(), hasSize(1));

        orderRepository.save(VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS));
        assertThat(orderRepository.getAllOrders(), hasSize(2));

        orderRepository.delete(first);
        assertThat(orderRepository.getAllOrders(), hasSize(1));
    }
}
