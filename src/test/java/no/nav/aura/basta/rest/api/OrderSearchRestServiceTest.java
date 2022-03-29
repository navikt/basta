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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.List;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@Transactional
public class OrderSearchRestServiceTest {
    private OrdersSearchRestService orderService;

    @Inject
    protected OrderRepository orderRepository;

    @BeforeEach
    public void createTestData() {
        System.setProperty("fasit_rest_api_url", "https://this.is.fasit.com");

        createOrder("1").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("a.devillo.no", ResultStatus.ACTIVE, NodeType.WAS_NODES);
        createOrder("2").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("b.devillo.no", ResultStatus.DECOMMISSIONED, NodeType.FLATCAR_LINUX);
        createOrder("3").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("c.devillo.no", ResultStatus.STOPPED, NodeType.JBOSS);
        createOrder("4").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("d.test.local", ResultStatus.ACTIVE, NodeType.BPM_NODES);
        createOrder("5").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("e.preprod.local", ResultStatus.ACTIVE, NodeType.LIBERTY);
        createOrder("6").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("f.adeo.no", ResultStatus.ACTIVE, NodeType.PLAIN_LINUX);

        orderService = new OrdersSearchRestService(orderRepository);
    }

    @Test
    public void ordersAreSortedDescendingByOrderId() {
        Response response = orderService.searchOrders("devillo.no", createUriInfo());
        List<OrderDO> orders = (List<OrderDO>) response.getEntity();

        assertThat(orders.size(), is(3));
        assertTrue(orders.get(0).getResults().contains("c.devillo.no"));
        assertTrue(orders.get(1).getResults().contains("b.devillo.no"));
        assertTrue(orders.get(2).getResults().contains("a.devillo.no"));
    }

    @Test
    public void searchByHostname() {
        Response response = orderService.searchOrders("b.devillo.no", createUriInfo());
        List<OrderDO> orders = (List<OrderDO>) response.getEntity();

        assertThat(orders.size(), is(1));
        assertTrue(orders.get(0).getResults().contains("b.devillo.no"));
    }

    @Test
    public void  searchByNodeType() {
        Response response = orderService.searchOrders("jboss", createUriInfo());
        List<OrderDO> orders = (List<OrderDO>) response.getEntity();

        assertThat(orders.size(), is(1));
        assertTrue(orders.get(0).getResults().contains("c.devillo.no"));
    }

    @Test
    public void  searchByStatus() {
        Response response = orderService.searchOrders("acti", createUriInfo());
        List<OrderDO> orders = (List<OrderDO>) response.getEntity();

        assertThat(orders.size(), is(4));
        assertThat(response.getHeaderString("Total_count"), is("4"));
    }

    @Test
    public void  seachByPartsOfHostname() {
        Response response = orderService.searchOrders("devillo", createUriInfo());
        List<OrderDO> orders = (List<OrderDO>) response.getEntity();

        assertThat(orders.size(), is(3));
        assertThat(response.getHeaderString("Total_count"), is("3"));
    }

    @Test
    public void seachByMultipleSearchWord() {
        Response response = orderService.searchOrders("active devillo was", createUriInfo());
        List<OrderDO> orders = (List<OrderDO>) response.getEntity();

        assertThat(orders.size(), is(1));
        assertThat(response.getHeaderString("Total_count"), is("1"));
    }

    @Test
    public void noMatchReturnEmptyList() {
        Response response = orderService.searchOrders("gibberish", createUriInfo());
        List<OrderDO> orders = (List<OrderDO>) response.getEntity();

        assertThat(orders.size(), is(0));
        assertThat(response.getHeaderString("Total_count"), is("0"));
    }

    @Test
    public void queryParamIsNullThrowsException() {
        assertThrows(BadRequestException.class, () -> {
            orderService.searchOrders(null, createUriInfo());
        });
    }

    @Test
    public void queryParamIsEmptyThrowsException() {
        assertThrows(BadRequestException.class, () -> {
            orderService.searchOrders("", createUriInfo());
        });
    }

    @Test
    public void queryParamIsTooShortThrowsException() {
        assertThrows(BadRequestException.class, () -> {
            orderService.searchOrders("aa", createUriInfo());
        });
    }



    private Order createOrder(String id) {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(
                NodeType.JBOSS);
        order.setExternalId(id);
        return orderRepository.save(order);
    }
}