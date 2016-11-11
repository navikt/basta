package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class IncompleteVmOrderHandler {
        public boolean running = false;
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private final OrderRepository orderRepository;
        private final Logger log = LoggerFactory.getLogger(IncompleteVmOrderHandler.class);
        private VmOrderHandler vmOrderHandler;

        @Inject
        public IncompleteVmOrderHandler(OrderRepository orderRepository, VmOrderHandler vmOrderHandler) {
            this.orderRepository = orderRepository;
            this.vmOrderHandler = vmOrderHandler;
        }

        public void handle() {
            final Runnable checker = () -> {

                System.out.println("****************** Running vm order handler");

                for(Order incompleteVmOrder : orderRepository.findIncompleteVmOrders())  {
                    System.out.println("Found incomplete order " + incompleteVmOrder.getId() + " " + incompleteVmOrder.getStatus());
                    System.out.println("External id " + incompleteVmOrder.getExternalId());
                    vmOrderHandler.handleIncompleteOrder(incompleteVmOrder.getId());
                }
//                for (Order waitingOrder : orderRepository.findWaitingOrders()) {
//                    log.debug("Found waiting order with id {}", waitingOrder.getId());
//                    final OrderType orderType = waitingOrder.getOrderType();
//
//                    if (orderType == DB) {
//                        if (waitingOrder.getOrderOperation() == CREATE) {
//                            dbHandler.handleCreationOrder(waitingOrder.getId());
//                        } else if (waitingOrder.getOrderOperation() == DELETE) {
//                            dbHandler.handleDeletionOrder(waitingOrder.getId());
//                        }
//                    } else {
//                        log.warn("Unable to handle order of type {}", orderType);
//                    }
//                }
            };

            String isMasterNode = System.getProperty("cluster.ismasternode");

            if (isMasterNode == null) {
                log.info("no system property available to know if this is master node, scheduling checks for waiting orders anyway");
                scheduler.scheduleAtFixedRate(checker, ThreadLocalRandom.current().nextInt(0, 1), 15, TimeUnit.MINUTES);
            } else if (isMasterNode.equalsIgnoreCase("true")) {
                log.info("found master node, scheduling checks for waiting orders");
                scheduler.scheduleAtFixedRate(checker, 1, 15, TimeUnit.MINUTES);
            } else {
                log.info("not master node, not scheduling checks for waiting orders here");
            }
        }
    }





