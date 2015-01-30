package no.nav.aura.basta.rest;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.NodeStatus;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.backend.vmware.OrchestratorRequestFactory;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.XmlUtils;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static no.nav.aura.basta.rest.UriFactory.createOrderApiUri;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;

@SuppressWarnings("serial")
@Component
@Path("/orders/")
@Transactional
public class OrdersRestService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersRestService.class);

    @Inject
    private OrderRepository orderRepository;
    @Inject
    private OrchestratorService orchestratorService;

    @Inject
    private FasitUpdateService fasitUpdateService;
    @Inject
    private FasitRestClient fasitRestClient;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response provisionNew(Map<String,String> map, @Context UriInfo uriInfo, @QueryParam("prepare") Boolean prepare) {

        VMOrderInput input = new VMOrderInput(map);

        Guard.checkAccessToEnvironmentClass(input);
        if (input.getNodeType().equals(NodeType.PLAIN_LINUX)){
            Guard.checkSuperUserAccess();
        }

        Order order = Order.newProvisionOrder(input);

        orderRepository.save(order);
        URI vmInformationUri = createOrderApiUri(uriInfo, "add", order.getId());
        URI resultUri = createOrderApiUri(uriInfo, "log", order.getId());
        ProvisionRequest request = new OrchestratorRequestFactory(order, User.getCurrentUser().getName(), vmInformationUri, resultUri, fasitRestClient).createProvisionOrder();
        WorkflowToken workflowToken;

        if (prepare == null || !prepare) {
            saveOrderStatusEntry(order, "Basta", "Calling Orchestrator", "provisioning", "");
            workflowToken = orchestratorService.send(request);
            order.setExternalId(workflowToken.getId());
            order.setExternalRequest(convertXmlToString(request.censore()));
        } else {
            order.setExternalRequest(convertXmlToString(request));
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
        if (order.getExternalId() == null) {
            saveOrderStatusEntry(order, "Basta", "Calling Orchestrator", "provisioning", "");
            order.setExternalRequest(convertXmlToString(request.censore()));
            order.setExternalId(workflowToken.getId());
            order.getInputAs(VMOrderInput.class).setXmlCustomized();
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
    public void removeVmInformation(@PathParam("orderId") Long orderId, List<OrchestratorNodeDO> vms, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        for (OrchestratorNodeDO vm : vms) {
            logger.info(ReflectionToStringBuilder.toString(vm));
            Order order = orderRepository.findOne(orderId);
            fasitUpdateService.removeFasitEntity(order, vm.getHostName());
            VMOrderResult result = order.getResultAs(VMOrderResult.class);
            result.addHostnameWithStatus(vm.getHostName(), NodeStatus.DECOMMISSIONED);
        }

    }

    @PUT
    @Path("{orderId}/stop")
    @Consumes(MediaType.APPLICATION_XML)
    public void stopVmInformation(@PathParam("orderId") Long orderId, List<OrchestratorNodeDO> vms, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        for (OrchestratorNodeDO vm : vms) {
            logger.info(ReflectionToStringBuilder.toString(vm));
            Order order = orderRepository.findOne(orderId);
            fasitUpdateService.stopFasitEntity(order, vm.getHostName());
            VMOrderResult result = order.getResultAs(VMOrderResult.class);
            result.addHostnameWithStatus(vm.getHostName(), NodeStatus.STOPPED);
        }
    }

    @PUT
    @Path("{orderId}/start")
    @Consumes(MediaType.APPLICATION_XML)
    public void startVmInformation(@PathParam("orderId") Long orderId, List<OrchestratorNodeDO> vms, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        for (OrchestratorNodeDO vm : vms) {
            logger.info(ReflectionToStringBuilder.toString(vm));
            Order order = orderRepository.findOne(orderId);
            fasitUpdateService.startFasitEntity(order, vm.getHostName());
            VMOrderResult result = order.getResultAs(VMOrderResult.class);
            result.addHostnameWithStatus(vm.getHostName(), NodeStatus.ACTIVE);
        }
    }


    @PUT
    @Path("{orderId}/vm")
    @Consumes(MediaType.APPLICATION_XML)
    public void putVmInformationAsList(@PathParam("orderId") Long orderId, List<OrchestratorNodeDO> vms, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        logger.info("Recieved list of with {} vms as orderid {}", vms.size(), orderId );
        for (OrchestratorNodeDO vm : vms) {
            logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
            Order order = orderRepository.findOne(orderId);
            VMOrderResult result = order.getResultAs(VMOrderResult.class);
            result.addHostnameWithStatus(vm.getHostName(), NodeStatus.ACTIVE);
            fasitUpdateService.createFasitEntity(order, vm);
            orderRepository.save(order);
        }
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

    protected OrderDO createRichOrderDO(final UriInfo uriInfo, Order order) {
        OrderDO orderDO = new OrderDO(order, uriInfo);
        orderDO.setNextOrderId(orderRepository.findNextId(order.getId()));
        orderDO.setPreviousOrderId(orderRepository.findPreviousId(order.getId()));
        for (NodeDO nodeDO : orderDO.getNodes()) {
            List<OrderDO> history = orderToOrderDo(uriInfo, nodeDO);
            nodeDO.setHistory(history);
        }



        if (order.getExternalId() != null || User.getCurrentUser().hasSuperUserAccess()) {
            orderDO.setExternalRequest(order.getExternalRequest());
        }

        OrderDetailsDO orderDetailsDO = new OrderDetailsDO(order);
        orderDO.setSettings(orderDetailsDO);
        return orderDO;
    }

    private List<OrderDO> orderToOrderDo(final UriInfo uriInfo, NodeDO nodeDO) {
        return FluentIterable.from(orderRepository.findRelatedOrders(nodeDO.getHostname())).transform(new Function<Order, OrderDO>() {
            @Override
            public OrderDO apply(Order input) {
                return new OrderDO(input, uriInfo);
            }
        }).toList();
    }

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
}
