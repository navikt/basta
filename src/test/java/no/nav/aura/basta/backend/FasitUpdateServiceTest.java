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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FasitUpdateServiceTest {

    @Test
    public void status_test() {
        assertTrue(OrderStatus.ERROR.isMoreImportantThan(OrderStatus.FAILURE));
        assertTrue(OrderStatus.FAILURE.isMoreImportantThan(OrderStatus.WARNING));
        assertTrue(OrderStatus.WARNING.isMoreImportantThan(OrderStatus.SUCCESS));
        assertTrue(OrderStatus.SUCCESS.isMoreImportantThan(OrderStatus.PROCESSING));
        assertTrue(OrderStatus.PROCESSING.isMoreImportantThan(OrderStatus.NEW));
    }

    @Test
    public void should_change_order_status_when_failstate() {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.addStatuslogWarning("msg");
        assertEquals(OrderStatus.fromStatusLogLevel(StatusLogLevel.warning), order.getStatus());
    }

    @Test
    public void should_not_change_order_status_when_not_in_failstate() {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.addStatuslogInfo("msg");
        assertTrue(order.getStatus().isMoreImportantThan(OrderStatus.fromStatusLogLevel(StatusLogLevel.info)));
    }

    @Test
    public void shouldAbbreviateException() {
        RuntimeException e = new ArrayIndexOutOfBoundsException("Jeg minner om morgendagens sommerfest.\n" +
                "Vi drar samlet fra jobb kl 1500 for å gå innom en matbutikk og ta med grillmat og drikke. Deretter tar vi trikk til jernbanetorget");
        assertThat(StatusLogHelper.abbreviateExceptionMessage(e).length(), is(160));
    }

}
