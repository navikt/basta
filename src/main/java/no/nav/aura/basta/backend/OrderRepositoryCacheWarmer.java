package no.nav.aura.basta.backend;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OrderRepositoryCacheWarmer implements   ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(OrderRepositoryCacheWarmer.class);

    @Autowired
    OrderRepository orderRepository;

    /*
    * Since the databasecall for fetching every order is really slow we cache the result. To avoid slow response times when
    * the app har recently started up, we prewarm the cache here on application startup
    * */
    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Running prewarming of orders cache on application ready");
        long start = System.nanoTime();
        List<Order> allOrders = orderRepository.getAllOrders();
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        log.info("Fetched all orders "  + allOrders.size() + " in " + TimeUnit.MILLISECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS) + " ms");
    }
}