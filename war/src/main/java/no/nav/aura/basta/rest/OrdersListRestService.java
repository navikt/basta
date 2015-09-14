package no.nav.aura.basta.rest;

import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.util.Tuple;

import org.jboss.resteasy.annotations.cache.Cache;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
@Component
@Path("/orders/")
@Transactional
public class OrdersListRestService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersListRestService.class);

    @Inject
    private OrderRepository orderRepository;

	@Inject
	private OrchestratorService orchestratorService;


    @GET
    @Path("/page/{page}/{size}/{fromdate}/{todate}")
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = 30)
    public Response getOrdersInPages(@PathParam("page") int page, @PathParam("size") int size, @PathParam("fromdate") long fromdate, @PathParam("todate") long todate, @Context final UriInfo uriInfo) {
        DateTime from = new DateTime(fromdate);
        DateTime to = new DateTime(todate);
        List<Order> set = orderRepository.findOrdersInTimespan(from, to, new PageRequest(page, size));
        if (set.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(FluentIterable.from(set).transform(new SerializableFunction<Order, OrderDO>() {
                public OrderDO process(Order order) {
                    OrderDO orderDO = new OrderDO(order, uriInfo);
                    return orderDO;
                }
            }).toList()).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@PathParam("id") long id, @Context final UriInfo uriInfo) {
        Order order = orderRepository.findOne(id);
        if (order == null || order.getExternalId() == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        OrderDO orderDO = createRichOrderDO(uriInfo, order);
        enrichOrderDOStatus(orderDO);
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

        Set<OrderStatusLog> orderStatusLogs = one.getStatusLogs();
        ImmutableList<OrderStatusLogDO> log = FluentIterable.from(orderStatusLogs).transform(new SerializableFunction<OrderStatusLog, OrderStatusLogDO>() {
            public OrderStatusLogDO process(OrderStatusLog orderStatusLog) {
                return new OrderStatusLogDO(orderStatusLog);
            }
        }).toList();
        Response response = Response.ok(log)
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

        if (order.getExternalId() != null || User.getCurrentUser().hasSuperUserAccess()) {
            orderDO.setExternalRequest(order.getExternalRequest());
        }

        return orderDO;
    }


    private List<OrderDO> getHistory(final UriInfo uriInfo, String result) {
        return FluentIterable.from(orderRepository.findRelatedOrders(result)).transform(new Function<Order, OrderDO>() {
            @Override
            public OrderDO apply(Order input) {
                return new OrderDO(input, uriInfo);
            }
        }).toList();
    }

	// TODO Fjerne denne
    protected OrderDO enrichOrderDOStatus(OrderDO orderDO) {
        if (!orderDO.getStatus().isEndstate()) {
            String orchestratorOrderId = orderDO.getExternalId();
            if (orchestratorOrderId == null) {
                orderDO.setStatus(OrderStatus.FAILURE);
                orderDO.setErrorMessage("Ordre mangler ordrenummer fra orchestrator");
            } else {
                Tuple<OrderStatus, String> tuple = orchestratorService.getOrderStatus(orchestratorOrderId);
                orderDO.setStatus(tuple.fst);
                orderDO.setErrorMessage(tuple.snd);
            }
            if (!orderDO.getStatus().isEndstate() && new DateTime(orderDO.getCreated()).isBefore(now().minus(standardHours(12)))) {
                orderDO.setStatus(OrderStatus.FAILURE);
                orderDO.setErrorMessage("Tidsavbrutt");
            }
        }
        return orderDO;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
