package no.nav.aura.basta.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import no.nav.aura.basta.EnvironmentClass;
import no.nav.aura.basta.User;
import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV1Factory;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.vmware.orchestrator.requestv1.ProvisionRequest;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
@Path("/")
public class OrdersRestService {

    @Inject
    private OrderRepository orderRepository;
    @Inject
    private OrchestratorService orchestratorService;

    @POST
    @Path("/orders")
    @Consumes(MediaType.APPLICATION_JSON)
    public OrderDO postOrder(SettingsDO settings, @QueryParam("dryRun") Boolean dryRun) {
        checkAccess(EnvironmentClass.from(settings.getEnvironmentClass()));
        ProvisionRequest request = new OrderV1Factory(settings).createOrder();
        WorkflowToken workflowToken = orchestratorService.send(request);
        Order order = orderRepository.save(new Order(workflowToken.getId()));
        return new OrderDO(order);
    }

    private void checkAccess(EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        if (!user.getEnvironmentClasses().contains(environmentClass)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to environment class " + environmentClass);
        }
    }
}
