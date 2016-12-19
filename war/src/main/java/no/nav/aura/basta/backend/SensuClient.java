package no.nav.aura.basta.backend;

import no.nav.aura.basta.domain.Order;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static no.nav.aura.basta.util.StatusLogHelper.abbreviateExceptionMessage;

public class SensuClient {

    private static final String SENSU_BASEURL = System.getProperty("sensu_api.url");
    private static final Logger log = LoggerFactory.getLogger(SensuClient.class);

    public static void deleteClientsFor(String hostname, Order order) {
        List<Map<String, String>> allClients = getAllClients(SENSU_BASEURL, order);

        if (allClients != null) {
            Set<String> clientNamesToDelete = getClientNamesWithHostname(allClients, hostname);
            if (clientNamesToDelete.isEmpty()) {
                log.info("No clients needed to be deleted from Sensu");
                order.addStatuslogInfo("No clients needed to be deleted from Sensu.");
            } else {
                for (String clientName : clientNamesToDelete) {
                    log.debug("Deleting " + clientName + " from Sensu");
                    try {
                        RestClient restClient = new RestClient();
                        Response response = restClient.delete(SENSU_BASEURL + "/clients/" + clientName);

                        if (ACCEPTED.getStatusCode() == response.getStatus()) {
                            order.addStatuslogSuccess("Successfully deleted client " + clientName + " from Sensu");
                        } else {
                            order.addStatuslogWarning("Unable to delete client " + clientName + " from Sensu. Got: " + response.getStatus());
                        }
                    } catch (Exception e) {
                        log.error("Unable to delete client " + clientName + " from Sensu.", e);
                        order.addStatuslogWarning("Unable to delete client " + clientName + " from Sensu");
                    }
                }
            }
        }
    }

    private static List<Map<String, String>> getAllClients(String sensuBaseUrl, Order order) {
        String sensuClientsEndpoint = sensuBaseUrl + "/clients";
        try {
            ClientResponse getAllClientsResponse = new ClientRequest(sensuClientsEndpoint).get();
            return (List<Map<String, String>>) getAllClientsResponse.getEntity(List.class);
        } catch (Exception e) {
            log.error("Unable to get clients from Sensu endpoint (" + sensuClientsEndpoint + ")", e);
            order.addStatuslogWarning("Unable to get clients from Sensu: " + abbreviateExceptionMessage(e));
            return null;
        }
    }

    protected static Set<String> getClientNamesWithHostname(List<Map<String,String>> payload, final String hostname) {

        return payload.stream()
            .filter(host -> hostname.equalsIgnoreCase(host.get("address")))
                .map(host -> host.get("name"))
                .collect(Collectors.toSet());
    }
}
