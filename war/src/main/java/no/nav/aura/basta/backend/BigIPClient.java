package no.nav.aura.basta.backend;

import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.aura.basta.backend.bigip.RestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;


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

    public void createPool(String poolName) {
        if (getPool(poolName) != null){
            //createpool
        }
    }

    public Map getPolicy(String policyName) {
        log.debug("Getting policy with name {}", policyName);
        return restClient.get(baseUrl + "/policy/~AutoProv~" + policyName + "/rules?expandSubcollections=true", Map.class).or(emptyMap());
    }
}