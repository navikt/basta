package no.nav.aura.basta.rest;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
@RestController
@RequestMapping("/rest/search")
@Transactional
public class OrdersSearchRestService {
    private static final int MIN_SEARCH_QUERY_LENGTH = 3;
    private OrderRepository orderRepository;

    @Inject
    public OrdersSearchRestService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<List<OrderDO>> searchOrders(@RequestParam("q") String query) {
        validateQueryParams(query);

        final List<String> searchQueries = parseSearchQueries(query);
        List<Order> allOrders = orderRepository.getAllOrders();
        List<OrderDO> orderDos = filterOrders(allOrders, searchQueries)
                .stream()
                .sorted(Comparator.comparingLong(Order::getId).reversed())
                .map(OrderDO::new)
                .collect(toList());

        return ResponseEntity.ok()
                .header("total_count", String.valueOf(orderDos.size()))
                .body(orderDos);
    }

    private List<String> parseSearchQueries(String query) {
        return Arrays.stream(query.split(" "))
                    .map(String::toLowerCase)
                    .collect(toList());
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
                        nullSafe(order.getCreatedBy()).toLowerCase().contains(searchQuery) ||
                        order.getOrderType().toString().toLowerCase().contains(searchQuery) ||
                        order.getStatus().toString().toLowerCase().contains(searchQuery) ||
                        order.getOrderOperation().toString().toLowerCase().contains(searchQuery)).collect(toList());
    }

    private String nullSafe(String maybeNull) {
        return Optional.ofNullable(maybeNull).orElse("");
    }

    private void validateQueryParams(String searchQuery) {
        if (searchQuery == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required query parameter q");
        }

        if (searchQuery.length() < MIN_SEARCH_QUERY_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query has to contain at least " + MIN_SEARCH_QUERY_LENGTH + " characters");
        }
    }
}