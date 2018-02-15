package no.nav.aura.basta.backend;

import static no.nav.aura.basta.domain.OrderOperation.CREATE;
import static no.nav.aura.basta.domain.OrderOperation.DELETE;
import static no.nav.aura.basta.domain.OrderType.DB;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
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

    public boolean running = false;
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
                    if (waitingOrder.getOrderOperation() == CREATE) {
                        dbHandler.handleCreationOrder(waitingOrder.getId());
                    } else if (waitingOrder.getOrderOperation() == DELETE) {
                        dbHandler.handleDeletionOrder(waitingOrder.getId());
                    }
                } else {
                    log.warn("Unable to handle order of type {}", orderType);
                }
            }
        };

        String isMasterNode = System.getProperty("cluster.ismasternode");

        if (isMasterNode == null) {
            log.info("no system property available to know if this is master node, scheduling checks for waiting orders anyway");
            scheduler.scheduleAtFixedRate(checker, ThreadLocalRandom.current().nextInt(1, 60), 60, TimeUnit.SECONDS);
        } else if (isMasterNode.equalsIgnoreCase("true")) {
            log.info("found master node, scheduling checks for waiting orders");
            scheduler.scheduleAtFixedRate(checker, 10, 30, TimeUnit.SECONDS);
        } else {
            log.info("not master node, not scheduling checks for waiting orders here");
        }
    }
}
