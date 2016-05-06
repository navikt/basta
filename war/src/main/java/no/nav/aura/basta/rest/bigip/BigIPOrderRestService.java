package no.nav.aura.basta.rest.bigip;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
import no.nav.aura.basta.backend.bigip.RestClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.domain.result.bigip.BigIPOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.util.ValidationHelper;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static no.nav.aura.basta.backend.BigIPClient.createEqualsCondition;
import static no.nav.aura.basta.backend.BigIPClient.createStartsWithCondition;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.SUCCESS;
import static no.nav.aura.basta.domain.result.bigip.BigIPOrderResult.FASIT_ID;
import static no.nav.aura.basta.rest.dataobjects.StatusLogLevel.info;
import static no.nav.aura.basta.util.StringHelper.isEmpty;

@Component(value = "test")
@Path("/v1/bigip")
public class BigIPOrderRestService {


    private static final Logger log = LoggerFactory.getLogger(BigIPOrderRestService.class);


    private BigIPClientSetup bigIPClientSetup;
    private OrderRepository orderRepository;
    private FasitUpdateService fasitUpdateService;
    private FasitRestClient fasitRestClient;
    private RestClient restClient;
    private static final String PARTITION = "AutoProv";


    @Inject
    public BigIPOrderRestService(OrderRepository orderRepository, FasitUpdateService fasitUpdateService, FasitRestClient fasitRestClient, RestClient restClient, BigIPClientSetup bigIPClientSetup) {
        this.orderRepository = orderRepository;
        this.fasitUpdateService = fasitUpdateService;
        this.fasitRestClient = fasitRestClient;
        this.bigIPClientSetup = bigIPClientSetup;
        this.restClient = restClient;

    }

    @POST
    @Consumes("application/json")
    public Response createBigIpConfig(Map<String, String> request) {
        log.debug("Got request with payload {}", request);
        validateSchema(request);
        BigIPOrderInput input = new BigIPOrderInput(request);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        HashSet<String> contextRoots = sanitizeContextRoots(input.getContextRoots());
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
        String poolName = createPoolName(environmentName, applicationName, input.getEnvironmentClass().name());
        ensurePoolExists(poolName, bigIPClient);
        order.log("Ensured pool with name " + poolName + " exists", info);

        recreateRulesOnPolicy(policyName, poolName, contextRoots, input, order, bigIPClient);

        bigIPClient.mapPolicyToVS(policyName, input.getVirtualServer());
        orderRepository.save(order.addStatuslogInfo("Ensured policy " + policyName + " is mapped to virtual server " + input.getVirtualServer()));

        String vsUrl = input.getDns() != null ? input.getDns() : bigIPClient.getVirtualServerIP(input.getVirtualServer());
        ResourceElement lbConfig = createLBConfigResource(input, poolName, vsUrl);

        order = orderRepository.save(order);

        Optional<Long> resourceId = getPotentiallyExistingLBConfigId(input);

        Optional<ResourceElement> maybeFasitResource = fasitUpdateService.createOrUpdateResource(resourceId.orElse(null), lbConfig, order);

        if (!maybeFasitResource.isPresent()) {
            order.setStatus(FAILURE);
        } else {
            ResourceElement fasitResource = maybeFasitResource.get();
            BigIPOrderResult result = order.getResultAs(BigIPOrderResult.class);
            result.put(FASIT_ID, String.valueOf(fasitResource.getId()));
            order.setStatus(SUCCESS);
        }

        order = orderRepository.save(order);
        return Response.ok(createResponseWithId(order.getId())).build();
    }

    private void recreateRulesOnPolicy(String policyName, String poolName, Set<String> contextRoots, BigIPOrderInput input, Order order, BigIPClient bigIPClient) {
        String equalsRuleName = createEqualsRuleName(input.getApplicationName(), input.getEnvironmentName(), input.getEnvironmentClass().name());
        String startsWithRuleName = createStartsWithRuleName(input.getApplicationName(), input.getEnvironmentName(), input.getEnvironmentClass().name());
        Set<String> ruleNames = Sets.newHashSet(equalsRuleName, startsWithRuleName);

        boolean noOtherRules = !policyHasOtherRules(policyName, ruleNames, bigIPClient);

        if (noOtherRules) {
            order.log("No other rules exist on policy, creating a placeholder rule (if not the clean up will fail)", info);
            bigIPClient.createDummyRuleOnPolicy(policyName, "dummy_rule");
        }

        for (String ruleName : ruleNames) {
            bigIPClient.deleteRuleFromPolicy(ruleName, policyName);
            orderRepository.save(order.addStatuslogInfo("Deleted rule " + ruleName + " from policy " + policyName));
        }

        bigIPClient.createRuleOnPolicy(equalsRuleName, policyName, poolName, createEqualsCondition(contextRoots));
        orderRepository.save(order.addStatuslogInfo("Created rule " + equalsRuleName + " from policy " + policyName));
        bigIPClient.createRuleOnPolicy(startsWithRuleName, policyName, poolName, createStartsWithCondition(contextRoots));
        orderRepository.save(order.addStatuslogInfo("Created rule " + startsWithRuleName + " from policy " + policyName));

        if (noOtherRules) {
            bigIPClient.deleteRuleFromPolicy("dummy_rule", policyName);
            order.log("Deleted placeholder rule", info);
            orderRepository.save(order.addStatuslogInfo("Deleted placeholder rule"));
        }
    }

