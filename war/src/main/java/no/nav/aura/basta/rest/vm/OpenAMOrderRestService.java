package no.nav.aura.basta.rest.vm;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.FasitUpdateService;
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
import no.nav.aura.basta.domain.result.vm.ResultStatus;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.PasswordGenerator;
import no.nav.aura.envconfig.client.ApplicationInstanceDO;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.aura.envconfig.client.PlatformTypeDO;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.aura.fasit.client.model.ExposedResource;
import no.nav.aura.fasit.client.model.RegisterApplicationInstancePayload;
import no.nav.aura.fasit.client.model.UsedResource;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.resteasy.spi.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@Component
@Path("/vm/orders/openam")
@Transactional
public class OpenAMOrderRestService {

    private static final String OPEN_AM_APPNAME = "openAm";

    private static final Logger logger = LoggerFactory.getLogger(OpenAMOrderRestService.class);

    private OrderRepository orderRepository;

    private OrchestratorService orchestratorService;

    private FasitRestClient fasit;

    private FasitUpdateService fasitUpdateService;

    protected OpenAMOrderRestService() {
    }

    @Inject
    public OpenAMOrderRestService(OrderRepository orderRepository, OrchestratorService orchestratorService, FasitRestClient fasit) {
        super();
        this.orderRepository = orderRepository;
        this.orchestratorService = orchestratorService;
        this.fasit = fasit;
        this.fasitUpdateService = new FasitUpdateService(fasit);
    }

    @POST
    @Path("server")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOpenAMServer(Map<String, String> map, @Context UriInfo uriInfo) {
        VMOrderInput input = new VMOrderInput(map);
        Guard.checkAccessToEnvironmentClass(input);
        
        List<String> validation = validateServerWithFasit(input.getEnvironmentClass(), input.getEnvironmentName());
        if(!validation.isEmpty()){
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

        URI vmcreateCallbackUri = uriInfo.getBaseUriBuilder().clone().path(getClass()).path(getClass(), "provisionOpenAmCallback").build(order.getId());
        URI logCallbackUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        ProvisionRequest request = new ProvisionRequest(input, vmcreateCallbackUri, logCallbackUri);

        String amldlapPwd = PasswordGenerator.generate(14);
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

    /** Callback fra orchestrator ved server create */
    @PUT
    @Path("{orderId}")
    @Consumes(MediaType.APPLICATION_XML)
    public void provisionOpenAmCallback(@PathParam("orderId") Long orderId, OrchestratorNodeDOList vmList) {
        List<OrchestratorNodeDO> vms = vmList.getVms();
        logger.info("Received list of with {} vms as orderid {}", vms.size(), orderId);
        Order order = orderRepository.findOne(orderId);
        VMOrderResult result = order.getResultAs(VMOrderResult.class);
        VMOrderInput input = order.getInputAs(VMOrderInput.class);
        for (OrchestratorNodeDO vm : vms) {
            logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
            result.addHostnameWithStatusAndNodeType(vm.getHostName(), ResultStatus.ACTIVE, input.getNodeType());
            fasitUpdateService.createFasitEntity(order, vm);
            order.getStatusLogs().add(new OrderStatusLog("Basta", String.format("Created %s", vm.getHostName()), "provision"));
            orderRepository.save(order);
        }
        if (result.hostnames().size() == input.getServerCount()) {
            order.getStatusLogs().add(new OrderStatusLog("Basta", "Har laget " + input.getServerCount() + " servere. Regner med at denne er ferdig", "provision"));
            order.getStatusLogs().add(new OrderStatusLog("Basta", "Registerer openAmApplikasjon i fasit", "fasit registering"));
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

            payload.getExposedResources().add(new ExposedResource(ResourceTypeDO.OpenAm, "openam", properties));
            fasit.registerApplication(payload, "Registerer openam applikasjon etter provisjonering");

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
            validations.add(String.format("Missing requried fasit resource amAdminUser of type Credential in %s", scope));
        }
        if (getEssoUser(input) == null) {
            validations.add(String.format("Missing requried fasit resource srvEsso of type Credential in %s", scope));
        }
        if (getSblWsUser(input) == null) {
            validations.add(String.format("Missing requried fasit resource srvSblWs of type Credential in %s", scope));
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
            return FluentIterable.from(openAmInstance.getCluster().getNodesAsList()).filter(new Predicate<NodeDO>() {

                @Override
                public boolean apply(NodeDO input) {
                    return PlatformTypeDO.OPENAM_SERVER.equals(input.getPlatformType());
                }
            }).toList();
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
        return fasit.getSecret(resource.getPropertyUri("password"));
    }



    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, VMOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), Zone.sbs);
        EnvClass envClass = EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasit.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), null, type, alias);
        return resources.isEmpty() ? null : resources.iterator().next();
    }


    private Order sendToOrchestrator(Order order, OrchestatorRequest request) {
        OrchestratorUtil.censore(request);
        WorkflowToken workflowToken;
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "provisioning", StatusLogLevel.info));
        workflowToken = orchestratorService.provision(request);
        order.setExternalId(workflowToken.getId());
        order.setExternalRequest(OrchestratorUtil.censore(request));
        order = orderRepository.save(order);
        return order;
    }
}
