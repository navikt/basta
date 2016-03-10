package no.nav.aura.basta.rest.bigip;

import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitUpdateService;
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
import javax.ws.rs.core.*;
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

    @Inject
    public BigIPOrderRestService(OrderRepository orderRepository, BigIPClient bigIPClient, FasitUpdateService fasitUpdateService, FasitRestClient fasitRestClient) {
        this.orderRepository = orderRepository;
        this.bigIPClient = bigIPClient;
        this.fasitUpdateService = fasitUpdateService;
        this.fasitRestClient = fasitRestClient;
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

    @GET
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateBigIpInstance(@Context UriInfo uriInfo) {

        HashMap<String, Object> response = new HashMap<>();
        HashMap<String, String> request = ValidationHelper.queryParamsAsMap(uriInfo.getQueryParameters());
        ValidationHelper.validateAllParams(request);
        BigIPOrderInput input = new BigIPOrderInput(request);


        response.put("bigip", getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input));
        response.put("lbconfig", getFasitResource(ResourceTypeDO.LoadBalancerConfig, "lbconfig", input));
        return Response.ok(response).build();

    }


    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, BigIPOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        DomainDO.EnvClass envClass = DomainDO.EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasitRestClient.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), input.getApplicationName(), type, alias);
        return resources.size() == 1 ? resources.iterator().next() : null;
    }


}
