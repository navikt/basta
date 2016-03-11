package no.nav.aura.basta.backend.bigip;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOExceptionWithCause;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ActiveBigIPInstanceFinder {

    private static Logger log = LoggerFactory.getLogger(ActiveBigIPInstanceFinder.class);

    public String getActiveBigIPInstance(ResourceElement loadBalancerInstance, String username, String password) {

        RestClient restClient = new RestClient(username,password);

        Set<String> instances = Sets.newHashSet(loadBalancerInstance.getPropertyString("hostname"), loadBalancerInstance.getPropertyString("secondary_hostname"));

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



    private ClientRequest createClientRequest(String url, String credentials) {
        ClientRequest clientRequest = new ClientRequest(url, TrustyExecutor.getTrustingExecutor());

        if (credentials != null) {
            clientRequest.header("Authorization", "Basic " + credentials);
        }

        clientRequest.header("Content-Type", "application/json");
        clientRequest.header("Accept", "application/json");
        return clientRequest;
    }

    private String encodeCredentials(String username, String password) {
        byte[] credentials = (username + ':' + password).getBytes();
        return new String(Base64.encodeBase64(credentials));
    }

}
