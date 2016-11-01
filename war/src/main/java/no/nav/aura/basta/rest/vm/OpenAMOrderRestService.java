package no.nav.aura.basta.rest.vm;

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
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.StatusLogHelper;
import no.nav.aura.basta.util.StringHelper;
import no.nav.aura.envconfig.client.*;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.PropertyElement.Type;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.aura.fasit.client.model.ExposedResource;
import no.nav.aura.fasit.client.model.RegisterApplicationInstancePayload;
import no.nav.aura.fasit.client.model.UsedResource;
import org.jboss.resteasy.spi.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Path("/vm/orders/openam")
@Transactional
public class OpenAMOrderRestService {

    private static final String OPEN_AM_APPNAME = "openAm";
    public static final String OPENAM_ACCESS_GROUP = "RA_OpenAMAdmin";

    private static final Logger logger = LoggerFactory.getLogger(OpenAMOrderRestService.class);

    private OrderRepository orderRepository;

    private OrchestratorService orchestratorService;

    private FasitRestClient fasit;

    protected OpenAMOrderRestService() {
    }

    @Inject
    public OpenAMOrderRestService(OrderRepository orderRepository, OrchestratorService orchestratorService, FasitRestClient fasit) {
        super();
        this.orderRepository = orderRepository;
        this.orchestratorService = orchestratorService;
        this.fasit = fasit;
    }

    @POST
    @Path("server")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOpenAMServer(Map<String, String> map, @Context UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);

