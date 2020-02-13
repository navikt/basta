package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorEnvironmentClass;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
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

import static no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType.*;

@Component
@Path("/vm/orders")
@Transactional
public class LinuxOrderRestService extends AbstractVmOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(LinuxOrderRestService.class);

    public LinuxOrderRestService() {}

    @Inject
    public LinuxOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient) {
        super(orderRepository, orchestratorClient);
    }

    @POST
    @Path("/linux")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewPlainLinux(Map<String, String> map, @Context UriInfo uriInfo) {
        return createNode(map, linux, uriInfo);
    }

    @POST
    @Path("/devtools")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewDevToolsServer(Map<String, String> map, @Context UriInfo uriInfo) {
        return createNode(map, devtools, uriInfo);

    }

    @POST
    @Path("/containerlinux")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewContainerLinux(Map<String, String> map, @Context UriInfo uriInfo) {
        return createNode(map, containerlinux, uriInfo);

    }

    @POST
    @Path("/flatcarlinux")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewFlatcarLinux(Map<String, String> map, @Context UriInfo uriInfo) {
        return createNode(map, flatcarlinux, uriInfo);

    }

    private boolean isIAppDevToolsServer(Map<String, String> map, MiddlewareType middlewareType) {
        return devtools.equals(middlewareType) && map.get("zone").equals("iapp");
    }

    private boolean isContainerLinux(MiddlewareType middlewareType) {
        return containerlinux.equals(middlewareType);
    }

    private boolean isFlatcarLinux(MiddlewareType middlewareType) { return flatcarlinux.equals(middlewareType); }

    public Response createNode(Map<String, String> map, MiddlewareType middlewareType, UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        boolean iAppDevToolsServer = isIAppDevToolsServer(map, middlewareType);
        boolean containerLinuxServer = isContainerLinux(middlewareType);
        boolean flatcarLinuxServer = isFlatcarLinux(middlewareType);

        // need to handle iapp devtools server as a special case. We want all develpopers to be allowed to order these servers without needing prod access.
        // need to set middlewaretype as linux to keep orchestrator from creating this as a devillo dev server.
        if (iAppDevToolsServer) {
            Guard.checkAccessToEnvironmentClass(EnvironmentClass.u);
            input.setMiddlewareType(linux);
        }
        else if (containerLinuxServer) {
            Guard.checkAccessToEnvironmentClass(input);
            input.setMiddlewareType(containerlinux);
        }
        else if (flatcarLinuxServer) {
            Guard.checkAccessToEnvironmentClass(input);
            input.setMiddlewareType(flatcarlinux);
        } else {
            Guard.checkAccessToEnvironmentClass(input);
            input.setMiddlewareType(middlewareType);
        }

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new linux order {} with input {}", order.getId(), map);
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new ProvisionRequest(OrchestratorEnvironmentClass.convert(input), input, vmcreateCallbackUri, logCallabackUri);

        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact(FactType.cloud_vm_ibmsw, input.hasIbmSoftware());

            if (iAppDevToolsServer) {
                logger.info("Setting puppet fact cloud_vm_type to iapp_utv");
                vm.addPuppetFact("vpn_accessible", "true");
            }
            vm.setClassification(Classification.custom);
            request.addVm(vm);
        }

        order = executeProvisionOrder(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
    }
}
