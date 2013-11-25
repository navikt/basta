package no.nav.aura.basta.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import no.nav.aura.basta.EnvironmentClass;
import no.nav.aura.basta.User;
import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV1Factory;
import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.util.SerializableFunction;
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

    @Inject
    public OrdersRestService(OrderRepository orderRepository, NodeRepository nodeRepository, OrchestratorService orchestratorService) {
        this.orderRepository = orderRepository;
        this.nodeRepository = nodeRepository;
        this.orchestratorService = orchestratorService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public OrderDO postOrder(SettingsDO settings) {
        checkAccess(EnvironmentClass.from(settings.getEnvironmentClass()));
        ProvisionRequest request = new OrderV1Factory(settings).createOrder();
        WorkflowToken workflowToken = orchestratorService.send(request);
        Order order = orderRepository.save(new Order(workflowToken.getId()));
        return new OrderDO(order);
    }

    @PUT
    @Path("{orderId}/vm")
    public void putVmInformation(@PathParam("orderId") Long orderId, ResultNodeDO vm) {
        nodeRepository.save(new Node(orderId, vm.getHostName()));
    }

    @GET
    public List<OrderDO> getOrders() {
        return FluentIterable.from(orderRepository.findAll()).transform(new SerializableFunction<Order, OrderDO>() {
            public OrderDO process(Order order) {
                return new OrderDO(order);
            }
        }).toList();
    }

    private void checkAccess(EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        if (!user.getEnvironmentClasses().contains(environmentClass)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to environment class " + environmentClass);
        }
    }
}
