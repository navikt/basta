package no.nav.aura.basta.backend;

import static com.google.common.collect.Collections2.transform;
import static java.util.Collections.emptyMap;

import java.util.*;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

@Component
public class BigIPClient {
    private static final Logger log = LoggerFactory.getLogger(BigIPClient.class);
    private RestClient restClient;

    private String hostname;
    private String baseUrl;

    public BigIPClient() {
    }

    public BigIPClient(String hostname, String username, String password) {
        this.restClient = new RestClient(username, password);
        this.hostname = hostname;
        this.baseUrl = "https://" + hostname + "/mgmt/tm/ltm";
    }

    public static Map<String, Object> createHostnameCondition(String hostname) {
        Map<String, Object> hostnameCondition = Maps.newHashMap();
        hostnameCondition.put("name", "2");
        hostnameCondition.put("equals", true);
        hostnameCondition.put("caseInsensitive", true);
        hostnameCondition.put("httpHost", true);
        hostnameCondition.put("host", true);
        hostnameCondition.put("request", true);
        hostnameCondition.put("values", Sets.newHashSet(hostname));
        return hostnameCondition;
    }

    public static Map<String, Object> createEqualsCondition(Set<String> contextRoots) {
        Map<String, Object> equalsCondition = Maps.newHashMap();
        equalsCondition.put("name", "0");
        equalsCondition.put("equals", true);
        equalsCondition.put("caseInsensitive", true);
        equalsCondition.put("httpUri", true);
        equalsCondition.put("path", true);
        equalsCondition.put("values", prefixWithSlash(contextRoots));
        return equalsCondition;
    }

    public static Map<String, Object> createStartsWithCondition(Set<String> contextRoots) {
        Map<String, Object> startsWithCondition = Maps.newHashMap();
        startsWithCondition.put("name", "1");
        startsWithCondition.put("startsWith", true);
        startsWithCondition.put("caseInsensitive", true);
        startsWithCondition.put("httpUri", true);
        startsWithCondition.put("path", true);
        startsWithCondition.put("values", wrapWithSlash(contextRoots));
        return startsWithCondition;
    }

