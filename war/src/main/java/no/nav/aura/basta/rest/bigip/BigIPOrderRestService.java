package no.nav.aura.basta.rest.bigip;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static no.nav.aura.basta.util.StringHelper.isEmpty;

import java.util.*;
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
import no.nav.aura.basta.backend.bigip.RestClient;
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

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component(value = "test")
@Path("/v1/bigip")
public class BigIPOrderRestService {

    private static final Logger log = LoggerFactory.getLogger(BigIPOrderRestService.class);
    private OrderRepository orderRepository;
    private FasitUpdateService fasitUpdateService;
    private FasitRestClient fasitRestClient;
    private ActiveBigIPInstanceFinder activeInstanceFinder;
    private DnsService dnsService;
    private static final String PARTITION = "AutoProv";

    @Inject
    public BigIPOrderRestService(OrderRepository orderRepository, FasitUpdateService fasitUpdateService, FasitRestClient fasitRestClient, ActiveBigIPInstanceFinder activeBigIPInstanceFinder, DnsService dnsService) {
        this.orderRepository = orderRepository;
        this.fasitUpdateService = fasitUpdateService;
        this.fasitRestClient = fasitRestClient;
        this.activeInstanceFinder = activeBigIPInstanceFinder;
        this.dnsService = dnsService;
    }

    public static void main(String[] args) {
        // RestClient restClient = new RestClient();
        // Optional<Map> mapOptional = restClient.get("https://fasit.adeo.no/conf/applications/fasit", Map.class);
        // if (mapOptional.isPresent()) {
        // System.out.println(mapOptional.get());
        // } else {
        // System.out.println("absnt :(");
        // }
        System.out.println(
                getConflictingRules("policy_pp_itjenester-q0.oera.no_https_CONVERTED", "xmlstilling,mininnboks,jobblogg,banan,balle", new BigIPClient("localhost:6969", "srvbigipautoprov", "vldH_ZBEWKcJoC"), ""));
    }

    @POST
    @Consumes("application/json")
    public Response createBigIpConfig(Map<String, String> request) {
        log.debug("Got request with payload {}", request);
        validateSchema(request);
        BigIPOrderInput input = new BigIPOrderInput(request);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        verifyFasitEntities(input);

        BigIPClient bigIPClient = setupBigIPClient(input);

        verifyBigIPState(input, bigIPClient);

        // 1. ensurePolicyExists
        // 2. ensurePoolExists
        // 3. createRule
        // 4. done, create fasit resource

        // Set<String> policies =
        // getPolicies(bigIPClient.getVirtualServer(input.getVirtualServer()).or(Collections.emptyMap()));
        // if (policies.isEmpty()){
        //// create policy and map it to vs
        // }

        // Map pool = bigIPClient.getPool(createPoolName(input.getEnvironmentName(), input.getApplicationName()));
        // bigIPClient.createPool(createPoolName(input.getEnvironmentName(), input.getEnvironmentName()));

        // String policyName = getPolicyName(virtualServer.get());

        return Response.ok("WOOOOOT").build();
    }

    private void verifyBigIPState(BigIPOrderInput input, BigIPClient bigIPClient) {
        Map virtualServerResponse = bigIPClient.getVirtualServer(input.getVirtualServer()).orNull();
        if (virtualServerResponse == null) {
            throw new NotFoundException("No virtual server found on BIG-IP with name " + input.getVirtualServer());
        }

        Set<String> policies = getPolicies(virtualServerResponse);
        boolean multiplePoliciesOnVS = policies.size() > 1;
        if (multiplePoliciesOnVS) {
            throw new BadRequestException("Multiple policies mapped to virtual server, this is not supported.");
        }

        String policyName = policies.iterator().next();
        Map<String, String> conflictingRules = getConflictingRules(policyName, input.getContextRoots(), bigIPClient, createRuleName(input.getEnvironmentName(), input.getApplicationName()));
        if (policies.size() == 1 && !conflictingRules.isEmpty()) {
            throw new BadRequestException("Policy " + policyName + " has rules that conflict with the provided context roots");
        }
    }

    private static void verifyFasitEntities(BigIPOrderInput input) {
        String fasitRestUrl = System.getProperty("fasit.rest.api.url");
        boolean applicationDefinedInFasit = new RestClient().get(fasitRestUrl + "/applications/" + input.getApplicationName(), Map.class).isPresent();
        if (!applicationDefinedInFasit) {
            throw new NotFoundException("Unable to find any applications in Fasit with name " + input.getApplicationName());
        }

        boolean environmentDefinedInFasit = new RestClient().get(fasitRestUrl + "/environments/" + input.getEnvironmentName(), Map.class).isPresent();
        if (!environmentDefinedInFasit) {
            throw new NotFoundException("Unable to find any environments in Fasit with name " + input.getEnvironmentName());
        }

        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        boolean loadbalancerResourceDefinedInFasit = new RestClient()
                .get(fasitRestUrl + "/resources/bestmatch?type=LoadBalancer&alias=bigip&envName=" + input.getEnvironmentName() + "&app=" + input.getApplicationName() + "&domain=" + domain, Map.class).isPresent();
        if (!loadbalancerResourceDefinedInFasit) {
            throw new NotFoundException("Unable to find any BIG-IP instances for the provided scope");
        }

    }

