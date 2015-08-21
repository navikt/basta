package no.nav.aura.basta.util;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.OrderStatus;

import org.apache.commons.lang.StringUtils;

public class StatusLogHelper {

    public static String abbreviateExceptionMessage(Exception e) {
        if (e.getMessage() != null && e.getMessage().length() > 3) {
            return ": " + StringUtils.abbreviate(e.getMessage(), 158);
        }
        return e.getMessage();
    }

    public static void addStatusLog(Order order, OrderStatusLog log) {
        order.addStatusLog(log);
        order.setStatusIfMoreImportant(OrderStatus.fromStatusLogLevel(log.getStatusOption()));
    }
}