    private static Set<String> wrapWithSlash(Set<String> strings) {
        Function<String, String> wrapWithSlashes = new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable String input) {
                return "/" + input + "/";
            }
        };

        return Sets.newHashSet(transform(strings, wrapWithSlashes));
    }

    private static Set<String> prefixWithSlash(Set<String> strings) {
        Function<String, String> prefixWithSlash = new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable String input) {
                return "/" + input;
            }
        };

        return Sets.newHashSet(transform(strings, prefixWithSlash));
    }

    public String getHostname() {
        return this.hostname;
    }

    public Map getPool(String poolName) {
        return restClient.get(baseUrl + "/pool/~AutoProv~" + poolName, Map.class).orElse(emptyMap());
    }

    public Optional<Map> getVirtualServer(String virtualServerName) {
        return restClient.get(baseUrl + "/virtual/~AutoProv~" + virtualServerName + "?expandSubcollections=true", Map.class);
    }

    public List<Map<String, Object>> getVirtualServers(String partition) {
        Map response = restClient.get(baseUrl + "/virtual?$filter=partition%20eq%20" + partition, Map.class).orElse(new HashMap());
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        return items == null ? new ArrayList<>() : items;
    }

    public void createPolicy(String policyName) {
        Map<String, Object> policy = Maps.newHashMap();
        policy.put("name", policyName);
        policy.put("partition", "AutoProv");
        policy.put("strategy", "/Common/first-match");
        policy.put("controls", new String[] { "forwarding" });
        policy.put("requires", new String[] { "http" });

        restClient.post(baseUrl + "/policy", new Gson().toJson(policy));
    }

    public void createPool(String poolName) {
        Map<String, Object> pool = Maps.newHashMap();
        pool.put("name", poolName);
        pool.put("partition", "AutoProv");
        pool.put("slowRampTime", 16);
        pool.put("loadBalancingMode", "least-connections-member");

        restClient.post(baseUrl + "/pool", new Gson().toJson(pool));
    }

    public void createRuleOnPolicy(String ruleName, String policyName, String poolName, Map<String, Object> condition) {
        Map<String, Object> rule = Maps.newHashMap();
        rule.put("name", ruleName);

        Map<String, Object> actionsReference = Maps.newHashMap();
        Map<String, Object> action = Maps.newHashMap();
        action.put("name", "1");
        action.put("pool", "/AutoProv/" + poolName);
        action.put("forward", true);
        actionsReference.put("items", new Map[] { action });
        rule.put("actionsReference", actionsReference);

        Map<String, Object> conditionsReference = Maps.newHashMap();
        conditionsReference.put("items", new Map[] { condition });
        rule.put("conditionsReference", conditionsReference);

        restClient.post(baseUrl + "/policy/~AutoProv~" + policyName + "/rules", new Gson().toJson(rule));
    }

    public String getVirtualServerIP(String virtualServer) {
        Optional<Map> maybeVs = restClient.get(baseUrl + "/virtual/~AutoProv~" + virtualServer, Map.class);
        if (!maybeVs.isPresent()) {
            throw new RuntimeException("Unable to get IP address from virtual server " + virtualServer);
        }
        String destination = (String) maybeVs.get().get("destination");
        String ip = destination.split("/")[2].split(":")[0];
        return ip;
    }

    public void createDummyRuleOnPolicy(String policyName, String ruleName) {
        Map dummyRule = Maps.newHashMap();
        dummyRule.put("name", ruleName);

        Map<String, Object> actionsReference = Maps.newHashMap();
        Map<String, Object> action = Maps.newHashMap();
        action.put("name", "0");
        action.put("log", true);
        action.put("message", "dummyval");
        action.put("code", 0);
        action.put("write", true);
        actionsReference.put("items", new Map[] { action });
        dummyRule.put("actionsReference", actionsReference);

        Map<String, Object> conditionsReference = Maps.newHashMap();
        Map<String, Object> condition = Maps.newHashMap();
        condition.put("name", "1");
        condition.put("all", true);
        condition.put("httpVersion", true);
        condition.put("values", new String[] { "dummyval" });
        conditionsReference.put("items", new Map[] { condition });
        dummyRule.put("conditionsReference", conditionsReference);

        restClient.post(baseUrl + "/policy/~AutoProv~" + policyName + "/rules", new Gson().toJson(dummyRule));
    }

    public Map getPolicy(String policyName) {
        log.debug("Getting policy with name {}", policyName);
        return restClient.get(baseUrl + "/policy/~AutoProv~" + policyName, Map.class).orElse(emptyMap());
    }

    public Map getRules(String policyName) {
        log.debug("Getting rules for policy with name {}", policyName);
        return restClient.get(baseUrl + "/policy/~AutoProv~" + policyName + "/rules?expandSubcollections=true", Map.class).orElse(emptyMap());
    }

    public Set<String> getPoliciesFrom(Map virtualServer) {
        Set<String> policyNames = Sets.newHashSet();
        Map policiesReference = (Map) virtualServer.get("policiesReference");
        List<Map<String, String>> policies = (List<Map<String, String>>) policiesReference.get("items");

        if (policies != null) {
            for (Map<String, String> policy : policies) {
                policyNames.add(policy.get("name"));
            }
        }

        return policyNames;
    }

    public void mapPolicyToVS(String policyName, String virtualServer) {
        Map<String, Object> vsUpdateRequest = Maps.newHashMap();
        Map<String, Object> policiesReference = Maps.newHashMap();
        vsUpdateRequest.put("policiesReference", policiesReference);
        Map<String, String> policy = Maps.newHashMap();
        policy.put("name", policyName);
        policiesReference.put("items", new Map[] { policy });

        restClient.put(baseUrl + "/virtual/~AutoProv~" + virtualServer, new Gson().toJson(vsUpdateRequest));
    }

    public Response deleteRuleFromPolicy(String policyName, String ruleName) {

        Response response = restClient.delete(baseUrl + "/policy/~AutoProv~" + policyName + "/rules/" + ruleName);
        log.info("Deleted rule {} on policy {}", ruleName, policyName);
        return response;
    }
}