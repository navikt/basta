package no.nav.aura.basta.rest.api;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.result.vm.ResultStatus;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.OrdersListRestService;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.List;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@Transactional
public class OrderListRestServiceTest  {
    private OrdersListRestService orderService;

    @Inject
    protected OrderRepository orderRepository;

    @Before
    public void createTestData() {
        System.setProperty("fasit_rest_api_url", "https://this.is.fasit.com");

        createOrder("1").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("a.devillo.no", ResultStatus.ACTIVE, NodeType.WAS_NODES);
        createOrder("2").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("b.devillo.no", ResultStatus.DECOMMISSIONED, NodeType.FLATCAR_LINUX);
        createOrder("3").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("c.devillo.no", ResultStatus.STOPPED, NodeType.JBOSS);
        createOrder("4").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("d.test.local", ResultStatus.ACTIVE, NodeType.BPM_NODES);
        createOrder("5").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("e.preprod.local", ResultStatus.ACTIVE, NodeType.LIBERTY);
        createOrder("6").getResultAs(VMOrderResult.class).addHostnameWithStatusAndNodeType("f.adeo.no", ResultStatus.ACTIVE, NodeType.PLAIN_LINUX);

        orderService = new OrdersListRestService(orderRepository);
    }

    @Test
    public void searchByHostnameReturnsCorrectOrder() {
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

    @Test(expected = BadRequestException.class)
    public void queryParamIsNullThrowsException() {
        orderService.searchOrders(null, createUriInfo());
    }

    @Test(expected = BadRequestException.class)
    public void queryParamIsEmptyThrowsException() {
        orderService.searchOrders("", createUriInfo());
    }

    @Test(expected = BadRequestException.class)
    public void queryParamIsTooShortThrowsException() {
        orderService.searchOrders("aa", createUriInfo());
    }



    private Order createOrder(String id) {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(
                NodeType.JBOSS);
        order.setExternalId(id);
        return orderRepository.save(order);
    }
}