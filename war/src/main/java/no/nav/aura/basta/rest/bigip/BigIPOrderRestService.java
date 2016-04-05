package no.nav.aura.basta.rest.bigip;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.SUCCESS;
import static no.nav.aura.basta.rest.dataobjects.StatusLogLevel.info;
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
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.Tuple;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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

    @POST
    @Consumes("application/json")
    public Response createBigIpConfig(Map<String, String> request) {
        log.debug("Got request with payload {}", request);
        validateSchema(request);
        BigIPOrderInput input = new BigIPOrderInput(request);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Set<String> contextRoots = sanitizeContextRoots(input.getContextRoots());
        if (contextRoots.isEmpty()) {
            throw new BadRequestException("Provided context roots was invalid");
        }

        verifyFasitEntities(input);

        BigIPClient bigIPClient = setupBigIPClient(input);

        verifyBigIPState(input, bigIPClient);

        // done with verification, all is good from here
        Order order = new Order(OrderType.BIGIP, OrderOperation.CREATE, input);

        String environmentName = input.getEnvironmentName();
        String policyName = ensurePolicyExists(input.getVirtualServer(), environmentName, bigIPClient);
        order.log("Ensured policy with name " + policyName + " exists", info);

        String applicationName = input.getApplicationName();
        String poolName = createPoolName(environmentName, applicationName);
        ensurePoolExists(poolName, bigIPClient);
        order.log("Ensured pool with name " + poolName + " exists", info);

        String ruleName = createRuleName(applicationName, environmentName);

        // if policy only has one rule (and it's the one we're (re)creating, we cannot remove it when it's connected to a VS
        // (edge case)
        boolean noOtherRules = !policyHasOtherRules(policyName, ruleName, bigIPClient);

        if (noOtherRules) {
            order.log("No other rules exist on policy, creating a placeholder rule", info);
            bigIPClient.createDummyRuleOnPolicy(policyName, "dummy_rule");
        }

        bigIPClient.deleteRuleFromPolicy(ruleName, policyName);
        order.log("Deleted rule " + ruleName + " from policy " + policyName, info);
        bigIPClient.createRuleOnPolicy(ruleName, policyName, contextRoots, poolName);
        order.log("Created rule " + ruleName + " from policy " + policyName, info);
        bigIPClient.mapPolicyToVS(policyName, input.getVirtualServer());
        order.log("Ensured policy " + policyName + " is mapped to virtual server " + input.getVirtualServer(), info);

        if (noOtherRules) {
            bigIPClient.deleteRuleFromPolicy("dummy_rule", policyName);
            order.log("Deleted placeholder rule", info);
        }

        createFasitResource();
        // create fasit resource

        order.setStatus(SUCCESS);
        Order savedOrder = orderRepository.save(order);
        return Response.ok(createResponseWithId(savedOrder.getId())).build();
    }

    private String createResponseWithId(Long id) {
        return "{\"id\": " + id + "}";
    }

    private boolean policyHasOtherRules(String policyName, String ruleName, BigIPClient bigIPClient) {
        Map rules = bigIPClient.getRules(policyName);
        List<Map> items = (List<Map>) rules.get("items");

        boolean singleRule = items.size() == 1;
        if (singleRule) {
            String existingRule = (String) items.get(0).get("name");
            return !existingRule.equalsIgnoreCase(ruleName);
        } else {
            return true;
        }
    }

    private static Set<String> sanitizeContextRoots(String contextRootString) {
        if (contextRootString == null) {
            return Collections.emptySet();
        }

        Set<String> contextRoots = Sets.newHashSet();

        for (String contextRoot : contextRootString.trim().split(",")) {
            contextRoot = contextRoot.trim();
            String ctxRootWithoutSurroundingSlashes = removeSurroundingSlashes(contextRoot);

            if (!ctxRootWithoutSurroundingSlashes.isEmpty()) {
                contextRoots.add(ctxRootWithoutSurroundingSlashes);
            }
        }

        return contextRoots;
    }

    private static String removeSurroundingSlashes(String str) {
        if (str.startsWith("/")) {
            str = str.substring(1);
        }

        if (str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }

    private void createFasitResource() {

    }

    private void ensurePoolExists(String poolName, BigIPClient bigIPClient) {
        boolean poolMissing = bigIPClient.getPool(poolName).isEmpty();
        if (poolMissing) {
            bigIPClient.createPool(poolName);
        }
    }

    private String ensurePolicyExists(String virtualServerName, String environmentName, BigIPClient bigIPClient) {
        Map virtualServerResponse = bigIPClient.getVirtualServer(virtualServerName).orNull();
        if (virtualServerResponse == null) {
            throw new RuntimeException("No virtual server found, exiting. (This should not happen)");
        }

        String policyName = getForwardingPolicy(virtualServerResponse, bigIPClient);

        if (policyName == null) {
            policyName = createPolicyName(environmentName);
            bigIPClient.createPolicy(policyName);
        }

        return policyName;
    }

    private static String createPolicyName(String environmentName) {
        return "policy_" + environmentName + "_skya";
    }

    private void verifyBigIPState(BigIPOrderInput input, BigIPClient bigIPClient) {
        Map virtualServerResponse = bigIPClient.getVirtualServer(input.getVirtualServer()).orNull();
        if (virtualServerResponse == null) {
            throw new NotFoundException("No virtual server found on BIG-IP with name " + input.getVirtualServer());
        }

        String policyName = getForwardingPolicy(virtualServerResponse, bigIPClient);

        if (policyName != null) {
            List<Map<String, String>> conflictingRules = getConflictingRules(policyName, input.getContextRoots(), bigIPClient, createRuleName(input.getApplicationName(), input.getEnvironmentName()));

            if (!conflictingRules.isEmpty()) {
                throw new BadRequestException("Policy " + policyName + " has rules that conflict with the provided context roots");
            }
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
                .get(fasitRestUrl + "/resources/bestmatch?type=LoadBalancer&alias=bigip&envName=" + input.getEnvironmentName() + "&app=" + input.getApplicationName() + "&domain=" + domain.getFqn(), Map.class)
                .isPresent();
        if (!loadbalancerResourceDefinedInFasit) {
            throw new NotFoundException("Unable to find any BIG-IP instances for the provided scope");
        }
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
        ResourceElement lbconfigResource = getFasitResource(ResourceTypeDO.LoadBalancerConfig, "bigip", input);

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

                String policy = getForwardingPolicy(virtualServerMap, bigIPClient);

                if (policy != null) {
                    response.put("conflictingContextRoots", getConflictingRules(policy, contextRoots, bigIPClient, createRuleName(input.getApplicationName(), input.getEnvironmentName())));
                }
            }
        }

        return Response.ok(response).build();
    }

    private String getForwardingPolicy(Map virtualServer, BigIPClient bigIPClient) {
        Set<String> policies = BigIPClient.getPoliciesFrom(virtualServer);
        for (String policy : policies) {
            Map policyPayload = bigIPClient.getPolicy(policy);
            List<String> controls = (List<String>) policyPayload.get("controls");
            if (controls != null && controls.contains("forwarding")) {
                return policy;
            }
        }
        return null;
    }

    private static List<Map<String, String>> getConflictingRules(String policyName, String contextRoots, BigIPClient bigIPClient, String ruleName) {
        List<Map<String, String>> conflictingRules = Lists.newArrayList();
        Map policy = bigIPClient.getRules(policyName);

        Set<Tuple<String, String>> ruleValues = Sets.newHashSet();
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

                        ruleValues.add(Tuple.of(existingRuleName, valueWithoutSlashes));
                    }
                }
            }
        }

        for (String contextRoot : contextRoots.split(",")) {
            for (Tuple<String, String> ruleValueEntry : ruleValues) {
                String existingRuleName = ruleValueEntry.fst;
                String ruleValue = ruleValueEntry.snd;
                if (ruleValue.equalsIgnoreCase(contextRoot) && !existingRuleName.equalsIgnoreCase(ruleName)) {
                    conflictingRules.add(ImmutableMap.of(existingRuleName, ruleValue));
                }
            }
        }

        return conflictingRules;
    }

    private static String createPoolName(String environmentName, String application) {
        return "pool_" + application + "_" + environmentName + "_auto";
    }

    private static String createRuleName(String applicationName, String environmentName) {
        return "prule_" + applicationName + "_" + environmentName + "_ctxroot";
    }

    private BigIPOrderInput parse(@Context UriInfo uriInfo) {
        HashMap<String, String> request = ValidationHelper.queryParamsAsMap(uriInfo.getQueryParameters());
        ValidationHelper.validateRequiredParams(request, "environmentClass", "environmentName", "zone", "application");
        return new BigIPOrderInput(request);
    }

    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, BigIPOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        DomainDO.EnvClass envClass = DomainDO.EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasitRestClient.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), input.getApplicationName(), type, alias);
        return resources.size() == 1 ? resources.iterator().next() : null;
    }

}
