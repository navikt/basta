package no.nav.aura.basta.backend;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.net.URL;

import javax.inject.Inject;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.ResultStatus;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.envconfig.client.FasitRestClient;

import org.jboss.resteasy.spi.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@TransactionConfiguration
@Transactional
public class FasitUpdateServiceTest {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @Inject
    private FasitRestClient fasitRestClient;

    @Test
    public void removeFasitEntity() throws Exception {
        createResult("hostindb", null);
        doThrow(NotFoundException.class).when(fasitRestClient).deleteNode(Mockito.eq("hostindb"), Mockito.anyString());
        fasitUpdateService.removeFasitEntity(Order.newDecommissionOrder("hostindb"), "hostindb");

        verify(fasitRestClient, times(1)).deleteNode(Mockito.anyString(), Mockito.anyString());

    }

    @Test
    public void status_test() throws Exception {
        assertTrue(OrderStatus.ERROR.isMoreImportantThan(OrderStatus.FAILURE));
        assertTrue(OrderStatus.FAILURE.isMoreImportantThan(OrderStatus.WARNING));
        assertTrue(OrderStatus.WARNING.isMoreImportantThan(OrderStatus.SUCCESS));
        assertTrue(OrderStatus.SUCCESS.isMoreImportantThan(OrderStatus.PROCESSING));
        assertTrue(OrderStatus.PROCESSING.isMoreImportantThan(OrderStatus.NEW));
    }

    @Test
    public void should_change_order_status_when_failstate() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.APPLICATION_SERVER);
        orderRepository.save(order);
        OrderStatusLog log = new OrderStatusLog("Basta", "msg", "phase", "warning");
        fasitUpdateService.addStatus(order, log);
        assertTrue(OrderStatus.fromString(log.getStatusOption()).equals(order.getStatus()));
    }

    @Test
    public void should_not_change_order_status_when_not_in_failstate() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.APPLICATION_SERVER);
        orderRepository.save(order);
        OrderStatusLog log = new OrderStatusLog("Basta", "msg", "phase", "");
        fasitUpdateService.addStatus(order, log);
        assertTrue(order.getStatus().isMoreImportantThan(OrderStatus.fromString(log.getStatusOption())));
    }

    @Test
    public void shouldAbbreviateException() throws Exception {
        RuntimeException e = new ArrayIndexOutOfBoundsException("Jeg minner om morgendagens sommerfest.\n" +
                "Vi drar samlet fra jobb kl 1500 for å gå innom en matbutikk og ta med grillmat og drikke. Deretter tar vi trikk til jernbanetorget");
        assertThat(fasitUpdateService.abbreviateExceptionMessage(e).length(), is(160));
    }

    private void createResult(String hostname, URL fasitUrl) {


        Order order = Order.newProvisionOrder(NodeType.APPLICATION_SERVER);
        VMOrderResult result = order.getResultAs(VMOrderResult.class);
        result.addHostnameWithStatusAndNodeType(hostname, ResultStatus.ACTIVE);
        orderRepository.save(order);
    }
}
