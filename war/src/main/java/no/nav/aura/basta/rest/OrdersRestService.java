package no.nav.aura.basta.rest;

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

import no.nav.aura.basta.EnvironmentClass;
import no.nav.aura.basta.User;
import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV1Factory;
import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.persistence.SettingsRepository;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.requestv1.ProvisionRequest;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.springframework.stereotype.Component;

import com.google.common.collect.FluentIterable;

@SuppressWarnings("serial")
@Component
@Path("/orders")
public class OrdersRestService {

    private final OrderRepository orderRepository;
    private final OrchestratorService orchestratorService;
    private final NodeRepository nodeRepository;
    private final SettingsRepository settingsRepository;

    @Inject
    public OrdersRestService(OrderRepository orderRepository, NodeRepository nodeRepository, SettingsRepository settingsRepository, OrchestratorService orchestratorService) {
        this.orderRepository = orderRepository;
        this.nodeRepository = nodeRepository;
        this.settingsRepository = settingsRepository;
        this.orchestratorService = orchestratorService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public OrderDO postOrder(SettingsDO settings, @Context UriInfo uriInfo) {
        checkAccess(EnvironmentClass.from(settings.getEnvironmentClass()));
        String currentUser = User.getCurrentUser().getName();
        ProvisionRequest request = new OrderV1Factory(settings, currentUser).createOrder();
        WorkflowToken workflowToken = orchestratorService.send(request);
        Order order = orderRepository.save(new Order(workflowToken.getId(), currentUser, xmlToString(request)));
        settingsRepository.save(new Settings(order, settings.getApplicationName(), settings.getApplicationServerType(), EnvironmentClass.from(settings.getEnvironmentClass()), settings.getEnvironmentName(),
                settings.getServerCount(), settings.getServerSize(), settings.getZone()));
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
    public void putVmInformation(@PathParam("orderId") Long orderId, ResultNodeDO vm) {
        nodeRepository.save(new Node(orderId, vm.getHostName(), vm.getAdminUrl(), vm.getCpuCount(), vm.getMemoryMb(), vm.getDatasenter(), vm.getMiddlewareType(), vm.getvApp()));
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
        Order order = orderRepository.findOne(id);
        return new OrderDO(order, uriInfo);
    }

    @GET
    @Path("{orderId}/requestXml")
    @Produces(MediaType.TEXT_XML)
    public String getRequestXml(@PathParam("orderId") long orderId, @Context UriInfo uriInfo) {
        return orderRepository.findOne(orderId).getRequestXml();
    }

    private void checkAccess(EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        if (!user.getEnvironmentClasses().contains(environmentClass)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to environment class " + environmentClass);
        }
    }
}
