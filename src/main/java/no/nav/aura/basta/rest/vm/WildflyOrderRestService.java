package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.security.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

@Component
@Path("/vm/orders/wildfly")
@Transactional
public class WildflyOrderRestService extends AbstractVmOrderRestService{

    private static final Logger logger = LoggerFactory.getLogger(WildflyOrderRestService.class);

    private OrderRepository orderRepository;

    private OrchestratorClient orchestratorClient;

    public WildflyOrderRestService() {}

    @Inject
    public WildflyOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient) {
        super(orderRepository, orchestratorClient);
        this.orderRepository = orderRepository;
        this.orchestratorClient = orchestratorClient;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createWildflyNode(Map<String, String> map, @Context UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);

        String wildflyVersion = input.getOptional("wildflyVersion").orElse("wildfly21");
        String javaVersion = input.getOptional("javaVersion").orElse("OpenJDK11");

        input.setClassification(findClassification(input.copy()));
        input.setMiddlewareType(Objects.requireNonNull(getMiddlewareType(wildflyVersion)));

        if (input.getDescription() == null) {
            input.setDescription("wildfly node");
        }

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new wildfly order {} with input {}", order.getId(), map);
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallabackUri);
        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact("cloud_java_version", javaVersion);
            request.addVm(vm);
        }
        order = executeProvisionOrder(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
    }

    private MiddlewareType getMiddlewareType(String wildflyVersion) {
        switch (wildflyVersion) {
            case "wildfly17":
                return MiddlewareType.wildfly_17;
            case "wildfly19":
                return MiddlewareType.wildfly_19;
            case "wildfly21":
                return MiddlewareType.wildfly_21;
            default:
                return null;
        }
    }

    private Classification findClassification(Map<String, String> map) {
        VMOrderInput input = new VMOrderInput(map);
        return input.getClassification();
    }



}
