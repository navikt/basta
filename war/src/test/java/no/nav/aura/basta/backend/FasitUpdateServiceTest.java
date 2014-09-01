package no.nav.aura.basta.backend;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URL;

import javax.inject.Inject;

import no.nav.aura.basta.persistence.*;

import no.nav.aura.basta.rest.OrderStatus;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
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
    private NodeRepository nodeRepository;

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @Inject
    private FasitRestClient fasitRestClient;

    @Test
    public void removeFasitEntity() throws Exception {
        createHost("hostindb", null);
        doThrow(NotFoundException.class).when(fasitRestClient).delete(Mockito.eq("hostindb"), Mockito.anyString());
        createHost("hostinfasit", new URL("http://delete.me"));
        doNothing().when(fasitRestClient).delete(Mockito.eq("hostinfasit"), Mockito.anyString());
        createHost("removedhost", new URL("http://crash.on.me"));
        doThrow(NotFoundException.class).when(fasitRestClient).delete(Mockito.eq("removedhost"), Mockito.anyString());

        fasitUpdateService.removeFasitEntity(Order.newDecommissionOrder("hostindb", "removedhost", "hostinfasit"), ", hostindb, removedhost, hostinfasit, ,  ");
        verify(fasitRestClient, times(3)).delete(Mockito.anyString(), Mockito.anyString());

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
        assertThat(fasitUpdateService.abbreviateExceptionMessage(e).length(),is(160));
    }


    private Node createHost(String hostname, URL fasitUrl) {
        Node hostInFasit = new Node(Order.newProvisionOrder(NodeType.APPLICATION_SERVER), NodeType.APPLICATION_SERVER, hostname, null, 1, 1024, null, MiddleWareType.jb, null);
        hostInFasit.setFasitUrl(fasitUrl);
        nodeRepository.save(hostInFasit);
        return hostInFasit;
    }
}
