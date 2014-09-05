package no.nav.aura.basta.rest;

import static no.nav.aura.basta.rest.UriFactory.createOrderUri;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV2Factory;
import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.NodeStatus;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.persistence.OrderStatusLog;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.resteasy.annotations.cache.Cache;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXParseException;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
@Component
@Path("/orders")
@Transactional
public class OrdersRestService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersRestService.class);

    @Inject
    private OrderRepository orderRepository;
    @Inject
    private OrchestratorService orchestratorService;
    @Inject
    private NodeRepository nodeRepository;

    @Inject
    private FasitUpdateService fasitUpdateService;
    @Inject
    private FasitRestClient fasitRestClient;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response provision(OrderDetailsDO orderDetails, @Context UriInfo uriInfo, @QueryParam("prepare") Boolean prepare) {
        Order order;
        Settings settings = new Settings(orderDetails);
        Guard.checkAccessToEnvironmentClass(orderDetails.getEnvironmentClass());
        if (orderDetails.getNodeType().equals(NodeType.PLAIN_LINUX)) {
            Guard.checkSuperUserAccess();
        }
        order = Order.newProvisionOrder(orderDetails.getNodeType(), settings);

        orderRepository.save(order);

        URI vmInformationUri = createOrderUri(uriInfo, "putVmInformation", order.getId());
        URI resultUri = createOrderUri(uriInfo, "updateStatuslog", order.getId());
        ProvisionRequest request = new OrderV2Factory(order, User.getCurrentUser().getName(), vmInformationUri, resultUri, fasitRestClient).createProvisionOrder();
        WorkflowToken workflowToken;

        if (prepare == null || !prepare) {
            saveOrderStatusEntry(order, "Basta", "Calling Orchestrator", "provisioning", "");
            workflowToken = orchestratorService.send(request);
            order.setOrchestratorOrderId(workflowToken.getId());
            order.setRequestXml(convertXmlToString(request.censore()));
        } else {
            order.setRequestXml(convertXmlToString(request));
        }
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity(createRichOrderDO(uriInfo, order))
                .build();
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{orderId}")
    public Response putXMLOrder(String string, @PathParam("orderId") Long orderId, @Context UriInfo uriInfo) {
        Guard.checkSuperUserAccess();
        ProvisionRequest request;
        try {
            request = XmlUtils.parseAndValidateXmlString(ProvisionRequest.class, string);
        } catch (UnmarshalException e) {
            SAXParseException spe = (SAXParseException) e.getLinkedException();
            return Response.status(400).entity(getValidationMessage(spe)).header("Content-type", "text/plain").build();
        }

        try {
            Guard.checkAccessToEnvironmentClass(ProvisionRequest.OrchestratorEnvClass.fromString(request.getEnvironmentClass()));
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getLocalizedMessage()).header("Content-type", "text/plain").build();
        }

        WorkflowToken workflowToken = orchestratorService.send(request);
        Order order = orderRepository.findOne(orderId);
        if (order.getOrchestratorOrderId() == null) {
            saveOrderStatusEntry(order, "Basta", "Calling Orchestrator", "provisioning", "");
            order.setRequestXml(convertXmlToString(request.censore()));
            order.setOrchestratorOrderId(workflowToken.getId());
            order.getSettings().setXmlCustomized();
            order = orderRepository.save(order);
        }
        return Response.ok(new OrderDO(order, uriInfo)).build();
    }

    private String getValidationMessage(SAXParseException spe) {
        String msg = spe.getLocalizedMessage();
        if (msg.contains(":")) {
            msg = msg.substring(msg.indexOf(":") + 1);
        }
        return "(" + spe.getLineNumber() + ":" + spe.getColumnNumber() + ")  - " + msg;
    }

    protected static String convertXmlToString(OrchestatorRequest request) {
        try {
            return XmlUtils.prettyFormat(XmlUtils.generateXml(request), 2);
        } catch (JAXBException e) {
            String message = "Error in XML printing";
            logger.error(message, e);
            return message;
        }
    }

    @PUT
    @Path("{orderId}/decommission")
    @Consumes(MediaType.APPLICATION_XML)
    public void removeVmInformation(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        logger.info(ReflectionToStringBuilder.toString(vm));
        Order order = orderRepository.findOne(orderId);
        fasitUpdateService.removeFasitEntity(order, vm.getHostName());

        updateNodeStatus(order, vm.getHostName(), NodeStatus.DECOMMISSIONED);
    }

    private void updateNodeStatus(Order order, String hostname, NodeStatus nodeStatus) {

        for (Node node : nodeRepository.findActiveNodesByHostname(hostname)) {
            node.addOrder(order);
            node.setNodeStatus(nodeStatus);
            order.addNode(node);
            orderRepository.save(order);
        }
    }

    @PUT
    @Path("{orderId}/stop")
    @Consumes(MediaType.APPLICATION_XML)
    public void stopVmInformation(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        logger.info(ReflectionToStringBuilder.toString(vm));
        Order order = orderRepository.findOne(orderId);
        fasitUpdateService.stopFasitEntity(order, vm.getHostName());
        updateNodeStatus(order, vm.getHostName(), NodeStatus.STOPPED);
    }

    @PUT
    @Path("{orderId}/start")
    @Consumes(MediaType.APPLICATION_XML)
    public void startVmInformation(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        logger.info(ReflectionToStringBuilder.toString(vm));
        Order order = orderRepository.findOne(orderId);
        fasitUpdateService.startFasitEntity(order, vm.getHostName());
        updateNodeStatus(order, vm.getHostName(), NodeStatus.ACTIVE);
    }

    @PUT
    @Path("{orderId}/vm")
    @Consumes(MediaType.APPLICATION_XML)
    public void putVmInformation(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
        Order order = orderRepository.findOne(orderId);
        Node node = new Node(order, order.getNodeType(), vm.getHostName(), vm.getAdminUrl(), vm.getCpuCount(), vm.getMemoryMb(), vm.getDatasenter(), vm.getMiddlewareType(), vm.getvApp());
        fasitUpdateService.createFasitEntity(order, vm, node);
        orderRepository.save(order);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{orderId}/statuslog")
    public void updateStatuslog(@PathParam("orderId") Long orderId, OrderStatusLogDO orderStatusLogDO, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        logger.info("Order id " + orderId + " got result " + orderStatusLogDO);
        Order order = orderRepository.findOne(orderId);
        order.setStatusIfMoreImportant(OrderStatus.fromString(orderStatusLogDO.getOption()));
        orderRepository.save(order);
        saveOrderStatusEntry(order, "Orchestrator", orderStatusLogDO.getText(), orderStatusLogDO.getType(), orderStatusLogDO.getOption());
    }

    @GET
    @Path("/page/{page}/{size}/{fromdate}/{todate}")
    @Cache(maxAge = 30)
    public Response getOrdersInPages(@PathParam("page") int page, @PathParam("size") int size, @PathParam("fromdate") long fromdate, @PathParam("todate") long todate, @Context final UriInfo uriInfo) {
        DateTime from = new DateTime(fromdate);
        DateTime to = new DateTime(todate);
        List<Order> set = orderRepository.findRelevantOrders(from, to, new PageRequest(page, size));
        if (set.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(FluentIterable.from(set).transform(new SerializableFunction<Order, OrderDO>() {
                public OrderDO process(Order order) {
                    OrderDO orderDO = new OrderDO(order, uriInfo);
                    orderDO.addAllNodesWithoutOrderReferences(order, uriInfo);
                    return orderDO;
                }
            }).toList()).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@PathParam("id") long id, @Context final UriInfo uriInfo) {
        Order one = orderRepository.findOne(id);
        if (one == null || one.getOrchestratorOrderId() == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Order order = statusEnricherFunction.process(one);
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

    private void saveOrderStatusEntry(Order order, String source, String text, String type, String option) {
        order.addStatusLog(new OrderStatusLog(source, text, type, option));
        orderRepository.save(order);
    }

    private CacheControl noCache() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    private OrderDO createRichOrderDO(final UriInfo uriInfo, Order order) {
        OrderDO orderDO = new OrderDO(order, uriInfo);
        orderDO.addAllNodesWithOrderReferences(order, uriInfo);
        orderDO.setNextOrderId(orderRepository.findNextId(order.getId()));
        orderDO.setPreviousOrderId(orderRepository.findPreviousId(order.getId()));
        if (order.getOrchestratorOrderId() != null || User.getCurrentUser().hasSuperUserAccess()) {
            orderDO.setRequestXml(order.getRequestXml());
        }

        OrderDetailsDO orderDetailsDO = new OrderDetailsDO(order);
        orderDO.setSettings(orderDetailsDO);
        return orderDO;
    }

    protected Order enrichStatus(Order order) {
        return statusEnricherFunction.apply(order);
    }

    private final SerializableFunction<Order, Order> statusEnricherFunction = new SerializableFunction<Order, Order>() {
        public Order process(Order order) {
            if (!order.getStatus().isEndstate()) {
                String orchestratorOrderId = order.getOrchestratorOrderId();
                if (orchestratorOrderId == null) {
                    order.setStatus(OrderStatus.FAILURE);
                    order.setErrorMessage("Ordre mangler ordrenummer fra orchestrator");
                } else {
                    Tuple<OrderStatus, String> tuple = orchestratorService.getOrderStatus(orchestratorOrderId);
                    order.setStatusIfMoreImportant(tuple.fst);
                    order.setErrorMessage(tuple.snd);
                }
                if (!order.getStatus().isEndstate() && order.getCreated().isBefore(now().minus(standardHours(12)))) {
                    order.setStatus(OrderStatus.FAILURE);
                    order.setErrorMessage("Tidsavbrutt");
                }
                orderRepository.save(order);
            }
            return order;
        }
    };
}