package no.nav.aura.basta.backend;

import static no.nav.aura.basta.domain.OrderType.DB;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WaitingOrderHandler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final OrderRepository orderRepository;
    private final Logger log = LoggerFactory.getLogger(WaitingOrderHandler.class);
    private DBHandler dbHandler;

    @Inject
    public WaitingOrderHandler(OrderRepository orderRepository, DBHandler dbHandler) {
        this.orderRepository = orderRepository;
        this.dbHandler = dbHandler;
    }

    public void handle() {
        final Runnable checker = () -> {
            for (Order waitingOrder : orderRepository.findWaitingOrders()) {
                log.debug("Found waiting order with id {}", waitingOrder.getId());
                final OrderType orderType = waitingOrder.getOrderType();

                if (orderType == DB) {
                    dbHandler.handleWaiting(waitingOrder.getId());
                } else {
                    log.warn("Unable to handle order of type {}", orderType);
                }
            }
        };

        scheduler.scheduleAtFixedRate(checker, 10, 10, TimeUnit.SECONDS);
    }

}
