package no.nav.aura.basta.rest;

import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import org.jboss.resteasy.annotations.cache.Cache;
import org.joda.time.DateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/orders/")
@Transactional
public class OrdersListRestService {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private OrchestratorClient orchestratorClient;

    @GET
    @Path("/page/{page}/{size}/{fromdate}/{todate}")
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = 30)
    public Response getOrdersInPages(@PathParam("page") int page, @PathParam("size") int size, @PathParam("fromdate") long fromdate, @PathParam("todate") long todate, @Context final UriInfo uriInfo) {
        DateTime from = new DateTime(fromdate);
        DateTime to = new DateTime(todate);
        List<Order> orders = orderRepository.findOrdersInTimespan(from, to, new PageRequest(page, size));
        if (orders.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            List<OrderDO> orderDos = orders.stream().map(order -> new OrderDO(order, uriInfo)).collect(Collectors.toList());
            return Response.ok(orderDos).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@PathParam("id") long id, @Context final UriInfo uriInfo) {
        Order order = orderRepository.findOne(id);
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
        Order one = orderRepository.findOne(orderId);
        if (one == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<OrderStatusLog> orderStatusLogs = one.getStatusLogs();
        List<OrderStatusLogDO> logs = orderStatusLogs.stream()
                .map(log -> new OrderStatusLogDO(log))
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}