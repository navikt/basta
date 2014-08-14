package no.nav.aura.basta.rest;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import no.nav.aura.basta.Converters;
import no.nav.aura.basta.User;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV2Factory;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.*;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.generated.vmware.ws.WorkflowToken;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.resteasy.spi.UnauthorizedException;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static no.nav.aura.basta.rest.UriFactory.createOrderUri;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;

@SuppressWarnings("serial")
@Component
@Path("/orders")
@Transactional
public class OrdersRestService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersRestService.class);
    private static final CacheControl MAX_AGE_60 = CacheControl.valueOf("max-age=60");

    @Inject
    private OrderRepository orderRepository;
    @Inject
    private OrchestratorService orchestratorService;
    @Inject
    private NodeRepository nodeRepository;
    @Inject
    private SettingsRepository settingsRepository;
    @Inject
    private OrderStatusLogRepository orderStatusLogRepository;
    @Inject
    private FasitUpdateService fasitUpdateService;
    @Inject
    private FasitRestClient fasitRestClient;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public OrderDO postOrder(OrderDetailsDO orderDetails, @Context UriInfo uriInfo, @QueryParam("prepare") Boolean prepare) {
        if (orderDetails.getNodeType() == NodeType.DECOMMISSIONING) {
            checkDecommissionAccess(orderDetails);
        }else{
            Guard.checkAccessToEnvironmentClass(orderDetails.getEnvironmentClass());
            if (orderDetails.getNodeType().equals(NodeType.PLAIN_LINUX)){
                Guard.checkSuperUserAccess();
            }
        }

        Order order = orderRepository.save(new Order(orderDetails.getNodeType()));

        URI vmInformationUri = createOrderUri(uriInfo, "putVmInformation", order.getId());
        URI resultUri = createOrderUri(uriInfo, "putResult", order.getId());
        URI decommissionUri = createOrderUri(uriInfo, "removeVmInformation", order.getId());

        Settings settings = new Settings(order, orderDetails);
        OrchestatorRequest request = new OrderV2Factory(settings, User.getCurrentUser().getName(), vmInformationUri, resultUri, decommissionUri, fasitRestClient).createOrder();

        WorkflowToken workflowToken;

        if (prepare == null || !prepare) {
            if (request instanceof ProvisionRequest) {
                saveOrderStatusEntry(order, "Basta", "Calling Orchestrator", "provisioning", "");
                workflowToken = orchestratorService.send(request);
            } else if (request instanceof DecomissionRequest) {
                saveOrderStatusEntry(order, "Basta", "Calling Orchestrator", "decommissioning", "");
                workflowToken = orchestratorService.decommission((DecomissionRequest) request);
            } else {
                throw new RuntimeException("Unknown request type " + request.getClass());
            }
            order.setOrchestratorOrderId(workflowToken.getId());
            order.setRequestXml(convertXmlToString(censore(request)));
        } else {
            order.setRequestXml(convertXmlToString(request));
        }
        order = orderRepository.save(order);
        settingsRepository.save(settings);
        return createRichOrderDO(uriInfo, order);
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
       }catch(IllegalArgumentException e){
           return Response.status(400).entity(e.getLocalizedMessage()).header("Content-type", "text/plain").build();
       }

        WorkflowToken workflowToken = orchestratorService.send(request);
        Order order = orderRepository.findOne(orderId);
        if (order.getOrchestratorOrderId() == null) {
            order.setRequestXml(convertXmlToString(censore(request)));
            order.setOrchestratorOrderId(workflowToken.getId());
            order = orderRepository.save(order);
            Settings settings = settingsRepository.findByOrderId(orderId);
            settings.setXmlCustomized();
            settingsRepository.save(settings);
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

    /**
     * @param request
     *            will be censored directly
     * @return same as input, but now censored
     */
    public OrchestatorRequest censore(OrchestatorRequest request) {
        if (request instanceof ProvisionRequest) {
            ProvisionRequest provisionRequest = (ProvisionRequest) request;
            for (VApp vapp : Optional.fromNullable(provisionRequest.getvApps()).or(Lists.<VApp> newArrayList())) {
                for (Vm vm : Optional.fromNullable(vapp.getVms()).or(Lists.<Vm> newArrayList())) {
                    for (Fact fact : Optional.fromNullable(vm.getCustomFacts()).or(Lists.<Fact> newArrayList())) {
                        if (FactType.valueOf(fact.getName()).isMask()) {
                            fact.setValue("********");
                        }
                    }
                }
            }
        }
        return request;
    }

    protected String convertXmlToString(OrchestatorRequest request) {
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
    }

    @PUT
    @Path("{orderId}/vm")
    @Consumes(MediaType.APPLICATION_XML)
    public void putVmInformation(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
        Order order = orderRepository.findOne(orderId);
        Node node = nodeRepository.save(new Node(order, vm.getHostName(), vm.getAdminUrl(), vm.getCpuCount(), vm.getMemoryMb(), vm.getDatasenter(), vm.getMiddlewareType(), vm.getvApp()));
        fasitUpdateService.createFasitEntity(order, vm, node);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{orderId}/result")
    public void putResult(@PathParam("orderId") Long orderId, OrderStatusLogDO orderStatusLogDO, @Context HttpServletRequest request) {
        Guard.checkAccessAllowedFromRemoteAddress(request.getRemoteAddr());
        logger.info("Order id " + orderId + " got result " + orderStatusLogDO);
        Order order = orderRepository.findOne(orderId);
        order.setStatusIfMoreImportant(OrderStatus.fromString(orderStatusLogDO.getOption()));
        orderRepository.save(order);
        saveOrderStatusEntry(order, "Orchestrator", orderStatusLogDO.getText(), orderStatusLogDO.getType(), orderStatusLogDO.getOption());
    }

    @GET
    @Path("/page/{page}/{size}/{fromdate}/{todate}")
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
                    orderDO.setNodes(transformToNodeDOs(uriInfo, getNodesByNodeType(order), false));
                    return orderDO;
                }
            }).toList()).cacheControl(MAX_AGE_60).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@PathParam("id") long id, @Context final UriInfo uriInfo) {
        Order one = orderRepository.findOne(id);
        if (one == null) {
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

        Set<OrderStatusLog> orderStatusLogs = orderStatusLogRepository.findByOrderId(orderId);
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


    private void saveOrderStatusEntry(Order order, String source, String text, String type, String option){
        OrderStatusLog statusLog = orderStatusLogRepository.save(new OrderStatusLog(order, source, text, type, option));
        logger.info("Order id " + order.getId() + " persisted with orderStatusLog.id '" + statusLog.getId() + "'");
    }

    private CacheControl noCache() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    private OrderDO createRichOrderDO(final UriInfo uriInfo, Order order) {
        String requestXml = null;
        if (order.getOrchestratorOrderId() != null || User.getCurrentUser().hasSuperUserAccess()) {
            requestXml = order.getRequestXml();
        }
        Set<Node> n = getNodesByNodeType(order);

        ImmutableList<NodeDO> nodes = transformToNodeDOs(uriInfo, n, true);

        OrderDetailsDO settings = new OrderDetailsDO(settingsRepository.findByOrderId(order.getId()));
        ApplicationMapping applicationMapping = settings.getApplicationMapping();
        if (applicationMapping.applicationsNeedsToBeFetchedFromFasit()) {
            applicationMapping.loadApplicationsInApplicationGroup(fasitRestClient);
        }
        Long next = orderRepository.findNextId(order.getId());
        Long previous = orderRepository.findPreviousId(order.getId());
        return new OrderDO(order, nodes, requestXml, settings, uriInfo, previous, next);
    }

    private Set<Node> getNodesByNodeType(Order order) {
        Set<Node> n;
        if (order.getNodeType().equals(NodeType.DECOMMISSIONING)) {
            n = nodeRepository.findByDecommissionOrder(order);
        } else {
            n = nodeRepository.findByOrder(order);
        }
        return n;
    }

    private ImmutableList<NodeDO> transformToNodeDOs(final UriInfo uriInfo, final Set<Node> n, final boolean full) {
        return FluentIterable.from(n).transform(new SerializableFunction<Node, NodeDO>() {
            public NodeDO process(Node node) {
                return full ? new NodeDO(node, uriInfo) : new NodeDO(node);
            }
        }).toList();
    }

    private void checkDecommissionAccess(final OrderDetailsDO orderDetails) {
        SerializableFunction<String, Iterable<Node>> retrieveNodes = new SerializableFunction<String, Iterable<Node>>() {
            @Override
            public Iterable<Node> process(String hostname) {
                return nodeRepository.findByHostnameAndDecommissionOrderIdIsNull(hostname);
            }
        };

        SerializableFunction<Node, Iterable<String>> filterUnauthorisedHostnames = new SerializableFunction<Node, Iterable<String>>() {
            @Override
            public Iterable<String> process(Node node) {
                Settings settings = settingsRepository.findByOrderId(node.getOrder().getId());
                if (User.getCurrentUser().hasAccess(settings.getEnvironmentClass())) {
                    return Collections.emptySet();
                }
                return Sets.newHashSet(node.getHostname());
            }
        };

        FluentIterable<String> errors = FluentIterable.from(Sets.newHashSet(orderDetails.getHostnames()))
                                                .filter(Predicates.containsPattern("."))
                                                .transformAndConcat(retrieveNodes)
                                                .transformAndConcat(filterUnauthorisedHostnames);
        if (!errors.isEmpty()) {
            throw new UnauthorizedException("User " + User.getCurrentUser().getName() + " does not have access to decommission nodes: " + errors.toString());
        }
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