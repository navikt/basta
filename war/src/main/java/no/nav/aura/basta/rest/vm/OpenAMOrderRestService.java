package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitReadService;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddleWareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorUtil;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.ProvisionRequest2;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.util.PasswordGenerator;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.generated.vmware.ws.WorkflowToken;
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

@Component
@Path("/vm/orders/openam")
@Transactional
public class OpenAMOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAMOrderRestService.class);

    private OrderRepository orderRepository;

    private OrchestratorService orchestratorService;

    private FasitReadService fasitReadService;

    private FasitRestClient fasit;

    protected OpenAMOrderRestService() {
    }

    @Inject
    public OpenAMOrderRestService(OrderRepository orderRepository, OrchestratorService orchestratorService, FasitReadService fasitReadService, FasitRestClient fasit) {
        super();
        this.orderRepository = orderRepository;
        this.orchestratorService = orchestratorService;
        this.fasitReadService = fasitReadService;
        this.fasit = fasit;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOpenAMNode(Map<String, String> map, @Context UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);

        input.setMiddleWareType(MiddleWareType.openam12_server);
        input.setClassification(Classification.standard);
        input.setDescription("openAM server node");

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new openam order {} with input {}", order.getId(), map);
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI logCallbackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest2 request = new ProvisionRequest2(input, vmcreateCallbackUri, logCallbackUri);


        String keystorePwd = PasswordGenerator.generate(14);
        String agentPwd = PasswordGenerator.generate(14);
        String amadminPwd = PasswordGenerator.generate(14);
        String amldlapPwd = PasswordGenerator.generate(14);
        String amencPwd = PasswordGenerator.generate(32);

        createFasitResource("OpenAM.keystoreuser", "keystore", keystorePwd, order);
        createFasitResource("OpenAM.agentuser", "agent", agentPwd, order);
        createFasitResource("OpenAM.amadminuser", "adadmin", amadminPwd, order);
        createFasitResource("OpenAM.amldapuser", "amldap", amldlapPwd, order);
        createFasitResource("OpenAM.amenckey", "amenc", amencPwd, order);

        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact(FactType.cloud_openam_esso_pwd, fasitReadService.getPasswordForUser(input, "srvsso"));
            vm.addPuppetFact(FactType.cloud_openam_arb_pwd, fasitReadService.getPasswordForUser(input, "srvsblws"));
            vm.addPuppetFact(FactType.cloud_openam_keystore_pwd, keystorePwd);
            vm.addPuppetFact(FactType.cloud_openam_agent_pwd, agentPwd);
            vm.addPuppetFact(FactType.cloud_openam_admin_pwd, amadminPwd);
            vm.addPuppetFact(FactType.cloud_openam_amldap_pwd, amldlapPwd);
            vm.addPuppetFact(FactType.cloud_openam_enc_key, amencPwd);
            order.getStatusLogs().add(new OrderStatusLog("Password", "generated passwords", "openam"));
            request.addVm(vm);
        }

        order = sendToOrchestrator(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
    }

    private ResourceElement createFasitResource(String alias, String userName, String password, Order order) {
        VMOrderInput input = order.getInputAs(VMOrderInput.class);
        ResourceElement fasitResource = new ResourceElement(ResourceTypeDO.Credential, alias);
        fasitResource.setEnvironmentClass(input.getEnvironmentClass().name());
        fasitResource.setApplication(input.getApplicationMappingName());
        fasitResource.setDomain(DomainDO.fromFqdn(input.getDomain().getFqn()));
        fasitResource.addProperty(new PropertyElement("username", userName));
        fasitResource.addProperty(new PropertyElement("password", password));

        return putCredentialInFasit(fasitResource, order);
    }


    private ResourceElement putCredentialInFasit(ResourceElement fasitResource, Order order) {
        VMOrderInput input = order.getInputAs(VMOrderInput.class);
        order.getStatusLogs().add(new OrderStatusLog("Fasit", "Registering credential in Fasit", "fasit"));
        fasit.setOnBehalfOf(User.getCurrentUser().getName());
        ResourceElement storedResource = fasitReadService.getFasitResource(ResourceTypeDO.Credential, fasitResource.getAlias(), input);
        if (storedResource != null) {
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Credential already exists in fasit with id " + storedResource.getId(), "fasit"));
            fasitResource.setApplication(storedResource.getApplication());
            fasitResource = fasit.updateResource(storedResource.getId(), fasitResource, "Updating credential for application " + input.getApplicationMappingName() + " in " + input.getEnvironmentClass());
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Updated credential with alias " + fasitResource.getAlias() + " and  id " + fasitResource.getId(), "fasit"));
        } else {
            fasitResource = fasit.registerResource(fasitResource, "Creating credential for application " + input.getApplicationMappingName() + " in " + input.getEnvironmentClass());
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Created new credential with alias " + fasitResource.getAlias() + " and  id " + fasitResource.getId(), "fasit"));
        }
        return fasitResource;
    }

    private Order sendToOrchestrator(Order order, OrchestatorRequest request) {

        WorkflowToken workflowToken;
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "provisioning", StatusLogLevel.info));
        workflowToken = orchestratorService.provision(request);
        order.setExternalId(workflowToken.getId());
        order.setExternalRequest(OrchestratorUtil.censore(request));
        order = orderRepository.save(order);
        return order;
    }
}
