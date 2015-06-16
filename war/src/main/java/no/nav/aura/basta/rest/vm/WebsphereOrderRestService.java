package no.nav.aura.basta.rest.vm;

import static no.nav.aura.envconfig.client.ResourceTypeDO.Certificate;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddleWareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OSType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorEnvironmentClass;
import no.nav.aura.basta.backend.vmware.orchestrator.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Fact;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.ProvisionRequest2;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/vm/orders/was")
@Transactional
public class WebsphereOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(WebsphereOrderRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private OrchestratorService orchestratorService;
    
    @Inject
    private FasitRestClient fasit;


	@POST
    @Path("node")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJbossNode(Map<String, String> map, @Context UriInfo uriInfo) {
		VMOrderInput input = new VMOrderInput(map);
        input.setMiddleWareType(MiddleWareType.was);
		Guard.checkAccessToEnvironmentClass(input);
        
		
		Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new was node order {} with input {}", order.getId(), map);
		URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
		URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
		ProvisionRequest2 request = new ProvisionRequest2(OrchestratorEnvironmentClass.convert(input.getEnvironmentClass(), false), vmcreateCallbackUri,
				logCallabackUri);
		
		for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(Zone.fss, OSType.rhel60, MiddleWareType.was, findClassification(map), input.getCpuCount(), input.getMemory());
            vm.setExtraDiskAsGig(input.getExtraDisk());
            if (input.getDescription() == null) {
                vm.setDescription("was node");
            } else {
                vm.setDescription(input.getDescription());
            }
            vm.addPuppetFact(FactType.cloud_app_was_mgr, getDmgr(input));
            
			request.addVm(vm);
		}
		order = sendToOrchestrator(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
	}

    private String getDmgr(VMOrderInput input) {
        return getFasitResource(ResourceTypeDO.DeploymentManager, "wasDmgr", input);
    }

    private String getFasitResource(ResourceTypeDO type, String alias, VMOrderInput input) {
        // return fasit.getResource(input.getEnvironmentName(), alias, type, input., appName)
        return null;
    }

    private boolean existsInFasit(ServiceUserAccount serviceUserAccount) {
        return fasit.resourceExists(EnvClass.valueOf(serviceUserAccount.getEnvironmentClass().name()), null, DomainDO.fromFqdn(serviceUserAccount.getDomainFqdn()), serviceUserAccount.getApplicationName(),
                Certificate, serviceUserAccount.getAlias());
    }

    private Classification findClassification(Map<String, String> map) {
        VMOrderInput input = new VMOrderInput(map);
        return input.getClassification();
    }

	private Order sendToOrchestrator(Order order, OrchestatorRequest request) {

		WorkflowToken workflowToken;
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "provisioning", StatusLogLevel.info));
		workflowToken = orchestratorService.provision(request);
		order.setExternalId(workflowToken.getId());
        order.setExternalRequest(VmOrderRestService.convertXmlToString(request.censore()));

		order = orderRepository.save(order);
		return order;
	}


    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
