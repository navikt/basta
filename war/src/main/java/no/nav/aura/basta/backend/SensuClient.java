package no.nav.aura.basta.backend;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static javax.ws.rs.core.Response.Status.OK;
import static no.nav.aura.basta.util.StatusLogHelper.abbreviateExceptionMessage;
import static no.nav.aura.basta.util.StatusLogHelper.addStatusLog;

import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

public class SensuClient {

    private static final String SENSU_BASEURL = System.getProperty("sensu_api.url");
    private static final Logger log = LoggerFactory.getLogger(SensuClient.class);

    public static void deleteClientsFor(String hostname, Order order) {
        List<Map> allClients = getAllClients(SENSU_BASEURL, order);

        if (allClients != null) {
            Set<String> clientNamesToDelete = getClientNamesWithHostname(allClients, hostname);
            if (clientNamesToDelete.isEmpty()) {
                log.info("No clients needed to be deleted from Sensu");
                addStatusLog(order, new OrderStatusLog("Basta", "No clients needed to be deleted from Sensu", "deleteSensuClient", StatusLogLevel.info));
            } else {
                for (String clientName : clientNamesToDelete) {
                    log.debug("Deleting " + clientName + " from Sensu");
                    try {
                        ClientResponse clientDeletionResponse = new ClientRequest(SENSU_BASEURL + "/clients/" + clientName).delete();
                        if (OK.equals(clientDeletionResponse.getResponseStatus())) {
                            addStatusLog(order, new OrderStatusLog("Basta", "Successfully deleted client " + clientName + " from Sensu", "deleteSensuClient", StatusLogLevel.success));
                        } else {
                            addStatusLog(order, new OrderStatusLog("Basta", "Unable to delete client " + clientName + " from Sensu", "deleteSensuClient", StatusLogLevel.warning));
                        }
                    } catch (Exception e) {
                        log.error("Unable to delete client " + clientName + " from Sensu.", e);
                        addStatusLog(order, new OrderStatusLog("Basta", "Unable to delete client " + clientName + " from Sensu", "deleteSensuClient", StatusLogLevel.warning));
                    }
                }
            }
        }
    }

    private static List<Map> getAllClients(String sensuBaseUrl, Order order) {
        String sensuClientsEndpoint = sensuBaseUrl + "/clients";
        try {
            ClientResponse getAllClientsResponse = new ClientRequest(sensuClientsEndpoint).get();
            return (List<Map>) getAllClientsResponse.getEntity(List.class);
        } catch (Exception e) {
            log.error("Unable to get clients from Sensu endpoint (" + sensuClientsEndpoint + ")", e);
            addStatusLog(order, new OrderStatusLog("Basta", "Unable to get clients from Sensu: " + abbreviateExceptionMessage(e), "deleteSensuClient", StatusLogLevel.warning));
            return null;
        }
    }

    public static Set<String> getClientNamesWithHostname(List<Map> payload, final String hostname) {
        Predicate<Map> onlyWithMatchingHostname = new Predicate<Map>() {
            @Override
            public boolean apply(Map map) {
                return hostname.equalsIgnoreCase((String) map.get("address"));
            }
        };

        Function<Map, String> extractName = new Function<Map, String>() {
            @Override
            public String apply(Map map) {
                return (String) map.get("name");
            }
        };

        return Sets.newHashSet(transform(filter(payload, onlyWithMatchingHostname), extractName));
    }
}
