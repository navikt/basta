package no.nav.aura.basta.rest.bigip;

import static java.util.stream.Collectors.toList;
import static no.nav.aura.basta.backend.BigIPClient.createEqualsCondition;
import static no.nav.aura.basta.backend.BigIPClient.createHostnameCondition;
import static no.nav.aura.basta.backend.BigIPClient.createStartsWithCondition;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.SUCCESS;
import static no.nav.aura.basta.domain.result.bigip.BigIPOrderResult.FASIT_ID;
import static no.nav.aura.basta.rest.dataobjects.StatusLogLevel.info;
import static no.nav.aura.basta.util.StringHelper.isEmpty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import jakarta.inject.Inject;
import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
import no.nav.aura.basta.backend.fasit.rest.model.ApplicationPayload;
import no.nav.aura.basta.backend.fasit.rest.model.EnvironmentPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.domain.result.bigip.BigIPOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.util.ValidationHelper;

@Component
@RestController
@RequestMapping("/rest/v1/bigip")
@Transactional
public class BigIPOrderRestService {

    public static final String DUMMY_RULE_NAME = "dummy_rule";
    private static final Logger log = LoggerFactory.getLogger(BigIPOrderRestService.class);
    private static final String PARTITION = "AutoProv";
    private BigIPClientSetup bigIPClientSetup;
    private OrderRepository orderRepository;
    private FasitUpdateService fasitUpdateService;
    private FasitRestClient fasitRestClient;

    public BigIPOrderRestService() {}

