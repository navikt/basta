package no.nav.aura.basta.backend;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.util.StatusLogHelper;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;


public class FasitUpdateServiceTest {

    @Test
    public void status_test() {
        Assertions.assertTrue(OrderStatus.ERROR.isMoreImportantThan(OrderStatus.FAILURE));
        Assertions.assertTrue(OrderStatus.FAILURE.isMoreImportantThan(OrderStatus.WARNING));
        Assertions.assertTrue(OrderStatus.WARNING.isMoreImportantThan(OrderStatus.SUCCESS));
        Assertions.assertTrue(OrderStatus.SUCCESS.isMoreImportantThan(OrderStatus.PROCESSING));
        Assertions.assertTrue(OrderStatus.PROCESSING.isMoreImportantThan(OrderStatus.NEW));
    }

    @Test
    public void should_change_order_status_when_failstate() {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.addStatuslogWarning("msg");
        Assertions.assertEquals(OrderStatus.fromStatusLogLevel(StatusLogLevel.warning), order.getStatus());
    }

    @Test
    public void should_not_change_order_status_when_not_in_failstate() {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.addStatuslogInfo("msg");
        Assertions.assertTrue(order.getStatus().isMoreImportantThan(OrderStatus.fromStatusLogLevel(StatusLogLevel.info)));
    }

    @Test
    public void shouldAbbreviateException() {
        RuntimeException e = new ArrayIndexOutOfBoundsException("Jeg minner om morgendagens sommerfest.\n" +
                "Vi drar samlet fra jobb kl 1500 for å gå innom en matbutikk og ta med grillmat og drikke. Deretter tar vi trikk til jernbanetorget");
        MatcherAssert.assertThat(StatusLogHelper.abbreviateExceptionMessage(e).length(), is(160));
    }

}
