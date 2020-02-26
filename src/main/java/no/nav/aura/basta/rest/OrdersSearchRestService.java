package no.nav.aura.basta.rest;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
@Path("/search/")
@Transactional
public class OrdersSearchRestService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersSearchRestService.class);

    private static final int MIN_SEARCH_QUERY_LENGTH = 3;
    private OrderRepository orderRepository;

    /*@Autowired
    CacheManager cacheManager;*/

/*    @EventListener
    public void onApplicationReady() {
        logger.info("Running prewarming of orders cache in const");
        long start = System.nanoTime();
        List<Order> allOrders = orderRepository.getAllOrders();
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        logger.info("Fetched all orders " + allOrders.size() + " in " + TimeUnit.MILLISECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS) + " ms");
    }*/



    @Inject
    public OrdersSearchRestService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
//cacheManager.getCache("orders").getNativeCache().s
        /*logger.info("Running prewarming of orders cache in const");
        long start = System.nanoTime();
        List<Order> allOrders = orderRepository.getAllOrders();
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        logger.info("Fetched all orders "  + allOrders.size() + " in " + TimeUnit.MILLISECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS) + " ms");*/

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchOrders(@QueryParam("q") String query, @Context final UriInfo uriInfo) {
        validateQueryParams(query);

        final List<String> searchQueries = Arrays.stream(query.split(" "))
                .map(String::toLowerCase)
                .collect(toList());

        long start = System.nanoTime();
        List<Order> allOrders = orderRepository.getAllOrders();
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        logger.info("Fetching all orders "  + allOrders.size() + " in " + TimeUnit.MILLISECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS) + " ms");

        long startFilter = System.nanoTime();
        List<OrderDO> orderDos = filterOrders(allOrders, searchQueries)
                .stream()
                .sorted(Comparator.comparingLong(Order::getId).reversed())
                .map(order -> new OrderDO(order, uriInfo))
                .collect(toList());

        long stopFilter = System.nanoTime();
        logger.info("Filtered " + orderDos.size() + " in " + TimeUnit.MILLISECONDS.convert(stopFilter - startFilter, TimeUnit.NANOSECONDS) + " ms");

        return Response.ok(orderDos).header("total_count", orderDos.size()).build();
    }

    private List<Order> filterOrders(List<Order> orders, List<String> queryParams) {
        List<Order> filteredOrders = orders;
        for (String query : queryParams) {
            filteredOrders = filterByQueryParam(filteredOrders, query);
        }
        return filteredOrders;
    }

    private List<Order> filterByQueryParam(List<Order> orders, String searchQuery) {
        return orders.stream()
                .filter(order -> order.getId().toString().contains(searchQuery) ||
                        order.getResults().entrySet().stream().map(entry -> entry.getValue().toLowerCase()).collect(Collectors.joining(" ")).contains(searchQuery) ||
                        nullSafe(order.getResult().getDescription()).toLowerCase().contains(searchQuery) ||
                        nullSafe(order.getCreatedByDisplayName()).toLowerCase().contains(searchQuery) ||
                        order.getCreatedBy().toLowerCase().contains(searchQuery) ||
                        order.getOrderType().toString().toLowerCase().contains(searchQuery) ||
                        order.getStatus().toString().toLowerCase().contains(searchQuery) ||
                        order.getOrderOperation().toString().toLowerCase().contains(searchQuery)).collect(toList());
    }

    private String nullSafe(String maybeNull) {
        return Optional.ofNullable(maybeNull).orElse("");
    }

    private void validateQueryParams(@QueryParam("q") String searchQuery) {
        if (searchQuery == null) {
            throw new BadRequestException("Missing required query parameter q");
        }

        if (searchQuery.length() < MIN_SEARCH_QUERY_LENGTH) {
            throw new BadRequestException("Search query has to contain at least " + MIN_SEARCH_QUERY_LENGTH + " characters");
        }
    }
}