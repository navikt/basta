package no.nav.aura.basta.rest.bigip;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.bigip.ActiveBigIPInstanceFinder;
import no.nav.aura.basta.backend.dns.DnsService;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.ValidationHelper;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;


@Component(value = "test")
@Path("/v1/bigip")
public class BigIPOrderRestService {

    private static final Logger log = LoggerFactory.getLogger(BigIPOrderRestService.class);
    private OrderRepository orderRepository;
    private BigIPClient bigIPClient;
    private FasitUpdateService fasitUpdateService;
    private FasitRestClient fasitRestClient;
    private ActiveBigIPInstanceFinder activeInstanceFinder;
    private DnsService dnsService;
    private static final  String PARTITION="AutoProv";

    @Inject
    public BigIPOrderRestService(OrderRepository orderRepository, BigIPClient bigIPClient, FasitUpdateService fasitUpdateService, FasitRestClient fasitRestClient, ActiveBigIPInstanceFinder activeBigIPInstanceFinder, DnsService dnsService) {
        this.orderRepository = orderRepository;
        this.bigIPClient = bigIPClient;
        this.fasitUpdateService = fasitUpdateService;
        this.fasitRestClient = fasitRestClient;
        this.activeInstanceFinder = activeBigIPInstanceFinder;
        this.dnsService = dnsService;
    }

    @POST
    @Consumes("application/json")
    public Response createBigIpConfig(Map<String, String> request) {
        log.debug("Got request with payload {}", request);
        validateSchema(request);
        BigIPOrderInput input = new BigIPOrderInput(request);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        ResourceElement bigipResource = getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input);
        //skriv lbconfig

        if (bigipResource != null) {
            setupBigIPClient(bigipResource);
            Map pool = bigIPClient.getPool(createPoolName(input.getEnvironmentName(), input.getApplicationName()));
            bigIPClient.createPool(createPoolName(input.getEnvironmentName(), input.getEnvironmentName()));
            Optional<Map> virtualServer = bigIPClient.getVirtualServer(input.getVirtualServer());
            if(!virtualServer.isPresent()){
                return Response.status(Response.Status.NOT_FOUND).entity("Virtual server '"+ input.getVirtualServer()+ " 'not found").build();
            }
            String policyName = getPolicyName(virtualServer.get());
            System.out.println(policyName);


            //   Optional<Map> vs = bigIPClient.getVirtualServer(createVirtualServerName(input.getEnvironmentName()));
            //  String ip = getIpFrom((String) vs.get().get("destination"));
            //response.put("dns", dnsService.getHostNamesFor(ip));

        }

        return Response.ok("WOOOOOT").build();
    }

    private String getPolicyName(Map virtualServer) {
        Map policiesReference = (Map)virtualServer.get("policiesReference");
        List<Map<String,String>> items = (List<Map<String,String>>) policiesReference.get("items");
        return items.get(0).get("name");
    }


    public static void validateSchema(Map<String, String> request) {
        ValidationHelper.validateRequest("/validation/createBigIPConfigSchema.json", request);

    }

    private void setupBigIPClient(ResourceElement loadBalancer) {

        String username = loadBalancer.getPropertyString("username");
        String password = fasitRestClient.getSecret(loadBalancer.getPropertyUri("password"));
        bigIPClient.setCredentials(username, password);

        String activeInstance = activeInstanceFinder.getActiveBigIPInstance(loadBalancer, username, password);
        if (activeInstance == null) {
            throw new RuntimeException("Unable to find any active BIG-IP instance");
        }
        bigIPClient.setHostname(activeInstance);
    }

    @GET
    @Path("/virtualservers/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVirtualServers(@Context UriInfo uriInfo) {
        HashMap<String, Object> response = new HashMap<>();
        BigIPOrderInput input = parse(uriInfo);
        ResourceElement bigipResource = getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input);
        if (bigipResource != null) {
            setupBigIPClient(bigipResource);
            List<Map<String,Object>> virtualServers = bigIPClient.getVirtualServers(PARTITION);
            List<String> names = virtualServers.stream().map(map -> (String)map.get("name")).collect(Collectors.toList());
            return Response.ok(names).build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).entity("BigIP resource not found").build();
        }
    }

    @GET
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validate(@Context UriInfo uriInfo) {
        HashMap<String, Object> response = new HashMap<>();
        BigIPOrderInput input = parse(uriInfo);

        ResourceElement bigipResource = getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input);
        ResourceElement lbconfigResource = getFasitResource(ResourceTypeDO.LoadBalancerConfig, "lbconfig", input);

        response.put("bigIpResourceExists", bigipResource != null ? true : false);
        response.put("lbConfigResourceExists", lbconfigResource != null ? true : false);

        if (bigipResource != null) {
            setupBigIPClient(bigipResource);
            Map pool = bigIPClient.getPool(createPoolName(input.getEnvironmentName(), input.getApplicationName()));
            response.put("bigIpPoolExists", pool != null ? pool.get("name"): null);

            String virtualServer = input.getVirtualServer();
            String contextRoots = input.getContextRoots();
            if (virtualServer != null && contextRoots != null) {
                response.put("conflictingContextRoots", checkForConflictingContextRoots(virtualServer, contextRoots));
            }
            //   Optional<Map> vs = bigIPClient.getVirtualServer(createVirtualServerName(input.getEnvironmentName()));
            //  String ip = getIpFrom((String) vs.get().get("destination"));
            //response.put("dns", dnsService.getHostNamesFor(ip));

        }

        return Response.ok(response).build();
    }

    private boolean checkForConflictingContextRoots(String virtualServer, String contextRoots) {
        return true;
    }

    private String createPoolName(String environmentName, String application) {
        return "pool_autodeploy-test-config-bare_u99"; //"pool_"+ application + "_" + environmentName + "https_auto";
    }

    private String createVirtualServerName(String environmentName) {
        return "vs_utv_itjenester-u99.oera.no_https"; //"vs_skya_"+environmentName;
    }

    private BigIPOrderInput parse(@Context UriInfo uriInfo) {
        HashMap<String, String> request = ValidationHelper.queryParamsAsMap(uriInfo.getQueryParameters());
        ValidationHelper.validateRequiredParams(request, "environmentClass", "environmentName", "zone", "application");
        return new BigIPOrderInput(request);
    }


    private String getIpFrom(String destination) {
        return destination.split("/")[2].split(":")[0];
    }


    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, BigIPOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        DomainDO.EnvClass envClass = DomainDO.EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasitRestClient.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), input.getApplicationName(), type, alias);
        return resources.size() == 1 ? resources.iterator().next() : null;
    }

}
