package no.nav.aura.basta.rest;

import static no.nav.aura.basta.rest.UriFactory.createOrderUri;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.FluentIterable;

@SuppressWarnings("serial")
@Component
@Path("/orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrdersRestService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersRestService.class);

    private final OrderRepository orderRepository;
    private final OrchestratorService orchestratorService;
    private final NodeRepository nodeRepository;
    private final SettingsRepository settingsRepository;
    private final FasitUpdateService fasitUpdateService;
    private final FasitRestClient fasitRestClient;

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
    public OrderDO postOrder(SettingsDO settingsDO, @Context UriInfo uriInfo) {
        checkAccess(settingsDO.getEnvironmentClass());
        String currentUser = User.getCurrentUser().getName();
        Order order = orderRepository.save(new Order());
        URI vmInformationUri = createOrderUri(uriInfo, "putVmInformation", order.getId());
        URI resultUri = createOrderUri(uriInfo, "putResult", order.getId());
        Settings settings = new Settings(order, settingsDO);
        ProvisionRequest request = new OrderV2Factory(settings, currentUser, vmInformationUri, resultUri, fasitRestClient).createOrder();
        order.setRequestXml(xmlToString(request));
        order = orderRepository.save(order);
        WorkflowToken workflowToken = orchestratorService.send(request);
        order.setOrchestratorOrderId(workflowToken.getId());
        order = orderRepository.save(order);
        settingsRepository.save(settings);
        return new OrderDO(order, uriInfo);
    }

    private String xmlToString(ProvisionRequest request) {
        try {
            return XmlUtils.prettyFormat(XmlUtils.generateXml(request), 2);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @PUT
    @Path("{orderId}/vm")
    @Consumes(MediaType.APPLICATION_XML)
    public void putVmInformation(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm) {
        logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
        Node node = nodeRepository.save(new Node(orderId, vm.getHostName(), vm.getAdminUrl(), vm.getCpuCount(), vm.getMemoryMb(), vm.getDatasenter(), vm.getMiddlewareType(), vm.getvApp()));
        fasitUpdateService.updateFasit(orderId, vm, node);
    }

    @POST
    @Path("{orderId}/result")
    public void putResult(@PathParam("orderId") Long orderId, String anything) {
        // TODO get real results
        logger.info("Order id " + orderId + " got result '" + anything + "'");
    }

    @GET
    public List<OrderDO> getOrders(@Context final UriInfo uriInfo) {
        return FluentIterable.from(orderRepository.findAll()).transform(new SerializableFunction<Order, OrderDO>() {
            public OrderDO process(Order order) {
                return new OrderDO(order, uriInfo);
            }
        }).toList();
    }

    @GET
    @Path("{id}")
    public OrderDO getOrder(@PathParam("id") long id, @Context UriInfo uriInfo) {
        return new OrderDO(orderRepository.findOne(id), uriInfo);
    }

    @GET
    @Path("{orderId}/requestXml")
    @Produces(MediaType.TEXT_XML)
    public String getRequestXml(@PathParam("orderId") long orderId) {
        return orderRepository.findOne(orderId).getRequestXml();
    }

    @GET
    @Path("{orderId}/settings")
    public SettingsDO getSettings(@PathParam("orderId") long orderId) {
        return new SettingsDO(settingsRepository.findByOrderId(orderId));
    }

    @GET
    @Path("{orderId}/nodes")
    public Iterable<NodeDO> getNodes(@PathParam("orderId") long orderId) {
        return FluentIterable.from(nodeRepository.findByOrderId(orderId)).transform(new SerializableFunction<Node, NodeDO>() {
            public NodeDO process(Node node) {
                return new NodeDO(node.getAdminUrl(), node.getApplicationServerType(), node.getCpuCount(), node.getDatasenter(), node.getHostname(), node.getMemoryMb(), node.getVapp());
            }
        });
    }

    private void checkAccess(EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        if (!user.getEnvironmentClasses().contains(environmentClass)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to environment class " + environmentClass);
        }
    }
}
