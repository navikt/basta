package no.nav.aura.basta.rest;

import static no.nav.aura.basta.rest.UriFactory.createOrderUri;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import no.nav.aura.basta.User;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV2Factory;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.*;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXParseException;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("serial")
@Component
@Path("/orders")
@Transactional
public class OrdersRestService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersRestService.class);

    private static final CacheControl MAX_AGE_30 = CacheControl.valueOf("max-age=30");

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
        checkAccess(orderDetails);
        String currentUser = User.getCurrentUser().getName();
        Order order = orderRepository.save(new Order(orderDetails.getNodeType()));
        URI vmInformationUri = createOrderUri(uriInfo, "putVmInformation", order.getId());
        URI resultUri = createOrderUri(uriInfo, "putResult", order.getId());
        Settings settings = new Settings(order, orderDetails);
        OrchestatorRequest request = new OrderV2Factory(settings, currentUser, vmInformationUri, resultUri, fasitRestClient).createOrder();
        WorkflowToken workflowToken;
        if (prepare == null || !prepare) {
            if (request instanceof ProvisionRequest) {
                orderStatusLogRepository.save(new OrderStatusLog(order, "Basta", "Calling Orchestrator for provisioning", "basta:provisioning:init", ""));
                workflowToken = orchestratorService.send(request);
                order.setOrchestratorOrderId(workflowToken.getId());
            } else if (request instanceof DecomissionRequest) {
                orderStatusLogRepository.save(new OrderStatusLog(order, "Basta", "Calling Orchestrator for decommissioning", "basta:decommissioning:init", ""));
                workflowToken = orchestratorService.decommission((DecomissionRequest) request);
                order.setOrchestratorOrderId(workflowToken.getId());
                Optional<String> hosts = settings.getProperty(DecommissionProperties.DECOMMISSION_HOSTS_PROPERTY_KEY);
                if (hosts.isPresent()) {
                    fasitUpdateService.removeFasitEntity(order, hosts.get());
                }
            } else {
                throw new RuntimeException("Unknown request type " + request.getClass());
            }
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
        ProvisionRequest request;
        try {
            request = XmlUtils.parseAndValidateXmlString(ProvisionRequest.class, string);
        } catch (UnmarshalException e) {
            SAXParseException spe = (SAXParseException) e.getLinkedException();
            return Response.status(400).entity(getValidationMessage(spe)).header("Content-type", "text/plain").build();
        }

        checksuperDuperAccess(User.getCurrentUser());
        WorkflowToken workflowToken;
        workflowToken = orchestratorService.send(request);
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
        return "(linje " + spe.getLineNumber() + ", kolonne " + spe.getColumnNumber() + ") " + msg;
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

    public String convertXmlToString(OrchestatorRequest request) {
        try {
            return XmlUtils.prettyFormat(XmlUtils.generateXml(request), 2);
        } catch (JAXBException e) {
            String message = "Error in XML printing";
            logger.error(message, e);
            return message;
        }
    }

    @PUT
    @Path("{orderId}/vm")
    @Consumes(MediaType.APPLICATION_XML)
    public void putVmInformation(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        checkAccess(request.getRemoteAddr());
        logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
        Order order = orderRepository.findOne(orderId);
        Node node = nodeRepository.save(new Node(order, vm.getHostName(), vm.getAdminUrl(), vm.getCpuCount(), vm.getMemoryMb(), vm.getDatasenter(), vm.getMiddlewareType(), vm.getvApp()));
        fasitUpdateService.createFasitEntity(order, vm, node);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{orderId}/result")
    public void putResult(@PathParam("orderId") Long orderId, OrderStatusLogDO orderStatusLogDO, @Context HttpServletRequest request) {
        checkAccess(request.getRemoteAddr());
        logger.info("Order id " + orderId + " got result " + orderStatusLogDO);
        Order order = orderRepository.findOne(orderId);
        order.setStatus(OrderStatus.fromString(orderStatusLogDO.getOption()));
        orderRepository.save(order);

        OrderStatusLog orderStatusLog = orderStatusLogRepository.save(new OrderStatusLog(order, "Orchestrator", orderStatusLogDO.getText(),orderStatusLogDO.getType(), orderStatusLogDO.getOption()));

        logger.info("Order id " + orderId + " persisted with orderStatusLog.id '" + orderStatusLog.getId() + "'");
    }

    @GET
    public Response getOrders(@Context final UriInfo uriInfo) {
        return Response.ok(FluentIterable.from(orderRepository.findByOrchestratorOrderIdNotNull()).transform(new SerializableFunction<Order, OrderDO>() {
            public OrderDO process(Order order) {
                return new OrderDO(order, uriInfo);
            }
        }).toList()).cacheControl(noCache()).expires(new Date(0L)).build();
    }

    private CacheControl noCache() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@PathParam("id") long id, @Context final UriInfo uriInfo) {
        Order one = orderRepository.findOne(id);
        if (one==null){
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
        if (one==null){
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

    private OrderDO createRichOrderDO(final UriInfo uriInfo, Order order) {
        String requestXml = null;
        if (order.getOrchestratorOrderId() != null || User.getCurrentUser().hasSuperUserAccess()) {
            requestXml = order.getRequestXml();
        }
        ImmutableList<NodeDO> nodes = FluentIterable.from(nodeRepository.findByOrder(order)).transform(new SerializableFunction<Node, NodeDO>() {
            public NodeDO process(Node node) {
                return new NodeDO(node, uriInfo);
            }
        }).toList();
        OrderDetailsDO settings = new OrderDetailsDO(settingsRepository.findByOrderId(order.getId()));
        Long next = orderRepository.findNextId(order.getId());
        Long previous = orderRepository.findPreviousId(order.getId());
        return new OrderDO(order, nodes, requestXml, settings, uriInfo, previous,next);
    }

    protected void checkAccess(final OrderDetailsDO orderDetails) {
        if (orderDetails.getNodeType() == NodeType.DECOMMISSIONING) {
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
        } else {
            if (!User.getCurrentUser().hasAccess(orderDetails.getEnvironmentClass())) {
                throw new UnauthorizedException("User " + User.getCurrentUser().getName() + " does not have access to environment class " + orderDetails.getEnvironmentClass());
            }
            if (!User.getCurrentUser().hasSuperUserAccess() && orderDetails.getNodeType().equals(NodeType.PLAIN_LINUX)) {
                throw new UnauthorizedException("User " + User.getCurrentUser().getName() + " does not have access to order a plain linux server");
            }
        }
    }

    private SerializableFunction<Node, Iterable<String>> filterUnauthorisedHostnames() {
        return new SerializableFunction<Node, Iterable<String>>() {
            public Iterable<String> process(Node node) {
                Settings settings = settingsRepository.findByOrderId(node.getOrder().getId());
                if (User.getCurrentUser().hasAccess(settings.getEnvironmentClass())) {
                    return Collections.<String> emptySet();
                }
                return Sets.newHashSet(node.getHostname());
            }
        };
    }

    private void checkAccess(String remoteAddr) {
        // TODO Check remote address
        logger.info("Called from " + remoteAddr);
    }

    private void checksuperDuperAccess(User currentUser) {
        if (!currentUser.hasSuperUserAccess()) {
            throw new UnauthorizedException("User " + User.getCurrentUser().getName() + " does not have super user access");
        }
    }


    protected Order enrichStatus(Order order) {
        return statusEnricherFunction.apply(order);
    }

    private final SerializableFunction<Order, Order> statusEnricherFunction = new SerializableFunction<Order, Order>() {
        public Order process(Order order) {
            if (!order.getStatus().isTerminated()) {
                String orchestratorOrderId = order.getOrchestratorOrderId();
                if (orchestratorOrderId == null) {
                    order.setStatus(OrderStatus.FAILURE);
                    order.setErrorMessage("Ordre mangler ordrenummer fra orchestrator");
                } else {
                    if(order.isProcessingStatus()){
                        Tuple<OrderStatus, String> tuple = orchestratorService.getOrderStatus(orchestratorOrderId);
                        order.setStatus(tuple.fst);
                        order.setErrorMessage(tuple.snd);
                    }
                }
                if (!order.getStatus().isTerminated() && order.getCreated().isBefore(now().minus(standardHours(12)))) {
                    order.setStatus(OrderStatus.FAILURE);
                    order.setErrorMessage("Tidsavbrutt");
                }
                orderRepository.save(order);
            }
            return order;
        }
    };
}
