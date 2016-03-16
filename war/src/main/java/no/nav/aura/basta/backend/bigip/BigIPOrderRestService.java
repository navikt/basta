package no.nav.aura.basta.backend.bigip;

import com.google.common.base.Optional;
import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitUpdateService;
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

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Component
@Path("/v1/bigip")
public class BigIPOrderRestService {

    private static final Logger log = LoggerFactory.getLogger(BigIPOrderRestService.class);
    private OrderRepository orderRepository;
    private BigIPClient bigIPClient;
    private FasitUpdateService fasitUpdateService;
    private FasitRestClient fasitRestClient;
    private ActiveBigIPInstanceFinder activeInstanceFinder;
    private DnsService dnsService;

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
        BigIPOrderInput input = new BigIPOrderInput(request);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());
        validateSchema(request);
        return Response.ok("WOOOOOT").build();
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
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateBigIpInstance(@Context UriInfo uriInfo) {

        HashMap<String, Object> response = new HashMap<>();
        HashMap<String, String> request = ValidationHelper.queryParamsAsMap(uriInfo.getQueryParameters());
        ValidationHelper.validateAllParams(request);
        BigIPOrderInput input = new BigIPOrderInput(request);

        ResourceElement bigipResource = getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input);
        ResourceElement lbconfigResource = getFasitResource(ResourceTypeDO.LoadBalancerConfig, "lbconfig", input);

        response.put("bigip", bigipResource != null ? true : false);
        response.put("lbconfig", lbconfigResource != null ? true : false);

        if (bigipResource != null) {
            setupBigIPClient(bigipResource);
            Optional<Map> vs = bigIPClient.getVirtualServer(createVirtualServerName(input.getEnvironmentName()));
            response.put("virtualServerExists", vs.isPresent() ? true : false);
            String ip = getIpFrom((String) vs.get().get("destination"));
            response.put("vs,", ip);
            response.put("dns", dnsService.getUsers());

        }

        return Response.ok(response).build();

    }

    private String getIpFrom(String destination) {
        return destination.split("/")[2].split(":")[0];
    }


    private String createVirtualServerName(String environmentName) {
        return "vs_utv_itjenester-u99.oera.no_https"; //"vs_skya_"+environmentName;
    }


    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, BigIPOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        DomainDO.EnvClass envClass = DomainDO.EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasitRestClient.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), input.getApplicationName(), type, alias);
        return resources.size() == 1 ? resources.iterator().next() : null;
    }

}