        List<String> validation = validateServerWithFasit(input.getEnvironmentClass(), input.getEnvironmentName());
        if (!validation.isEmpty()) {
            throw new BadRequestException("Valdiation failure " + validation);
        }
        input.setNodeType(NodeType.OPENAM_SERVER);
        input.setClassification(Classification.standard);
        input.setDescription("openAM server node");
        input.setCpuCount(2);
        input.setMemory(2);
        input.setApplicationMappingName(OPEN_AM_APPNAME);

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new openam order {} with input {}", order.getId(), map);

        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI logCallbackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallbackUri);

        String amldlapPwd = StringHelper.generateRandom(14);
        String amadminPwd = resolvePassword(getAmAdminUser(input));
        String essoPasswd = resolvePassword(getEssoUser(input));
        String sblWsPassword = resolvePassword(getSblWsUser(input));

        order.getStatusLogs().add(new OrderStatusLog("Basta", "generated passwords", "openam"));

        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.setType(MiddlewareType.openam12_server);
            vm.addPuppetFact(FactType.cloud_openam_esso_pwd, essoPasswd);
            vm.setChangeDeployerPassword(true);
            vm.addPuppetFact(FactType.cloud_openam_arb_pwd, sblWsPassword);
            vm.addPuppetFact(FactType.cloud_openam_admin_pwd, amadminPwd); // pålogging til console + ssoadm script Global
            vm.addPuppetFact(FactType.cloud_openam_amldap_pwd, amldlapPwd); // lokal ldap på server? Kun på server
            request.addVm(vm);
        }

        order = sendToOrchestrator(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
    }

    /** Registering openam as an application in fasit */
    public void registrerOpenAmApplication(Order order, VMOrderResult result, VMOrderInput input) {
        if (result.hostnames().size() == input.getServerCount()) {
            order.getStatusLogs().add(new OrderStatusLog("Basta", "Har laget " + input.getServerCount() + " servere. Regner med at denne er ferdig", "provision"));
            try {
                List<NodeDO> openAmServerNodes = findOpenAmNodes(input.getEnvironmentName());

                RegisterApplicationInstancePayload payload = new RegisterApplicationInstancePayload("openAm", "12", input.getEnvironmentName());

                payload.addUsedResources(new UsedResource(getAmAdminUser(input)), new UsedResource(getEssoUser(input)), new UsedResource(getSblWsUser(input)));
                payload.setNodes(result.hostnames());
                Map<String, String> properties = new HashMap<>();
                NodeDO masterNode = openAmServerNodes.get(0);
                properties.put("hostname", masterNode.getHostname());
                properties.put("username", masterNode.getUsername());
                properties.put("password", fasit.getSecret(masterNode.getPasswordRef()));
                properties.put("restUrl", getRestUrl(input));
                properties.put("logoutUrl", getLogoutUrl(input));

                ExposedResource exposedResource = new ExposedResource(ResourceTypeDO.OpenAm.name(), "openam", properties);
                exposedResource.setAccessAdGroups(OPENAM_ACCESS_GROUP);
                payload.getExposedResources().add(exposedResource);
                fasit.registerApplication(payload, "Registerer openam applikasjon etter provisjonering");
                order.addStatuslogInfo("Registerer openAmApplikasjon i fasit");
            } catch (RuntimeException e) {

                order.addStatuslogWarning( "Registering openam application i Fasit " + StatusLogHelper.abbreviateExceptionMessage(e));
                logger.error("Error updating Fasit with order " + order.getId(), e);
            }
            orderRepository.save(order);
        }
    }

    private String getRestUrl(VMOrderInput input) {
        if (input.getEnvironmentClass() == EnvironmentClass.p) {
            return "https://itjenester.oera.no/esso";
        }
        return String.format("https://itjenester-%s.oera.no/esso", input.getEnvironmentName());
    }

    private String getLogoutUrl(VMOrderInput input) {
        if (input.getEnvironmentClass() == EnvironmentClass.p) {
            return "https://tjenester.nav.no/esso/logout";
        }
        return String.format("https://tjenester-%s.nav.no/esso/logout", input.getEnvironmentName());
    }

    @GET
    @Path("server/validation")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> validateServerWithFasit(@QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("environmentName") String environment) {
        logger.info("validating for {}", environment);

        List<String> validations = new ArrayList<>();
        Domain domain = Domain.findBy(envClass, Zone.sbs);
        String scope = String.format(" %s|%s|%s", envClass, environment, domain);
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(envClass);
        input.setEnvironmentName(environment);
        List<NodeDO> openAmServerNodes = findOpenAmNodes(environment);
        if (!openAmServerNodes.isEmpty()) {
            validations.add(String.format("Fasit already has openam servers in %s. Remove them if you want to create new", environment));
        }

        if (getAmAdminUser(input) == null) {
            validations.add(String.format("Missing required fasit resource amAdminUser of type Credential in %s", scope));
        }
        if (getEssoUser(input) == null) {
            validations.add(String.format("Missing required fasit resource srvEsso of type Credential in %s", scope));
        }
        if (getSblWsUser(input) == null) {
            validations.add(String.format("Missing required fasit resource srvSblWs of type Credential in %s", scope));
        }

        Collection<ResourceElement> openAmResources = fasit.findResources(EnvClass.valueOf(envClass.name()), environment, DomainDO.fromFqdn(domain.getFqn()), null, ResourceTypeDO.OpenAm, "openAm");
        if (!openAmResources.isEmpty()) {
            for (ResourceElement fasitResource : openAmResources) {
                if (environment.equalsIgnoreCase(fasitResource.getEnvironmentName())) {
                    validations.add(String.format("Resource OpenAm already exist in fasit for scope %s. This must be removed to create a new", scope));
                }
            }
        }

        return validations;
    }

    @POST
    @Path("proxy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOpenProxy(Map<String, String> map, @Context UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);

        List<String> validation = validateProxyWithFasit(input.getEnvironmentClass(), input.getEnvironmentName());
        if (!validation.isEmpty()) {
            throw new BadRequestException("Valdiation failure " + validation);
        }
        input.setNodeType(NodeType.OPENAM_PROXY);
        input.setClassification(Classification.standard);
        input.setDescription("openAM proxy node");
        input.setZone(Zone.dmz);
        input.setCpuCount(2);
        input.setMemory(1);
        input.setApplicationMappingName(OPEN_AM_APPNAME);

        Order order = orderRepository.save(new Order(OrderType.VM, OrderOperation.CREATE, input));
        logger.info("Creating new openam proxy order {} with input {}", order.getId(), map);

        URI vmcreateCallbackUri = VmOrdersRestApi.apiCreateCallbackUri(uriInfo, order.getId());
        URI logCallbackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallbackUri);

        NodeDO masterAmNode = findOpenAmNodes(input.getEnvironmentName()).get(0);

        for (int i = 0; i < input.getServerCount(); i++) {
            Vm vm = new Vm(input);
            vm.setType(MiddlewareType.openam12_proxy);
            vm.setChangeDeployerPassword(true);

            vm.addPuppetFact(FactType.cloud_openam_master, masterAmNode.getHostname());
            vm.addPuppetFact("cloud_openam_node_id", String.valueOf(i + 1));
            request.addVm(vm);
        }

        order = sendToOrchestrator(order, request);
        return Response.created(UriFactory.getOrderUri(uriInfo, order.getId())).entity(order.asOrderDO(uriInfo)).build();
    }

    @GET
    @Path("proxy/validation")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> validateProxyWithFasit(@QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("environmentName") String environment) {
        logger.debug("validating proxy for {}", environment);

        List<String> validations = new ArrayList<>();
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(envClass);
        input.setEnvironmentName(environment);
        List<NodeDO> openAmServerNodes = findOpenAmNodes(environment);
        if (openAmServerNodes.isEmpty()) {
            validations.add(String.format("No openam server is registered in %s", environment));
        }

        return validations;
    }

    private List<NodeDO> findOpenAmNodes(String environment) {
        ApplicationInstanceDO openAmInstance;
        try {
            openAmInstance = fasit.getApplicationInstance(environment, OPEN_AM_APPNAME);
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
        if (openAmInstance != null) {
            return openAmInstance.getCluster().getNodesAsList().stream()
                    .filter(node -> node.getPlatformType() == PlatformTypeDO.OPENAM_SERVER)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * ADbruker registert i fasit. Brukes til endagspålogging ,en pr miljøklasse. Ligger i oerastacken
     */
    private ResourceElement getEssoUser(VMOrderInput input) {
        return getFasitResource(ResourceTypeDO.Credential, "srvEsso", input);
    }

    /**
     * Adbruker?, Brukes til ? en pr miljøklasse oera
     */
    private ResourceElement getSblWsUser(VMOrderInput input) {
        return getFasitResource(ResourceTypeDO.Credential, "srvSblWs", input);
    }

    /** Adminbruker for openam instansen. Brukes til å logge på gui, og utføre ssoadm commandoer */
    private ResourceElement getAmAdminUser(VMOrderInput input) {
        return getFasitResource(ResourceTypeDO.Credential, "amAdminUser", input);
    }

    private String resolvePassword(ResourceElement resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Can not resolve password for null");
        }
        return resolveProperty(resource, "password");
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

    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, VMOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), Zone.sbs);
        EnvClass envClass = EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasit.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), null, type, alias);
        return resources.isEmpty() ? null : resources.iterator().next();
    }

    private Order sendToOrchestrator(Order order, OrchestatorRequest request) {
        OrchestratorUtil.censore(request);
//        WorkflowToken workflowToken;
        order.addStatuslogInfo("Calling Orchestrator for provisioning");
/*        workflowToken = orchestratorService.provision(request);
        order.setExternalId(workflowToken.getId());*/
        order.setExternalRequest(OrchestratorUtil.censore(request));
        order = orderRepository.save(order);
        return order;
    }
}
