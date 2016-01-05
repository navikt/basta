package no.nav.aura.basta.rest.vm;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorUtil;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.PropertyElement.Type;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/vm/orders/liberty")
@Transactional
public class LibertyOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(LibertyOrderRestService.class);

    private OrderRepository orderRepository;

    private OrchestratorService orchestratorService;

    private FasitRestClient fasit;

    protected LibertyOrderRestService() {
    }

    @Inject
    public LibertyOrderRestService(OrderRepository orderRepository, OrchestratorService orchestratorService, FasitRestClient fasit) {
        super();
        this.orderRepository = orderRepository;
        this.orchestratorService = orchestratorService;
        this.fasit = fasit;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLibertyNode(Map<String, String> map, @Context UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);
        List<String> validation = validatereqiredFasitResourcesForDmgr(input.getEnvironmentClass(), input.getZone(), input.getEnvironmentName());
        if (!validation.isEmpty()) {
            throw new IllegalArgumentException("Required fasit resources is not present " + validation);
        }

        input.setMiddlewareType(MiddlewareType.liberty_8);
        input.setClassification(findClassification(input.copy()));
        if (input.getDescription() == null) {
            input.setDescription("liberty node");
        }

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new liberty node order {} with input {}", order.getId(), map);
        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI logCallabackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallabackUri);
        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.addPuppetFact(FactType.cloud_app_was_adminuser, getWasAdminUser(input, "username"));
            vm.addPuppetFact(FactType.cloud_app_was_adminpwd, getWasAdminUser(input, "password"));
            vm.addPuppetFact(FactType.cloud_app_ldap_binduser, getLdapBindUser(input, "username"));
            vm.addPuppetFact(FactType.cloud_app_ldap_bindpwd, getLdapBindUser(input, "password"));
            request.addVm(vm);
        }
        order = sendToOrchestrator(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
    }

    @GET
    @Path("/validation")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> validatereqiredFasitResourcesForDmgr(@QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone, @QueryParam("environmentName") String environment) {
        List<String> validations = new ArrayList<>();
        Domain domain = Domain.findBy(envClass, zone);
        String scope = String.format(" %s|%s|%s", envClass, environment, domain);
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(envClass);
        input.setZone(zone);
        input.setEnvironmentName(environment);

        if (getLdapBindUser(input, "username") == null) {
            validations.add(String.format("Missing requried fasit resource wasLdapUser of type Credential in scope %s", scope));
        }

        return validations;
    }

    private String getWasAdminUser(VMOrderInput input, String property) {
        ResourceElement wsAdminUser = getFasitResource(ResourceTypeDO.Credential, "wsadminUser", input);
        return wsAdminUser == null ? null : resolveProperty(wsAdminUser, property);
    }

    private String getLdapBindUser(VMOrderInput input, String property) {
        ResourceElement ldapBindUser = getFasitResource(ResourceTypeDO.Credential, "wasLdapUser", input);
        return ldapBindUser == null ? null : resolveProperty(ldapBindUser, property);
    }

    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, VMOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        EnvClass envClass = EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasit.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), null, type, alias);
        return resources.isEmpty() ? null : resources.iterator().next();
    }

    private String resolveProperty(ResourceElement resource, String propertyName) {
        for (PropertyElement property : resource.getProperties()) {
            if (property.getName().equals(propertyName)) {
                if (property.getType() == Type.SECRET) {
                    return fasit.getSecret(property.getRef());
                }
                return property.getValue();
            }
        }
        throw new RuntimeException("Property " + propertyName + " not found for Fasit resource " + resource.getAlias());
    }

    private Classification findClassification(Map<String, String> map) {
        VMOrderInput input = new VMOrderInput(map);
        return input.getClassification();
    }

    private Order sendToOrchestrator(Order order, OrchestatorRequest request) {
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "provisioning", StatusLogLevel.info));
        WorkflowToken workflowToken = orchestratorService.provision(request);
        order.setExternalId(workflowToken.getId());
        order.setExternalRequest(OrchestratorUtil.censore(request));

        order = orderRepository.save(order);
        return order;
    }

}
