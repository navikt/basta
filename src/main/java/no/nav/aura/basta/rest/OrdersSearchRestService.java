package no.nav.aura.basta.rest;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
@Path("/search/")
@Transactional
public class OrdersSearchRestService {
    private static final int MIN_SEARCH_QUERY_LENGTH = 3;
    
    @Inject
    private OrderRepository orderRepository;

    public OrdersSearchRestService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchOrders(@QueryParam("q") String query, @Context final UriInfo uriInfo) {
        validateQueryParams(query);

        final List<String> searchQueries = parseSearchQueries(query);
        List<Order> allOrders = orderRepository.getAllOrders();
        List<OrderDO> orderDos = filterOrders(allOrders, searchQueries)
                .stream()
                .sorted(Comparator.comparingLong(Order::getId).reversed())
                .map(order -> new OrderDO(order, uriInfo))
                .collect(toList());

        return Response.ok(orderDos).header("total_count", orderDos.size()).build();
    }

    private List<String> parseSearchQueries(@QueryParam("q") String query) {
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

    private void validateQueryParams(@QueryParam("q") String searchQuery) {
        if (searchQuery == null) {
            throw new BadRequestException("Missing required query parameter q");
        }

        if (searchQuery.length() < MIN_SEARCH_QUERY_LENGTH) {
            throw new BadRequestException("Search query has to contain at least " + MIN_SEARCH_QUERY_LENGTH + " characters");
        }
    }
}