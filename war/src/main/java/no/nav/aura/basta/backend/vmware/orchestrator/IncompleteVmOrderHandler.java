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
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private final OrderRepository orderRepository;
        private final Logger log = LoggerFactory.getLogger(IncompleteVmOrderHandler.class);
    public boolean running = false;
        private VmOrderHandler vmOrderHandler;

        @Inject
        public IncompleteVmOrderHandler(OrderRepository orderRepository, VmOrderHandler vmOrderHandler) {
            this.orderRepository = orderRepository;
            this.vmOrderHandler = vmOrderHandler;
        }

        public void handle() {
            final Runnable checker = () -> {
                for(Order incompleteVmOrder : orderRepository.findIncompleteVmOrders())  {
                    vmOrderHandler.handleIncompleteOrder(incompleteVmOrder.getId());
                }
            };

            String isMasterNode = System.getProperty("cluster.ismasternode");

            if (isMasterNode == null) {
                log.info("no system property available to know if this is master node, scheduling checks for waiting orders anyway");

                scheduler.scheduleAtFixedRate(checker, ThreadLocalRandom.current().nextInt(1, 60), 5, TimeUnit.MINUTES);

            } else if (isMasterNode.equalsIgnoreCase("true")) {
                log.info("found master node, scheduling checks for waiting orders");
                scheduler.scheduleAtFixedRate(checker, 1, 15, TimeUnit.MINUTES);
            } else {
                log.info("not master node, not scheduling checks for waiting orders here");
            }
        }
    }





