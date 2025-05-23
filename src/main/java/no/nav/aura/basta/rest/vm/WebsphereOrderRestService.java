package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.*;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.security.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Path("/vm/orders/was")
@Transactional
public class WebsphereOrderRestService extends AbstractVmOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(WebsphereOrderRestService.class);

//  for cglib
    public WebsphereOrderRestService() {
    }

    @Inject
    public WebsphereOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient, RestClient restClient) {
        super(orderRepository, orchestratorClient, restClient);
        this.orderRepository = orderRepository;
        this.orchestratorClient = orchestratorClient;
    }

    @POST
    @Path("node")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createWasNode(Map<String, String> map, @Context UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);
        List<String> validation = validateRequiredFasitResourcesForNode(input.getEnvironmentClass(), input.getZone(), input.getEnvironmentName(), input.getNodeType());

        if (!validation.isEmpty()) {
            throw new IllegalArgumentException("Required fasit resources is not present " + validation);
        }

        input.setMiddlewareType(MiddlewareType.was_9);
        input.setClassification(findClassification(input.copy()));
        if (input.getDescription() == null) {
            input.setDescription("was node in " + input.getEnvironmentName());
        }

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new was node order {} with input {}", order.getId(), map);
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallabackUri);
        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact(FactType.cloud_app_was_mgr, getWasDmgr(input));
            vm.addPuppetFact(FactType.cloud_app_was_type, "node");
            vm.addPuppetFact(FactType.cloud_app_was_adminuser, getWasAdminUser(input, "username"));
            vm.addPuppetFact(FactType.cloud_app_was_adminpwd, getWasAdminUser(input, "password"));
            request.addVm(vm);
        }
        order = executeProvisionOrder(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
    }

    @POST
    @Path("dmgr")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createWasDmgr(Map<String, String> map, @Context UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);
        List<String> validation = validateRequiredFasitResourcesForDmgr(input.getEnvironmentClass(), input.getZone(), input.getEnvironmentName(), input.getNodeType());

        if (!validation.isEmpty()) {
            throw new IllegalArgumentException("Required fasit resources is not present " + validation);
        }

        input.setMiddlewareType(MiddlewareType.was_9);
        input.setClassification(Classification.custom);
        input.setApplicationMappingName("was-dmgr");
        input.setExtraDisk(10);
        input.setServerCount(1);
        if (input.getDescription() == null) {
            input.setDescription("Websphere deployment manager for " + input.getEnvironmentName());
        }

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new was dmgr order {} with input {}", order.getId(), map);
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallabackUri);
        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact(FactType.cloud_app_was_type, "mgr");
            vm.addPuppetFact(FactType.cloud_app_was_adminuser, getWasAdminUser(input, "username"));
            vm.addPuppetFact(FactType.cloud_app_was_adminpwd, getWasAdminUser(input, "password"));
            vm.addPuppetFact(FactType.cloud_app_ldap_binduser, getLdapBindUser(input, "username"));
            vm.addPuppetFact(FactType.cloud_app_ldap_bindpwd, getLdapBindUser(input, "password"));
            if (input.getZone() == Zone.sbs) {
                vm.addPuppetFact(FactType.cloud_app_ldap_binduser_fss, getWasLdapBindUserForFss(input, "username"));
                vm.addPuppetFact(FactType.cloud_app_ldap_bindpwd_fss, getWasLdapBindUserForFss(input, "password"));
            }
            request.addVm(vm);
        }
        order = executeProvisionOrder(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
    }

    @GET
    @Path("dmgr/validation")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> validateRequiredFasitResourcesForDmgr(@QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone, @QueryParam("environmentName") String environment,
                                                              @QueryParam("nodeType") NodeType nodeType) {
        List<String> validations = new ArrayList<>();
        Domain domain = Domain.findBy(envClass, zone);
        String scope = String.format(" %s|%s|%s", envClass, environment, domain);
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(envClass);
        input.setZone(zone);
        input.setEnvironmentName(environment);
        input.setNodeType(nodeType);

        if (getWasDmgr(input).isPresent()) {
            validations.add(String.format("Can not create more than one %s in %s", getWasDmgrAlias(input.getNodeType()), scope));
        }

        if (!getWasAdminUser(input, "username").isPresent()) {
            validations.add(String.format("Missing requried fasit resource wsAdminUser of type Credential in scope %s", scope));
        }

        if (!getLdapBindUser(input, "username").isPresent()) {
            validations.add(String.format("Missing requried fasit resource wasLdapUser of type Credential in scope %s", scope));
        }

        if (input.getZone() == Zone.sbs && getWasLdapBindUserForFss(input, "username") == null) {
            validations.add(String.format("Missing requried fasit resource wasLdapUser of type Credential in FSS"));
        }

        return validations;
    }

    @GET
    @Path("node/validation")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> validateRequiredFasitResourcesForNode(@QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone, @QueryParam("environmentName") String environment,
                                                              @QueryParam("nodeType") NodeType nodeType) {
        List<String> validations = new ArrayList<>();
        Domain domain = Domain.findBy(envClass, zone);
        String scope = String.format(" %s|%s|%s", envClass, environment, domain);
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(envClass);
        input.setZone(zone);
        input.setEnvironmentName(environment);
        input.setNodeType(nodeType);

        if (!getWasDmgr(input).isPresent()) {
            validations.add(String.format("Missing requried fasit resource %s of type DeploymentManager in scope %s", getWasDmgrAlias(input.getNodeType()), scope));
        }
        if (!getWasAdminUser(input, "username").isPresent()) {
            validations.add(String.format("Missing requried fasit resource wsAdminUser of type Credential in scope %s", scope));
        }
        return validations;
    }

    private Optional<String> getWasDmgr(VMOrderInput input) {
        String alias = getWasDmgrAlias(input.getNodeType());
        Optional<ResourcePayload> dmgr = getFasitResource(ResourceType.deploymentmanager, alias, input);
        return resolveProperty(dmgr, "hostname");
    }

    private String getWasDmgrAlias(NodeType nodeType) {
        return NodeType.WAS9_DEPLOYMENT_MANAGER.equals(nodeType) || NodeType.WAS9_NODES.equals(nodeType) ? "was9Dmgr" : "wasDmgr";
    }

    private Optional<String> getWasAdminUser(VMOrderInput input, String property) {
        Optional<ResourcePayload> wsAdminUser = getFasitResource(ResourceType.credential, "wsadminUser", input);
        return resolveProperty(wsAdminUser, property);
    }

    private Optional<String> getLdapBindUser(VMOrderInput input, String property) {
        Optional<ResourcePayload> ldapBindUser = getFasitResource(ResourceType.credential, "wasLdapUser", input);
        return resolveProperty(ldapBindUser, property);
    }

    private Classification findClassification(Map<String, String> map) {
        VMOrderInput input = new VMOrderInput(map);
        return input.getClassification();
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
