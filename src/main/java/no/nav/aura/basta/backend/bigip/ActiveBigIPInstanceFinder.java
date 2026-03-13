package no.nav.aura.basta.backend.bigip;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;


@Component
public class ActiveBigIPInstanceFinder {

    private static Logger log = LoggerFactory.getLogger(ActiveBigIPInstanceFinder.class);

    public String getActiveBigIPInstance(ResourcePayload loadBalancerInstance, String username, String password) {

        RestClient restClient = new RestClient(username, password);

        Map<String, String> props = loadBalancerInstance.getProperties();
        Set<String> instances = Sets.newHashSet(props.get("hostname"), props.get("secondary_hostname"));

        log.debug("Checking the following instances to see which one is active: {}", instances);

        for (String instance : instances) {
            log.debug("Checking instance {}", instance);
            String url = "https://" + instance + "/mgmt/tm/cm/device";

            Optional<Map> entity;
            try {
                log.debug("GET url {}", url);
                entity = restClient.get(url, Map.class);
            } catch (Exception e) {
                throw new RuntimeException("Unable to find active bigip instance", e);
            }

            List<Map> items = (List<Map>) entity.get().get("items");

            for (Map item : items) {
                String failoverState = (String) item.get("failoverState");
                String ip = (String) item.get("managementIp");
                log.debug("Found BIG-IP item with state {} and IP {}", failoverState, ip);
                if (failoverState.equalsIgnoreCase("active")) {
                    return ip;
                }
            }
        }

        return null;
    }
}