package no.nav.aura.basta.rest.bigip;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
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
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static no.nav.aura.basta.backend.BigIPClient.*;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.SUCCESS;
import static no.nav.aura.basta.domain.result.bigip.BigIPOrderResult.FASIT_ID;
import static no.nav.aura.basta.rest.dataobjects.StatusLogLevel.info;
import static no.nav.aura.basta.util.StringHelper.isEmpty;

@Component
@Path("/v1/bigip")
public class BigIPOrderRestService {

    public static final String DUMMY_RULE_NAME = "dummy_rule";
    private static final Logger log = LoggerFactory.getLogger(BigIPOrderRestService.class);
    private static final String PARTITION = "AutoProv";
    private BigIPClientSetup bigIPClientSetup;
    private OrderRepository orderRepository;
    private FasitUpdateService fasitUpdateService;
    private FasitRestClient fasitRestClient;
    private RestClient restClient;

    @Inject
    public BigIPOrderRestService(OrderRepository orderRepository, FasitUpdateService fasitUpdateService, FasitRestClient fasitRestClient, RestClient restClient, BigIPClientSetup bigIPClientSetup) {
        this.orderRepository = orderRepository;
        this.fasitUpdateService = fasitUpdateService;
        this.fasitRestClient = fasitRestClient;
        this.bigIPClientSetup = bigIPClientSetup;
        this.restClient = restClient;
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

    private static String getSystemPropertyOrThrow(String key, String message) {
        String property = System.getProperty(key);

        if (property == null) {
            throw new IllegalStateException(message);
        }
        return property;
    }

    // Checks if policy has any rules that contains context-roots in conflict with incoming
    private static List<Map<String, String>> getConflictingRules(String policyName, String contextRoots, BigIPClient bigIPClient, Set<String> ruleNames) {
        List<Map<String, String>> conflictingRules = Lists.newArrayList();
        Map policy = bigIPClient.getRules(policyName);

        log.info("Reading policy: " + policyName);
        Set<Tuple<String, String>> ruleValues = Sets.newHashSet();
        List<Map> rules = (List<Map>) policy.get("items");
        if (rules != null) {
            for (Map rule : rules) {
                log.info("Reading rule: " + rule.get("name"));
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

    @POST
    @Consumes("application/json")
    public Response createBigIpConfig(Map<String, String> request) {
        log.debug("Got request with payload {}", request);
        ValidationHelper.validateRequest("/validation/createBigIPConfigSchema.json", request);
        BigIPOrderInput input = new BigIPOrderInput(request);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        if (!input.getUseHostnameMatching() && sanitizeContextRoots(input.getContextRoots()).isEmpty()) {
            throw new BadRequestException("Provided context roots was invalid");
        } else if (input.getUseHostnameMatching() && isEmpty(input.getHostname())) {
            throw new BadRequestException("No hostname was specified");
        } else if (input.getUseHostnameMatching() && isCommonVS(input.getHostname())) {
            throw new BadRequestException("You cannot create a hostname matching rule for a common VS, please read instructions and try again");
        }

        verifyFasitEntities(input);

        BigIPClient bigIPClient = bigIPClientSetup.setupBigIPClient(input);

        verifyBigIPState(input, bigIPClient);

        // done with verification, all is good from here
        Order order = new Order(OrderType.BIGIP, OrderOperation.CREATE, input);

        String environmentName = input.getEnvironmentName();
        String environmentClass = input.getEnvironmentClass().name();
        String policyName = ensurePolicyExists(input.getVirtualServer(), BigIPNamer.createPolicyName(environmentName, environmentClass), bigIPClient);
        order.log("Ensured policy with name " + policyName + " exists", info);

        String applicationName = input.getApplicationName();
        String poolName = BigIPNamer.createPoolName(environmentName, applicationName, environmentClass);
        ensurePoolExists(poolName, bigIPClient);
        order.log("Ensured pool with name " + poolName + " exists", info);

        recreateRulesOnPolicy(policyName, poolName, input, order, bigIPClient);

        bigIPClient.mapPolicyToVS(policyName, input.getVirtualServer());
        orderRepository.save(order.addStatuslogInfo("Ensured policy " + policyName + " is mapped to virtual server " + input.getVirtualServer()));

        String vsUrl = input.getHostname() != null ? input.getHostname() : bigIPClient.getVirtualServerIP(input.getVirtualServer());
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
        return Response.ok(order.getId()).build();
    }

    private boolean isCommonVS(String hostname) {
        return hostname.matches("app-.+\\.adeo\\.no")
                || hostname.matches("wasapp-.+\\.adeo\\.no")
                || hostname.matches("modapp-.+\\.adeo\\.no")
                || hostname.matches("tjenester-.+\\.nav\\.no")
                || hostname.matches("itjenester-.+\\.oera\\.no");
    }

    public static boolean usesPolicyDrafts(String version) {
        String[] versions = version.split("\\.");
        int major = Integer.parseInt(versions[0]);
        int minor = Integer.parseInt(versions[1]);
        return major >= 12 && minor >= 1;
    }

    private void recreateRulesOnPolicy(String policyName, String poolName, BigIPOrderInput input, Order order, BigIPClient bigIPClient) {
        boolean usesPolicyDrafts = usesPolicyDrafts(bigIPClient.getVersion());

        if (usesPolicyDrafts) {
            bigIPClient.createPolicyDraft(policyName);
        }

        if (input.getUseHostnameMatching()) {
            String hostnameRuleName = BigIPNamer.createHostnameRuleName(input.getApplicationName(), input.getEnvironmentName(), input.getEnvironmentClass().name());

            bigIPClient.deleteRuleFromPolicy(policyName, hostnameRuleName, usesPolicyDrafts);
            order.log("Deleted rule " + hostnameRuleName + " from policy " + policyName, info);

            bigIPClient.createRuleOnPolicy(hostnameRuleName, policyName, poolName, createHostnameCondition(input.getHostname()), usesPolicyDrafts);
            order.log("Created rule " + hostnameRuleName + " on " + policyName, info);
        } else {
            HashSet<String> contextRoots = sanitizeContextRoots(input.getContextRoots());
            String equalsRuleName = BigIPNamer.createEqualsRuleName(input.getApplicationName(), input.getEnvironmentName(), input.getEnvironmentClass().name());
            String startsWithRuleName = BigIPNamer.createStartsWithRuleName(input.getApplicationName(), input.getEnvironmentName(), input.getEnvironmentClass().name());

            bigIPClient.deleteRuleFromPolicy(policyName, equalsRuleName, usesPolicyDrafts);
            order.log("Deleted rule " + equalsRuleName + " from policy " + policyName, info);

            bigIPClient.createRuleOnPolicy(equalsRuleName, policyName, poolName, createEqualsCondition(contextRoots), usesPolicyDrafts);
            order.log("Created rule " + equalsRuleName + " on " + policyName, info);

            bigIPClient.deleteRuleFromPolicy(policyName, startsWithRuleName, usesPolicyDrafts);
            order.log("Deleted rule " + startsWithRuleName + " from policy " + policyName, info);

            bigIPClient.createRuleOnPolicy(startsWithRuleName, policyName, poolName, createStartsWithCondition(contextRoots), usesPolicyDrafts);
            order.log("Created rule " + startsWithRuleName + " on " + policyName, info);
        }

        if (usesPolicyDrafts) {
            bigIPClient.publishPolicyDraft(policyName);
        }

    }

    private Optional<Long> getPotentiallyExistingLBConfigId(BigIPOrderInput input) {
        String resourceApi = getSystemPropertyOrThrow("fasit_resources_v2_url", "No fasit resource api present");
        String url = resourceApi + "?type=LoadBalancerConfig&environment=" + input.getEnvironmentName() + "&application=" + input.getApplicationName();

        List<Map> resources = restClient.get(url, List.class).get();

        if (resources.isEmpty()) {
            return Optional.empty();
        }

        if (resources.size() > 1) {
            throw new RuntimeException("More than one loadbalancer config resource for application, don't know which one to pick");
        }

        Map resource = resources.get(0);
        if (resource.get("id") == null) {
            return Optional.empty();
        }

        long fasitId = (long) (int) resource.get("id");
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
        if (!isEmpty(input.getContextRoots())) {
            lbConfig.addProperty(new PropertyElement("contextRoots", input.getContextRoots()));
        }
        lbConfig.setEnvironmentClass(input.getEnvironmentClass().name());
        lbConfig.setEnvironmentName(input.getEnvironmentName());
        lbConfig.setApplication(input.getApplicationName());
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        lbConfig.setDomain(DomainDO.fromFqdn(domain.getFqn()));
        return lbConfig;
    }

    private void ensurePoolExists(String poolName, BigIPClient bigIPClient) {
        boolean poolMissing = bigIPClient.getPool(poolName).isEmpty();
        if (poolMissing) {
            bigIPClient.createPool(poolName);
        }
    }

    private String ensurePolicyExists(String virtualServerName, String policyName, BigIPClient bigIPClient) {
        Map virtualServerResponse = bigIPClient.getVirtualServer(virtualServerName).orElse(null);
        if (virtualServerResponse == null) {
            throw new RuntimeException("No virtual server found, exiting)");
        }

        String existingPolicyName = getForwardingPolicy(virtualServerResponse, bigIPClient);

        if (existingPolicyName == null) {
            bigIPClient.createPolicy(policyName);
            return policyName;
        } else {
            return existingPolicyName;
        }
    }

    private void verifyBigIPState(BigIPOrderInput input, BigIPClient bigIPClient) {
        Map virtualServerResponse = bigIPClient.getVirtualServer(input.getVirtualServer()).orElse(null);
        if (virtualServerResponse == null) {
            throw new NotFoundException("No virtual server found on BIG-IP with name " + input.getVirtualServer());
        }

        String policyName = getForwardingPolicy(virtualServerResponse, bigIPClient);

        if (policyName != null && !input.getUseHostnameMatching()) {
            List<Map<String, String>> conflictingRules = getConflictingRules(policyName, input.getContextRoots(), bigIPClient,
                    BigIPNamer.createRuleNames(input.getApplicationName(), input.getEnvironmentName(), input.getEnvironmentClass().name()));

            if (!conflictingRules.isEmpty()) {
                throw new BadRequestException("Policy " + policyName + " has rules that conflict with the provided context roots");
            }
        }
    }

    private void verifyFasitEntities(BigIPOrderInput input) {
        String applicationsApi = getSystemPropertyOrThrow("fasit_applications_v2_url", "No fasit " +
                "applications api " +
                "present");
        boolean applicationDefinedInFasit = restClient.get(applicationsApi + "/" + input.getApplicationName(), Map.class).isPresent();
        if (!applicationDefinedInFasit) {
            throw new NotFoundException("Unable to find any applications in Fasit with name " + input.getApplicationName());
        }

        String environmentsApi = getSystemPropertyOrThrow("fasit_environments_v2_url", "No fasit " +
                "environments api " +
                "present");
        boolean environmentDefinedInFasit = restClient.get(environmentsApi + "/" + input.getEnvironmentName(), Map.class).isPresent();
        if (!environmentDefinedInFasit) {
            throw new NotFoundException("Unable to find any environments in Fasit with name " + input.getEnvironmentName());
        }

        boolean loadbalancerResourceDefinedInFasit = bigipResourceExists(input);
        if (!loadbalancerResourceDefinedInFasit) {
            throw new NotFoundException("Unable to find any BIG-IP instances/resources for the provided scope");
        }

        if (!possibleToUpdateFasit(input)) {
            throw new BadRequestException("Multiple resources lbConfig resources exists in scope for this application, unable to choose which one to update");
        }
    }

    @GET
    @Path("/virtualservers/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVirtualServers(@Context UriInfo uriInfo) {
        BigIPOrderInput input = parse(uriInfo);
        ResourceElement bigipResource = getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input);
        if (bigipResource != null) {
            BigIPClient bigIPClient = bigIPClientSetup.setupBigIPClient(input);
            List<Map<String, Object>> virtualServers = bigIPClient.getVirtualServers(PARTITION);
            List<String> names = virtualServers.stream().map(map -> (String) map.get("name")).collect(toList());
            return Response.ok(names).build();
        } else {
            return Response.status(NOT_FOUND).entity(new String[] { "BigIP resource not found" }).build();
        }
    }

    boolean possibleToUpdateFasit(BigIPOrderInput input) {
        String resourceApi = getSystemPropertyOrThrow("fasit_resources_v2_url", "No fasit resource api present");

        String url = resourceApi + "?type=LoadBalancerConfig&environment=" + input.getEnvironmentName() + "&application=" + input.getApplicationName();
        List resources = restClient.get(url, List.class).orElseThrow(() -> new RuntimeException("Unable to get LBConfig resources from Fasit"));

        return resources.size() <= 1;
    }

    boolean bigipResourceExists(BigIPOrderInput input) {
        String fasitRestUrl = getSystemPropertyOrThrow("fasit_scopedresource_v2_url", "No fasit scopedresource " +
                "rest-api present");
        String zone = input.getZone().toString();

        try {
            String url = String.format("%s?type=LoadBalancer&alias=bigip&environment=%s&application=%s&zone=%s",
                    fasitRestUrl, input.getEnvironmentName(), input.getApplicationName(), zone);
            return restClient.get(url, Map.class).isPresent();
        } catch (RuntimeException e) {
            log.warn("Unable to check if fasit resource exists", e);
            return false;
        }
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

    private BigIPOrderInput parse(@Context UriInfo uriInfo) {
        HashMap<String, String> request = ValidationHelper.queryParamsAsMap(uriInfo.getQueryParameters());
        ValidationHelper.validateRequiredParams(request, "environmentClass", "environmentName", "zone", "application");
        return new BigIPOrderInput(request);
    }

    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, BigIPOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        return fasitRestClient.getResource(input.getEnvironmentName(), alias, type, DomainDO.fromFqdn(domain.getFqn()), input.getApplicationName());
    }

}