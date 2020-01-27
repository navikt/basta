package no.nav.aura.basta.backend;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.util.StatusLogHelper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class FasitUpdateServiceTest {

    /*
    * TODO trengs denne?
    * */
    /*@Test
    public void createsNodeInFasit() {
        RestClient fasitClientMock = mock(RestClient.class);

        FasitUpdateService fasitUpdateService = new FasitUpdateService(null, fasitClientMock);
        OrchestratorNodeDO orchestratorNode = new OrchestratorNodeDO();
        orchestratorNode.setHostName("hostname");
        orchestratorNode.setDeployerPassword("hemmelig");

        Map<String, String> orderInputValues = new HashMap<>();

        orderInputValues.put(VMOrderInput.ENVIRONMENT_NAME, "dev");
        orderInputValues.put(VMOrderInput.ZONE, "fss");
        orderInputValues.put(VMOrderInput.CLUSTER_NAME, "the_cluster");
        orderInputValues.put(VMOrderInput.NODE_TYPE, NodeType.JBOSS.name());

        VMOrderInput orderInput = new VMOrderInput(orderInputValues);

        fasitUpdateService.registerNode(orchestratorNode, orderInput, new Order(OrderType.VM, OrderOperation.CREATE, Collections.emptyMap()));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(fasitClientMock).post(nullable(String.class), argument.capture());
        String payload = argument.getValue();
        assertThat(payload, containsString("\"name\":\"the_cluster\""));
        assertThat(payload, containsString("\"type\":\"docker\""));
        assertThat(payload, containsString("\"value\":\"hemmelig\""));

    }*/

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
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.addStatuslogWarning("msg");
        assertTrue(OrderStatus.fromStatusLogLevel(StatusLogLevel.warning).equals(order.getStatus()));
    }

    @Test
    public void should_not_change_order_status_when_not_in_failstate() throws Exception {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.addStatuslogInfo("msg");
        assertTrue(order.getStatus().isMoreImportantThan(OrderStatus.fromStatusLogLevel(StatusLogLevel.info)));
    }

    @Test
    public void shouldAbbreviateException() throws Exception {
        RuntimeException e = new ArrayIndexOutOfBoundsException("Jeg minner om morgendagens sommerfest.\n" +
                "Vi drar samlet fra jobb kl 1500 for å gå innom en matbutikk og ta med grillmat og drikke. Deretter tar vi trikk til jernbanetorget");
        assertThat(StatusLogHelper.abbreviateExceptionMessage(e).length(), is(160));
    }

}