    private String getPolicyName(Map virtualServer) {
        Map policiesReference = (Map) virtualServer.get("policiesReference");
        List<Map<String, String>> items = (List<Map<String, String>>) policiesReference.get("items");
        return items.get(0).get("name");
    }

    public static void validateSchema(Map<String, String> request) {
        ValidationHelper.validateRequest("/validation/createBigIPConfigSchema.json", request);
    }

    private BigIPClient setupBigIPClient(BigIPOrderInput input) {
        ResourceElement loadBalancer = getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input);

        String username = loadBalancer.getPropertyString("username");
        String password = fasitRestClient.getSecret(loadBalancer.getPropertyUri("password"));

        String activeInstance = activeInstanceFinder.getActiveBigIPInstance(loadBalancer, username, password);
        if (activeInstance == null) {
            throw new RuntimeException("Unable to find any active BIG-IP instance");
        }
        return new BigIPClient(activeInstance, username, password);
    }

    @GET
    @Path("/virtualservers/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVirtualServers(@Context UriInfo uriInfo) {
        BigIPOrderInput input = parse(uriInfo);
        ResourceElement bigipResource = getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input);
        if (bigipResource != null) {
            BigIPClient bigIPClient = setupBigIPClient(input);
            List<Map<String, Object>> virtualServers = bigIPClient.getVirtualServers(PARTITION);
            List<String> names = virtualServers.stream().map(map -> (String) map.get("name")).collect(Collectors.toList());
            return Response.ok(names).build();
        } else {
            return Response.status(NOT_FOUND).entity("BigIP resource not found").build();
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
            BigIPClient bigIPClient = setupBigIPClient(input);
            Map pool = bigIPClient.getPool(createPoolName(input.getEnvironmentName(), input.getApplicationName()));
            response.put("bigIpPoolExists", pool != null ? pool.get("name") : null);

            String virtualServer = input.getVirtualServer();
            String contextRoots = input.getContextRoots();

            if (!isEmpty(virtualServer) && !isEmpty(contextRoots)) {
                Map virtualServerMap = bigIPClient.getVirtualServer(virtualServer).orNull();
                response.put("vsExists", virtualServerMap != null);
                if (virtualServerMap == null) {
                    return Response.ok(response).build();
                }

                Set<String> policies = getPolicies(virtualServerMap);
                boolean multiplePoliciesOnVS = policies.size() > 1;
                response.put("multiplePoliciesOnVS", multiplePoliciesOnVS);

                if (!multiplePoliciesOnVS && !policies.isEmpty()) {
                    response.put("conflictingContextRoots", getConflictingRules(policies.iterator().next(), contextRoots, bigIPClient, createRuleName(input.getApplicationName(), input.getEnvironmentName())));
                }
            }
        }

        return Response.ok(response).build();
    }

    private Set<String> getPolicies(Map virtualServer) {
        Set<String> policyNames = Sets.newHashSet();
        Map policiesReference = (Map) virtualServer.get("policiesReference");
        List<Map<String, String>> policies = (List<Map<String, String>>) policiesReference.get("items");

        for (Map<String, String> policy : policies) {
            policyNames.add(policy.get("name"));
        }

        return policyNames;
    }

    private static Map<String, String> getConflictingRules(String policyName, String contextRoots, BigIPClient bigIPClient, String ruleName) {
        Map<String, String> conflictingRules = Maps.newHashMap();
        Map policy = bigIPClient.getPolicy(policyName);
        Map<String, String> ruleValues = Maps.newHashMap();
        List<Map> rules = (List<Map>) policy.get("items");
        if (rules != null) {
            for (Map rule : rules) {
                Map conditionsReference = (Map) rule.get("conditionsReference");
                List<Map> conditions = (List<Map>) conditionsReference.get("items");
                for (Map condition : conditions) {
                    List<String> values = (List<String>) condition.get("values");
                    for (String value : values) {
                        String valueWithoutSlashes = value.replace("/", "");
                        String existingRuleName = (String) rule.get("name");
                        ruleValues.put(existingRuleName, valueWithoutSlashes);
                    }
                }
            }
        }

        for (String contextRoot : contextRoots.split(",")) {
            for (Map.Entry<String, String> ruleValueEntry : ruleValues.entrySet()) {
                String existingRuleName = ruleValueEntry.getKey();
                String ruleValue = ruleValueEntry.getValue();
                if (ruleValue.equalsIgnoreCase(contextRoot) && !existingRuleName.equalsIgnoreCase(ruleName)) {
                    conflictingRules.put(existingRuleName, ruleValue);
                }
            }
        }

        return conflictingRules;
    }

    private static String createPoolName(String environmentName, String application) {
        return "pool_autodeploy-test-config-bare_u99"; // "pool_"+ application + "_" + environmentName + "https_auto";
    }

    private static String createRuleName(String applicationName, String environmentName) {
        return "prule_" + applicationName + "_" + environmentName + "_ctxroot";
    }

    private static String createVirtualServerName(String environmentName) {
        return "vs_utv_itjenester-u99.oera.no_https"; // "vs_skya_"+environmentName;
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
