package no.nav.aura.basta.backend;

import com.google.common.collect.ImmutableMap;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class FasitUpdateServiceTest {


    @Test
    public void asdf() {
        RestClient fasitClientMock = mock(RestClient.class);

        FasitUpdateService fasitUpdateService = new FasitUpdateService(null, fasitClientMock);
        OrchestratorNodeDO orchestratorNode = new OrchestratorNodeDO();
        orchestratorNode.setHostName("hostname");
        orchestratorNode.setMiddlewareType(MiddlewareType.dockerhost);
        orchestratorNode.setDeployerPassword("hemmelig");

        VMOrderInput orderInput = new VMOrderInput(ImmutableMap.of(
                VMOrderInput.ENVIRONMENT_NAME, "dev",
                VMOrderInput.ZONE, "fss",
                VMOrderInput.CLUSTER_NAME, "the_cluster"));

        fasitUpdateService.registerNode(orchestratorNode, orderInput, new Order(OrderType.VM, OrderOperation.CREATE, Collections.emptyMap()));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(fasitClientMock).post(anyString(), argument.capture());
        String payload = argument.getValue();

        assertThat(payload, containsString("\"name\":\"the_cluster\""));
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
