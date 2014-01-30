package no.nav.aura.basta.rest;

import static no.nav.aura.basta.rest.UriFactory.createOrderUri;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;

import java.net.URI;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import no.nav.aura.basta.User;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV2Factory;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.persistence.SettingsRepository;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.Fact;
import no.nav.aura.basta.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.VApp;
import no.nav.aura.basta.vmware.orchestrator.request.Vm;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
@Component
@Path("/orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrdersRestService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersRestService.class);

    private static final CacheControl MAX_AGE_30 = CacheControl.valueOf("max-age=30");

    private final OrderRepository orderRepository;
    private final OrchestratorService orchestratorService;
    private final NodeRepository nodeRepository;
    private final SettingsRepository settingsRepository;
    private final FasitUpdateService fasitUpdateService;
    private final FasitRestClient fasitRestClient;

    protected final SerializableFunction<Order, Order> statusEnricherFunction = new SerializableFunction<Order, Order>() {
        public Order process(Order order) {
            if (!order.getStatus().isTerminated()) {
                String orchestratorOrderId = order.getOrchestratorOrderId();
                if (orchestratorOrderId == null) {
                    order.setStatus(OrderStatus.FAILURE);
                    order.setErrorMessage("Ordre mangler ordrenummer fra orchestrator");
                } else {
                    Tuple<OrderStatus, String> tuple = orchestratorService.getOrderStatus(orchestratorOrderId);
                    order.setStatus(tuple.fst);
                    order.setErrorMessage(tuple.snd);
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

    @Inject
    public OrdersRestService(OrderRepository orderRepository, NodeRepository nodeRepository, SettingsRepository settingsRepository, OrchestratorService orchestratorService, FasitUpdateService fasitUpdateService,
            FasitRestClient fasitRestClient) {
        this.orderRepository = orderRepository;
        this.nodeRepository = nodeRepository;
        this.settingsRepository = settingsRepository;
        this.orchestratorService = orchestratorService;
        this.fasitUpdateService = fasitUpdateService;
        this.fasitRestClient = fasitRestClient;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public OrderDO postOrder(OrderDetailsDO orderDetails, @Context UriInfo uriInfo) {
        checkAccess(orderDetails.getEnvironmentClass());
        String currentUser = User.getCurrentUser().getName();
        Order order = orderRepository.save(new Order(orderDetails.getNodeType()));
        URI vmInformationUri = createOrderUri(uriInfo, "putVmInformation", order.getId());
        URI resultUri = createOrderUri(uriInfo, "putResult", order.getId());
        Settings settings = new Settings(order, orderDetails);
        OrchestatorRequest request = new OrderV2Factory(settings, currentUser, vmInformationUri, resultUri, fasitRestClient).createOrder();
        WorkflowToken workflowToken = orchestratorService.send(request);
        order.setRequestXml(convertXmlToString(censore(request)));
        order.setOrchestratorOrderId(workflowToken.getId());
        order = orderRepository.save(order);
        settingsRepository.save(settings);
        return new OrderDO(order, uriInfo);
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
        fasitUpdateService.updateFasit(orderId, vm, node);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{orderId}/result")
    public void putResult(@PathParam("orderId") Long orderId, String anything, @Context HttpServletRequest request) {
        checkAccess(request.getRemoteAddr());
        // TODO get real results
        logger.info("Order id " + orderId + " got result '" + anything + "'");
    }

    @GET
    public Response getOrders(@Context final UriInfo uriInfo) {
        return Response.ok(FluentIterable.from(orderRepository.findAll()).transform(statusEnricherFunction).transform(new SerializableFunction<Order, OrderDO>() {
            public OrderDO process(Order order) {
                return new OrderDO(order, uriInfo);
            }
        }).toList()).cacheControl(MAX_AGE_30).build();
    }

    @GET
    @Path("{id}")
    public Response getOrder(@PathParam("id") long id, @Context UriInfo uriInfo) {
        Order order = statusEnricherFunction.process(orderRepository.findOne(id));
        ResponseBuilder builder = Response.ok(new OrderDO(order, uriInfo));
        if (!order.getStatus().isTerminated()) {
            builder = builder.cacheControl(MAX_AGE_30);
        }
        return builder.build();
    }

    @GET
    @Path("{orderId}/requestXml")
    @Produces(MediaType.TEXT_XML)
    public String getRequestXml(@PathParam("orderId") long orderId) {
        return orderRepository.findOne(orderId).getRequestXml();
    }

    @GET
    @Path("{orderId}/settings")
    public OrderDetailsDO getSettings(@PathParam("orderId") long orderId) {
        return new OrderDetailsDO(settingsRepository.findByOrderId(orderId));
    }

    @GET
    @Path("{orderId}/nodes")
    public Response getNodes(@PathParam("orderId") long orderId, @Context final UriInfo uriInfo) {
        Order order = orderRepository.findOne(orderId);
        ImmutableList<NodeDO> entity = FluentIterable.from(nodeRepository.findByOrder(order)).transform(new SerializableFunction<Node, NodeDO>() {
            public NodeDO process(Node node) {
                return new NodeDO(node, uriInfo);
            }
        }).toList();
        return Response.ok(entity).cacheControl(MAX_AGE_30).build();
    }

    private void checkAccess(EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        if (!user.getEnvironmentClasses().contains(environmentClass)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to environment class " + environmentClass);
        }
    }

    private void checkAccess(String remoteAddr) {
        // TODO Check remote address
        logger.info("Called from " + remoteAddr);
    }

}
