package no.nav.aura.basta.backend;

import static java.util.Collections.emptyMap;

import java.util.*;

import no.nav.aura.basta.backend.bigip.RestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
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
        setHostname(hostname);
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        this.baseUrl = "https://" + hostname + "/mgmt/tm/ltm";
    }

    public void setCredentials(String username, String password) {
        log.debug("set credentials for {}", username);
        this.restClient = new RestClient(username, password);
    }

    public String getHostname() {
        return this.hostname;
    }

    public Map getPool(String poolName) {
        return restClient.get(baseUrl + "/pool/~AutoProv~" + poolName, Map.class).or(emptyMap());
    }

    public Optional<Map> getVirtualServer(String virtualServerName) {
        return restClient.get(baseUrl + "/virtual/~AutoProv~" + virtualServerName, Map.class);
    }

    public List<Map<String, Object>> getVirtualServers(String partition) {
        Map response = restClient.get(baseUrl + "/virtual?$filter=partition%20eq%20" + partition, Map.class).or(new HashMap());
        List<Map<String,Object>> items = (List<Map<String,Object>>) response.get("items");
        return items == null ? new ArrayList<>() : items;

    }

    public static void main(String[] args) {
        BigIPClient bigIPClient = new BigIPClient("10.33.43.241", "autoprov", "provauto");
        bigIPClient.createPolicy("policyasdf2");
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

    public Map getPolicy(String policyName) {
        log.debug("Getting policy with name {}", policyName);
        return restClient.get(baseUrl + "/policy/~AutoProv~" + policyName, Map.class).or(emptyMap());
    }

    public Map getRules(String policyName) {
        log.debug("Getting rules for policy with name {}", policyName);
        return restClient.get(baseUrl + "/policy/~AutoProv~" + policyName + "/rules?expandSubcollections=true", Map.class).or(emptyMap());
    }

    public static Set<String> getPoliciesFrom(Map virtualServer) {
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

}