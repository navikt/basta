package no.nav.aura.basta.rest.vm;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/vm/orders/wildfly")
@Transactional
public class WildflyOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(WildflyOrderRestService.class);

    private OrderRepository orderRepository;

    private OrchestratorService orchestratorService;

    protected WildflyOrderRestService() {
    }

    @Inject
    public WildflyOrderRestService(OrderRepository orderRepository, OrchestratorService orchestratorService) {
        super();
        this.orderRepository = orderRepository;
        this.orchestratorService = orchestratorService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createWildflyNode(Map<String, String> map, @Context UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);

        input.setMiddlewareType(MiddlewareType.wildfly_9);
        input.setClassification(findClassification(input.copy()));
        if (input.getDescription() == null) {
            input.setDescription("wildfly node");
        }

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new jboss order {} with input {}", order.getId(), map);
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallabackUri);
        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            request.addVm(vm);
        }
        order = sendToOrchestrator(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
    }

    private Classification findClassification(Map<String, String> map) {
        VMOrderInput input = new VMOrderInput(map);
        return input.getClassification();
    }

    private Order sendToOrchestrator(Order order, OrchestatorRequest request) {

        WorkflowToken workflowToken;
        orderRepository.save(order.addStatuslogInfo("Calling Orchestrator for provisioning"));
        workflowToken = orchestratorService.provision(request);
        order.setExternalId(workflowToken.getId());

        order.setExternalRequest(XmlUtils.generateXml(request));

        order = orderRepository.save(order);
        return order;
    }

}
