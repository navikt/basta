package no.nav.aura.basta.rest.api;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.result.vm.ResultStatus;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.OrdersSearchRestService;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import no.nav.aura.basta.spring.SpringUnitTestConfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderSearchRestServiceTest {
    private OrdersSearchRestService orderService;

    @Inject
    protected OrderRepository orderRepository;

    @AfterAll
    public void tearDown() {
    	orderRepository.deleteAll();
	}
    
    @BeforeEach
    public void createTestData() {
        System.setProperty("fasit_rest_api_url", "https://this.is.fasit.com");

        createOrder("1").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("a.devillo.no", ResultStatus.ACTIVE, NodeType.WAS_NODES);
        createOrder("2").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("b.devillo.no", ResultStatus.DECOMMISSIONED, NodeType.FLATCAR_LINUX);
        createOrder("3").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("c.devillo.no", ResultStatus.STOPPED, NodeType.JBOSS);
        createOrder("4").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("d.test.local", ResultStatus.ACTIVE, NodeType.BPM_NODES);
        createOrder("5").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("f.adeo.no", ResultStatus.ACTIVE, NodeType.PLAIN_LINUX);

        orderService = new OrdersSearchRestService(orderRepository);
    }

    @Test
    public void ordersAreSortedDescendingByOrderId() {
    	ResponseEntity<List<OrderDO>> response = orderService.searchOrders("devillo.no");
        List<OrderDO> orders = response.getBody();

        assertThat(orders.size(), is(3));
        assertTrue(orders.get(0).getResults().contains("c.devillo.no"));
        assertTrue(orders.get(1).getResults().contains("b.devillo.no"));
        assertTrue(orders.get(2).getResults().contains("a.devillo.no"));
    }

    @Test
    public void searchByHostname() {
    	ResponseEntity<List<OrderDO>> response = orderService.searchOrders("b.devillo.no");
        List<OrderDO> orders = response.getBody();
        assertThat(orders.size(), is(1));
        assertTrue(orders.get(0).getResults().contains("b.devillo.no"));
    }

    @Test
    public void  searchByNodeType() {
    	ResponseEntity<List<OrderDO>> response = orderService.searchOrders("jboss");
        List<OrderDO> orders = response.getBody();

        assertThat(orders.size(), is(1));
        assertTrue(orders.get(0).getResults().contains("c.devillo.no"));
    }

    @Test
    public void  searchByStatus() {
    	ResponseEntity<List<OrderDO>> response = orderService.searchOrders("acti");
        List<OrderDO> orders = response.getBody();

        assertThat(orders.size(), is(3));
        assertThat(response.getHeaders().getFirst("Total_count"), is("3"));
    }

    @Test
    public void  seachByPartsOfHostname() {
    	ResponseEntity<List<OrderDO>> response = orderService.searchOrders("devillo");
        List<OrderDO> orders = response.getBody();

        assertThat(orders.size(), is(3));
        assertThat(response.getHeaders().getFirst("Total_count"), is("3"));
    }

    @Test
    public void seachByMultipleSearchWord() {
    	ResponseEntity<List<OrderDO>> response = orderService.searchOrders("active devillo was");
        List<OrderDO> orders = response.getBody();

        assertThat(orders.size(), is(1));
        assertThat(response.getHeaders().getFirst("Total_count"), is("1"));
    }

    @Test
    public void noMatchReturnEmptyList() {
    	ResponseEntity<List<OrderDO>> response = orderService.searchOrders("gibberish");
        List<OrderDO> orders = response.getBody();

        assertThat(orders.size(), is(0));
        assertThat(response.getHeaders().getFirst("Total_count"), is("0"));
    }

    @Test
    public void queryParamIsNullThrowsException() {
        assertThrows(RuntimeException.class, () -> orderService.searchOrders(null));
    }

    @Test
    public void queryParamIsEmptyThrowsException() {
        assertThrows(RuntimeException.class, () -> orderService.searchOrders(""));
    }

    @Test
    public void queryParamIsTooShortThrowsException() {
        assertThrows(RuntimeException.class, () -> orderService.searchOrders("aa"));
    }



    private Order createOrder(String id) {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(
                NodeType.JBOSS);
        order.setExternalId(id);
        return orderRepository.save(order);
    }
}