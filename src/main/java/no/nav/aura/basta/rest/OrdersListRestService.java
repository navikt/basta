package no.nav.aura.basta.rest;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import org.jboss.resteasy.annotations.cache.Cache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
@Path("/orders/")
@Transactional
public class OrdersListRestService {

    private static final int MIN_SEARCH_QUERY_LENGTH = 3;
    private OrderRepository orderRepository;

    @Inject
    public OrdersListRestService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GET
    @Path("/page/{page}/{size}")
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = 30)
    public Response getOrdersInPages(@PathParam("page") int page, @PathParam("size") int size, @Context final UriInfo uriInfo) {
        Page<Order> orders = orderRepository.findOrders(PageRequest.of(page, size));
        if (orders.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            List<OrderDO> orderDos = orders.stream().map(order -> new OrderDO(order, uriInfo)).collect(toList());
            return Response.ok(orderDos)
                    .header("total_count", orders.getTotalElements())
                    .header("page_count", orders.getTotalPages())
                    .build();
        }
    }

    @GET
    @Path("/search")
    public Response searchOrders(@QueryParam("q") String query, @Context final UriInfo uriInfo) {
        validateQueryParams(query);

        final List<String> searchQueries = Arrays.stream(query.split(" "))
                .map(String::toLowerCase)
                .collect(toList());

        long start = System.nanoTime();
        List<Order> allOrders = orderRepository.getAllOrders();
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        System.out.println("Fetching all orders in " + TimeUnit.MILLISECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS) + " ms");

        long startFilter = System.nanoTime();
        List<OrderDO> orderDos = filterOrders(allOrders, searchQueries)
                .stream()
                .map(order -> new OrderDO(order, uriInfo))
                .collect(toList());

        long stopFilter = System.nanoTime();
        System.out.println("Filtered " + orderDos.size() + " in " + TimeUnit.MILLISECONDS.convert(stopFilter - startFilter, TimeUnit.NANOSECONDS) + " ms");

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
                .filter(order -> searchQuery.contains(order.getId().toString().toLowerCase()) ||
                        order.getResults().entrySet().stream().map(entry -> entry.getValue().toLowerCase()).collect(Collectors.joining(" ")).contains(searchQuery) ||
                        order.getCreatedByDisplayName().toLowerCase().contains(searchQuery) ||
                        order.getCreatedBy().toLowerCase().contains(searchQuery) ||
                        order.getResult().getDescription().toLowerCase().contains(searchQuery) ||
                        order.getOrderType().toString().toLowerCase().contains(searchQuery) ||
                        order.getStatus().toString().toLowerCase().contains(searchQuery) ||
                        order.getOrderOperation().toString().toLowerCase().contains(searchQuery)).collect(toList());
    }

    private void validateQueryParams(@QueryParam("q") String searchQuery) {
        if (searchQuery == null) {
            throw new BadRequestException("Missing required query parameter q");
        }

        if (searchQuery.length() < MIN_SEARCH_QUERY_LENGTH) {
            throw new BadRequestException("Search query has to contain at least " + MIN_SEARCH_QUERY_LENGTH + " characters");
        }
    }


    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@PathParam("id") long id, @Context final UriInfo uriInfo) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        OrderDO orderDO = createRichOrderDO(uriInfo, order);

        Response response = Response.ok(orderDO)
                .cacheControl(noCache())
                .expires(new Date(0L))
                .build();
        return response;
    }

    @GET
    @Path("{orderid}/statuslog")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatusLog(@PathParam("orderid") long orderId, @Context final UriInfo uriInfo) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<OrderStatusLog> orderStatusLogs = order.getStatusLogs();
        List<OrderStatusLogDO> logs = orderStatusLogs.stream()
                .map(log -> new OrderStatusLogDO(log))
                .collect(toList());

        Response response = Response.ok(logs)
                .cacheControl(noCache())
                .expires(new Date(0L))
                .build();
        return response;
    }

    private CacheControl noCache() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    protected OrderDO createRichOrderDO(final UriInfo uriInfo, Order order) {
        OrderDO orderDO = new OrderDO(order, uriInfo);
        orderDO.setNextOrderId(orderRepository.findNextId(order.getId()));
        orderDO.setPreviousOrderId(orderRepository.findPreviousId(order.getId()));
        orderDO.setInput(order.getInputAs(MapOperations.class).copy());
        for (ResultDO result : order.getResult().asResultDO()) {
            result.setHistory(getHistory(uriInfo, result.getResultName()));
            orderDO.addResultHistory(result);
        }
        return orderDO;
    }

    private List<OrderDO> getHistory(final UriInfo uriInfo, String result) {
        return orderRepository.findRelatedOrders(result).stream()
                .map(order -> new OrderDO(order, uriInfo))
                .collect(toList());
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
