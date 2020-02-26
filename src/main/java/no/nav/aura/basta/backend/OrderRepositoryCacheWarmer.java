package no.nav.aura.basta.backend;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Collections2.transform;
import static java.util.Collections.emptyMap;

@Component
public class OrderRepositoryCacheWarmer {
    private static final Logger log = LoggerFactory.getLogger(OrderRepositoryCacheWarmer.class);

    @Inject
    OrderRepositoryCacheWarmer(OrderRepository orderRepository) {
        log.info("Running prewarming of orders cache");
        long start = System.nanoTime();
        List<Order> allOrders = orderRepository.getAllOrders();
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        log.info("Fetched all orders "  + allOrders.size() + " in " + TimeUnit.MILLISECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS) + " ms");
    }

}