    @Inject
	public BigIPOrderRestService(OrderRepository orderRepository, FasitUpdateService fasitUpdateService, FasitRestClient fasitRestClient, BigIPClientSetup bigIPClientSetup) {
        this.orderRepository = orderRepository;
        this.fasitUpdateService = fasitUpdateService;
        this.bigIPClientSetup = bigIPClientSetup;
        this.fasitRestClient = fasitRestClient;
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

    // Checks if policy has any rules that contains context-roots in conflict with incoming
    private static List<Map<String, String>> getConflictingRules(String policyName, String contextRoots, BigIPClient bigIPClient, Set<String> ruleNames) {
        List<Map<String, String>> conflictingRules = Lists.newArrayList();
        Map policy = bigIPClient.getRules(policyName);

        Set<Tuple<String, String>> ruleValues = Sets.newHashSet();
        List<Map> rules = (List<Map>) policy.get("items");
        if (rules != null) {
            for (Map rule : rules) {
                log.debug("Reading rule " + rule.get("name"));
                Map conditionsReference = (Map) rule.get("conditionsReference");
                List<Map> conditions = (List<Map>) conditionsReference.get("items");
                if (conditions != null) {
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

    @PostMapping
    public ResponseEntity<?> createBigIpConfig(@RequestBody Map<String, String> request) {
        log.debug("Got request with payload {}", request);
        ValidationHelper.validateRequest("/validation/createBigIPConfigSchema.json", request);
        BigIPOrderInput input = new BigIPOrderInput(request);
        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        if (!input.getUseHostnameMatching() && sanitizeContextRoots(input.getContextRoots()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided context roots was invalid");
        } else if (input.getUseHostnameMatching() && !input.getHostname().isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hostname was specified");
        } else if (input.getUseHostnameMatching() && isCommonVS(input.getHostname().get())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot create a hostname matching rule for a common VS, please read instructions and try again");
        }
        verifyFasitEntities(input);
        log.info("Verified that application {} and environment {} exists in Fasit, and that BIG-IP resource exists for the provided scope", input.getApplicationName(), input.getEnvironmentName());
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

        String vsUrl = input.getHostname().orElse(bigIPClient.getVirtualServerIP(input.getVirtualServer()));
        ResourcePayload lbConfig = createLBConfigResource(input, poolName, vsUrl);

        order = orderRepository.save(order);

        Optional<Long> resourceId = getPotentiallyExistingLBConfigId(input);

        Optional<String> maybeFasitResourceId = fasitUpdateService.createOrUpdateResource(resourceId.orElse(null), lbConfig, order);

        if (!maybeFasitResourceId.isPresent()) {
            order.setStatus(FAILURE);
        } else {
            BigIPOrderResult result = order.getResultAs(BigIPOrderResult.class);
            result.put(FASIT_ID, maybeFasitResourceId.get());
            order.setStatus(SUCCESS);
        }

        order = orderRepository.save(order);
        return ResponseEntity.ok(order.getId());
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

            bigIPClient.createRuleOnPolicy(hostnameRuleName, policyName, poolName, createHostnameCondition(input.getHostname().get()), usesPolicyDrafts);
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
        ScopePayload scope = new ScopePayload()
                .environmentClass(input.getEnvironmentClass())
                .environment(input.getEnvironmentName())
                .application(input.getApplicationName());

        List<ResourcePayload> existing = fasitRestClient.findFasitResources(ResourceType.LoadBalancerConfig, null, scope);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        Long fasitId = existing.get(0).id;
        log.debug("Found existing LBConfig resource in Fasit with id {}", fasitId);
        return Optional.ofNullable(fasitId);
    }

    private String getLBConfigAlias(String applicationName) {
        return "loadbalancer:" + applicationName;
    }

    private ResourcePayload createLBConfigResource(BigIPOrderInput input, String poolName, String url) {
        ScopePayload scope = new ScopePayload()
        		.environmentClass(input.getEnvironmentClass())
                .environment(input.getEnvironmentName())
                .application(input.getApplicationName());

        HashMap<String, String> properties = new HashMap<>();
        properties.put("url", url);
        properties.put("poolName", poolName);
        
        if (!isEmpty(input.getContextRoots())) {
            properties.put("contextRoots", input.getContextRoots());
        }

        ResourcePayload lbConfig = new ResourcePayload(ResourceType.LoadBalancerConfig, getLBConfigAlias(input.getApplicationName()));
        lbConfig.setScope(scope);
        lbConfig.setProperties(properties);

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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No virtual server found on BIG-IP with name " + input.getVirtualServer());
        }

        String policyName = getForwardingPolicy(virtualServerResponse, bigIPClient);

        if (policyName != null && !input.getUseHostnameMatching()) {
            List<Map<String, String>> conflictingRules = getConflictingRules(policyName, input.getContextRoots(), bigIPClient,
                    BigIPNamer.createRuleNames(input.getApplicationName(), input.getEnvironmentName(), input.getEnvironmentClass().name()));

            if (!conflictingRules.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Policy " + policyName + " has rules that conflict with the provided context roots");
            }
        }
    }

    private void verifyFasitEntities(BigIPOrderInput input) {
//        boolean applicationDefinedInFasit = fasitRestClient.get(fasitUrl + "/api/v2/applications/" + input.getApplicationName(), Map.class).isPresent();
        ApplicationPayload applicationDefined = fasitRestClient.getApplicationByName(input.getApplicationName());
        if (applicationDefined == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find any applications in Fasit with name " + input.getApplicationName());
        }

//        boolean environmentDefinedInFasit = fasitRestClient.get(fasitUrl + "/api/v2/environments/" + input.getEnvironmentName(), Map.class).isPresent();
        EnvironmentPayload environmentDefined = fasitRestClient.getEnvironmentByName(input.getEnvironmentName());
        if (environmentDefined == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find any environments in Fasit with name " + input.getEnvironmentName());
        }

        boolean loadbalancerResourceDefinedInFasit = bigipResourceExists(input);
        if (!loadbalancerResourceDefinedInFasit) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find any BIG-IP instances/resources for the provided scope");
        }

        if (!possibleToUpdateFasit(input)) {
        	log.error("Multiple LBConfig resources exists in Fasit for the provided scope, unable to determine which one to update");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Multiple resources lbConfig resources exists in scope for this application, unable to choose which one to update");
        }
    }

    @GetMapping("/virtualservers")
    public ResponseEntity<?> getVirtualServers(
            @RequestParam String environmentClass,
            @RequestParam String environmentName,
            @RequestParam String zone,
            @RequestParam String application) {
        HashMap<String, String> request = new HashMap<>();
        request.put("environmentClass", environmentClass);
        request.put("environmentName", environmentName);
        request.put("zone", zone);
        request.put("application", application);
        
        BigIPOrderInput input = new BigIPOrderInput(request);
        ResourcePayload bigipResource;
        try {
        	ScopePayload scope = new ScopePayload()
        			.environmentClass(input.getEnvironmentClass())
        			.environment(input.getEnvironmentName())
        			.application(input.getApplicationName())
        			.zone(input.getZone());
//            bigipResource = getFasitResource(ResourceType.LoadBalancer, "bigip", input);
            fasitRestClient.getScopedFasitResource(ResourceType.LoadBalancer, "bigip", scope);
        } catch (IllegalArgumentException e) {
            log.warn("No BIG-IP resource found in Fasit for the provided scope: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new String[] { "BigIP resource not found" });
        }
        BigIPClient bigIPClient = bigIPClientSetup.setupBigIPClient(input);
        List<Map<String, Object>> virtualServers = bigIPClient.getVirtualServers(PARTITION);
        log.info("Found virtual servers {} on BIG-IP instance {}", virtualServers.stream().map(vs -> vs.get("name")).collect(toList()), bigIPClient.getHostname());
        List<String> names = virtualServers.stream().map(map -> (String) map.get("name")).collect(toList());
        return ResponseEntity.ok(names);
    }

    boolean possibleToUpdateFasit(BigIPOrderInput input) {
        ScopePayload scope = new ScopePayload()
				.environmentClass(input.getEnvironmentClass())
				.environment(input.getEnvironmentName())
				.application(input.getApplicationName());
        List<ResourcePayload> resources = fasitRestClient.findFasitResources(ResourceType.LoadBalancerConfig, null, scope);
        log.debug("Found {} LBConfig resources in Fasit for the provided scope", resources.size());
        return resources.size() <= 1;
    }

    boolean bigipResourceExists(BigIPOrderInput input) {
        ScopePayload scope = new ScopePayload();
        scope.environmentClass(input.getEnvironmentClass());
        scope.environment(input.getEnvironmentName());
        scope.application(input.getApplicationName());
        scope.zone(input.getZone());

        try {
            ResourcePayload resource = fasitRestClient.getScopedFasitResource(ResourceType.LoadBalancer, "bigip", scope);
            return resource != null;
        } catch (Exception e) {
            log.warn("Could not find BIG-IP resource in Fasit: {}", e.getMessage());
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

//    private ResourcePayload getFasitResource(ResourceType type, String alias, BigIPOrderInput input) {
//    	ScopePayload scope = new ScopePayload();
//    	scope.environmentClass(input.getEnvironmentClass());
//    	scope.environment(input.getEnvironmentName());
//    	scope.application(input.getApplicationName());
//    	scope.zone(input.getZone());
//    	
//    	return fasitRestClient.getScopedFasitResource(type, alias, scope);
//    }

}