    private Optional<Long> getPotentiallyExistingLBConfigId(BigIPOrderInput input) {
        String fasitRestUrl = System.getProperty("fasit.rest.api.url");
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());

        List<Map> resources = restClient
                .get(fasitRestUrl + "/resources?bestmatch=true&type=LoadBalancerConfig&alias=" + getLBConfigAlias(input.getApplicationName()) + "&envName=" + input.getEnvironmentName() + "&app="
                                + input.getApplicationName() + "&domain="
                                + domain.getFqn(),
                        List.class)
                .get();

        resources = resources.stream().filter(resource -> resource.get("application") != null).collect(toList());

        if (resources.isEmpty()) {
            return Optional.empty();
        }

        long fasitId = (long) (int) resources.get(0).get("id");

        log.debug("Found existing LBConfig resource in Fasit with id {}", fasitId);

        return Optional.of(fasitId);
    }

    private String getLBConfigAlias(String applicationName) {
        return "loadbalancer:" + applicationName;
    }

    private ResourceElement createLBConfigResource(BigIPOrderInput input, String poolName, String url) {
        ResourceElement lbConfig = new ResourceElement(ResourceTypeDO.LoadBalancerConfig, getLBConfigAlias(input.getApplicationName()));
        lbConfig.addProperty(new PropertyElement("url", url));
        lbConfig.addProperty(new PropertyElement("poolName", poolName));
        lbConfig.setEnvironmentClass(input.getEnvironmentClass().name());
        lbConfig.setEnvironmentName(input.getEnvironmentName());
        lbConfig.setApplication(input.getApplicationName());
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        lbConfig.setDomain(DomainDO.fromFqdn(domain.getFqn()));
        return lbConfig;
    }

    private String createResponseWithId(Long id) {
        return "{\"id\": " + id + "}";
    }

    private boolean policyHasOtherRules(String policyName, Set<String> ruleNames, BigIPClient bigIPClient) {
        Set<String> existingRules = getExistingRulesOnPolicy(policyName, bigIPClient);

        return !(ruleNames.containsAll(existingRules) && existingRules.size() == ruleNames.size());
    }

    private Set<String> getExistingRulesOnPolicy(String policyName, BigIPClient bigIPClient) {
        Map rules = bigIPClient.getRules(policyName);
        List<Map> items = (List<Map>) rules.get("items");
        return items.stream().map(item -> (String) item.get("name")).collect(toSet());
    }

    private static HashSet<String> sanitizeContextRoots(String contextRootString) {
        if (contextRootString == null) {
            return Sets.newHashSet();
        }

        HashSet<String> contextRoots = Sets.newHashSet();

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
            List<Map<String, String>> conflictingRules = getConflictingRules(policyName, input.getContextRoots(), bigIPClient,
                    createRuleNames(input.getApplicationName(), input.getEnvironmentName(), input.getEnvironmentClass().name()));

            if (!conflictingRules.isEmpty()) {
                throw new BadRequestException("Policy " + policyName + " has rules that conflict with the provided context roots");
            }
        }
    }

    private void verifyFasitEntities(BigIPOrderInput input) {
        String fasitRestUrl = System.getProperty("fasit.rest.api.url");
        boolean applicationDefinedInFasit = restClient.get(fasitRestUrl + "/applications/" + input.getApplicationName(), Map.class).isPresent();
        if (!applicationDefinedInFasit) {
            throw new NotFoundException("Unable to find any applications in Fasit with name " + input.getApplicationName());
        }

        boolean environmentDefinedInFasit = restClient.get(fasitRestUrl + "/environments/" + input.getEnvironmentName(), Map.class).isPresent();
        if (!environmentDefinedInFasit) {
            throw new NotFoundException("Unable to find any environments in Fasit with name " + input.getEnvironmentName());
        }

        boolean loadbalancerResourceDefinedInFasit = fasitResourceExists(input, "LoadBalancer", "bigip");
        if (!loadbalancerResourceDefinedInFasit) {
            throw new NotFoundException("Unable to find any BIG-IP instances/resources for the provided scope");
        }

        if (!possibleToUpdateFasit(input)) {
            throw new BadRequestException("Multiple resources lbConfig resources exists in scope for this application, unable to choose which one to update");
        }
    }

    public static void validateSchema(Map<String, String> request) {
        ValidationHelper.validateRequest("/validation/createBigIPConfigSchema.json", request);
    }

    private BigIPClient setupBigIPClient(BigIPOrderInput input) {
        return bigIPClientSetup.setupBigIPClient(input);
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
            List<String> names = virtualServers.stream().map(map -> (String) map.get("name")).collect(toList());
            return Response.ok(names).build();
        } else {
            return Response.status(NOT_FOUND).entity(new String[]{"BigIP resource not found"}).build();
        }
    }

    @GET
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validate(@Context UriInfo uriInfo) {
        HashMap<String, Object> response = new HashMap<>();
        BigIPOrderInput input = parse(uriInfo);

        ResourceElement bigipResource = getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input);
        response.put("bigIpResourceExists", bigipResource != null);
        response.put("possibleToUpdateFasit", possibleToUpdateFasit(input));

        if (bigipResource != null) {
            BigIPClient bigIPClient = setupBigIPClient(input);

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
                    response.put("conflictingContextRoots",
                            getConflictingRules(policy, contextRoots, bigIPClient, createRuleNames(input.getApplicationName(), input.getEnvironmentName(), input.getEnvironmentClass().name())));
                }
            }
        }

        return Response.ok(response).build();
    }

    // TODO: use new fasit resource api instead when ready
    private boolean possibleToUpdateFasit(BigIPOrderInput input) {
        String fasitRestUrl = System.getProperty("fasit.rest.api.url");
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());

        try {
            restClient.get(fasitRestUrl + "/resources?bestmatch=true&type=LoadBalancerConfig&alias=" + getLBConfigAlias(input.getApplicationName()) + "&envName=" + input.getEnvironmentName() + "&app="
                            + input.getApplicationName() + "&domain="
                            + domain.getFqn(),
                    List.class);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean fasitResourceExists(BigIPOrderInput input, final String resourceType, final String alias) {
        String fasitRestUrl = System.getProperty("fasit.rest.api.url");
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        return !restClient
                .get(fasitRestUrl + "/resources?type=" + resourceType + "&alias=" + alias + "&envName=" + input.getEnvironmentName() + "&app=" + input.getApplicationName() + "&domain=" + domain.getFqn(),
                        List.class)
                .get().isEmpty();
    }

    private String getForwardingPolicy(Map virtualServer, BigIPClient bigIPClient) {
        Set<String> policies = bigIPClient.getPoliciesFrom(virtualServer);
        for (String policy : policies) {
            Map policyPayload = bigIPClient.getPolicy(policy);
            List<String> controls = (List<String>) policyPayload.get("controls");
            if (controls != null && controls.contains("forwarding")) {
                return policy;
            }
        }
        return null;
    }

    private static List<Map<String, String>> getConflictingRules(String policyName, String contextRoots, BigIPClient bigIPClient, Set<String> ruleNames) {
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
                if (ruleValue.equalsIgnoreCase(contextRoot) && !ruleNames.contains(existingRuleName)) {
                    conflictingRules.add(ImmutableMap.of(existingRuleName, ruleValue));
                }
            }
        }

        return conflictingRules;
    }

    private static String createPoolName(String environmentName, String application, String environmentClass) {
        String mappedEnvClass = mapToBigIPNamingStandard(environmentClass);
        return "pool_" + mappedEnvClass + "_" + application + "_" + environmentName + "_auto";
    }

    private static HashSet<String> createRuleNames(String applicationName, String environmentName, String environmentClass) {
        return Sets.newHashSet(createStartsWithRuleName(applicationName, environmentName, environmentClass),
                createEqualsRuleName(applicationName, environmentName, environmentClass));
    }

    private static String createEqualsRuleName(String applicationName, String environmentName, String environmentClass) {
        String mappedEnvClass = mapToBigIPNamingStandard(environmentClass);
        return "prule_" + mappedEnvClass + "_" + applicationName + "_" + environmentName + "_https_eq_auto";
    }

    private static String createStartsWithRuleName(String applicationName, String environmentName, String environmentClass) {
        String mappedEnvClass = mapToBigIPNamingStandard(environmentClass);
        return "prule_" + mappedEnvClass + "_" + applicationName + "_" + environmentName + "_https_sw_auto";
    }

    private BigIPOrderInput parse(@Context UriInfo uriInfo) {
        HashMap<String, String> request = ValidationHelper.queryParamsAsMap(uriInfo.getQueryParameters());
        ValidationHelper.validateRequiredParams(request, "environmentClass", "environmentName", "zone", "application");
        return new BigIPOrderInput(request);
    }

    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, BigIPOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        return fasitRestClient.getResource(input.getEnvironmentName(), alias, type, DomainDO.fromFqdn(domain.getFqn()), input.getApplicationName());
    }

    private static String mapToBigIPNamingStandard(String environmentClass) {
        switch (environmentClass) {
            case "u":
                return "utv";
            case "t":
                return "tst";
            case "q":
                return "pp";
            case "p":
                return "pr";
            default:
                throw new RuntimeException("Unknown environmentclass: " + environmentClass);
        }
    }
}
