package no.nav.aura.basta.backend;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.OK;

public class OracleClient {
    private static final String PLUGGABLEDB_ORACLE_CONTENTTYPE = "application/oracle.com.cloud.common.PluggableDbPlatformInstance+json";
    private static final Logger log = LoggerFactory.getLogger(OracleClient.class);
    public static final String NONEXISTENT = "NONEXISTENT";

    private final String oemUrl;
    private final String username;
    private final String password;

    public OracleClient(String oemUrl, String username, String password) {
        this.oemUrl = oemUrl;
        this.username = username;
        this.password = password;
    }

    public String createDatabase(String dbName, String password, String zoneName, String templateURI) {
        final String zoneURI = getZoneURIFrom(zoneName);
        log.debug("Creating database with name {} in zone {}", dbName, zoneURI);
        ClientRequest dbCreationRequest = createRequest(zoneURI).accept(PLUGGABLEDB_ORACLE_CONTENTTYPE);
        final String payload = createPayload(dbName, password, templateURI);
        dbCreationRequest.body(PLUGGABLEDB_ORACLE_CONTENTTYPE, payload);

        try {
            log.debug("Sending HTTP POST to OEM with payload {}", payload.replace(password, "*****"));
            final ClientResponse post = dbCreationRequest.post();
            Map response = (Map) post.getEntity(Map.class);

            if (post.getResponseStatus() != OK) {
                log.info("Unable to create database {}. {}", dbName, response);
                throw new RuntimeException("Unable to create database " + dbName + ". " + response);
            }
            final String uri = (String) response.get("uri");

            log.info("Successfully sent database creation order to OEM, got URI {}", uri);
            return uri;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteDatabase(String dbURI) {
        log.debug("Deleting database with URI {}", dbURI);
        final ClientRequest request = createRequest(dbURI).accept(PLUGGABLEDB_ORACLE_CONTENTTYPE);
        try {
            final ClientResponse delete = request.delete();
            final Map response = (Map) delete.getEntity(Map.class);
            if (delete.getResponseStatus() != OK) {
                throw new RuntimeException("Unable to delete database with URI " + dbURI + ". " + response);
            } else {
                return (String) response.get("uri");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete database with URI " + dbURI, e);
        }
    }

    public String stopDatabase(String dbURI) {
        log.debug("Stopping database with URI {}", dbURI);
        final ClientRequest request = createRequest(dbURI).accept(PLUGGABLEDB_ORACLE_CONTENTTYPE);
        request.body(PLUGGABLEDB_ORACLE_CONTENTTYPE, "{\"operation\": \"SHUTDOWN\"}");
        try {
            final ClientResponse post = request.post();
            final Map response = (Map) post.getEntity(Map.class);

            final Map resource_state = (Map) response.get("resource_state");
            String state = (String) resource_state.get("state");

            if (!state.equalsIgnoreCase("initiated")) {
                throw new RuntimeException("Unable to stop database with URI " + dbURI + ". " + response);
            } else {
                return (String) response.get("uri");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to stop database with URI " + dbURI, e);
        }
    }

    public String startDatabase(String dbURI) {
        log.debug("Starting database with URI {}", dbURI);
        final ClientRequest request = createRequest(dbURI).accept(PLUGGABLEDB_ORACLE_CONTENTTYPE);
        request.body(PLUGGABLEDB_ORACLE_CONTENTTYPE, "{\"operation\": \"STARTUP\"}");
        try {
            final ClientResponse post = request.post();
            final Map response = (Map) post.getEntity(Map.class);

            final Map resource_state = (Map) response.get("resource_state");
            String state = (String) resource_state.get("state");

            if (!state.equalsIgnoreCase("initiated")) {
                throw new RuntimeException("Unable to start database with URI " + dbURI + ". " + response);
            } else {
                return (String) response.get("uri");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to start database with URI " + dbURI, e);
        }
    }

    public String getStatus(String dbURI) {
        final ClientRequest request = createRequest(dbURI);
        try {
            final ClientResponse get = request.get();
            final Map response = (Map) get.getEntity(Map.class);
            final String status = (String) response.get("status");

            if (get.getResponseStatus() != OK || status == null) {
                log.debug("Unable to get status from provided database URI {}, assuming it doesn't exist", dbURI);
                return NONEXISTENT;
            }

            log.debug("Got database status {} for DB with URI {}", status, dbURI);
            return status;
        } catch (Exception e) {
            throw new RuntimeException("Unable to get database status", e);
        }
    }

    public String createPayload(String databaseName, String password, String templateURI) {
        JsonArray tableSpaces = new JsonArray();
        tableSpaces.add(new JsonPrimitive(databaseName.toUpperCase()));

        JsonObject params = new JsonObject();
        params.add("tablespaces", tableSpaces);
        params.addProperty("pdb_name", databaseName);
        params.addProperty("username", databaseName);
        params.addProperty("password", password);
        params.addProperty("workload_name", getWorkloadNameFor(templateURI));
        params.addProperty("service_name", databaseName.replaceAll("[^A-Za-z0-9]", ""));
        params.addProperty("target_name", databaseName);

        JsonObject json = new JsonObject();
        json.add("params", params);
        json.addProperty("based_on", templateURI);
        json.addProperty("name", databaseName);
        return json.toString();
    }

    private String getWorkloadNameFor(String templateURI) {
        final ClientRequest request = createRequest(templateURI);
        try {
            final Map response = request.get(Map.class).getEntity();
            final List<Map> workloads = (List<Map>) response.get("workloads");

            if (workloads.isEmpty()) {
                throw new RuntimeException("No workloads defined for template with URI " + templateURI);
            } else {
                final Map<String, String> workload = workloads.get(0);
                final String workloadName = workload.get("name");
                log.debug("Found workload name {} for template URI {}", workloadName, templateURI);
                return workloadName;
            }

        } catch (Exception e) {
            throw new RuntimeException("Unable to find workload name for template uri  " + templateURI, e);
        }
    }

    public List<Map<String, String>> getTemplatesForZone(String zoneName) {
        final String zoneURI = getZoneURIFrom(zoneName);
        final ClientRequest request = createRequest(zoneURI);

        try {
            final Map zoneInfo = request.get(Map.class).getEntity();
            final Map templates = (Map) zoneInfo.get("templates");
            final List<Map> allElements = (List<Map>) templates.get("elements");
            final List<Map> dbaasElements = allElements.stream().filter(element -> ((String) element.get("type")).equalsIgnoreCase("dbaas")).collect(toList());

            Map<String, String> templatesMap = Maps.newHashMap();
            List<Map<String, String>> templatesList = Lists.newArrayList();

            for (Map<String, String> template : dbaasElements) {
                templatesList.add(ImmutableMap.of("uri", template.get("uri"), "description", template.get("description"), "name", template.get("name").toLowerCase()));
            }

            return templatesList;
        } catch (Exception e) {
            throw new RuntimeException("Unable to get database templates for zone with URI " + zoneURI, e);
        }
    }

    public String getZoneURIFrom(final String zoneName) {
        ClientRequest request = createRequest("/em/cloud");

        try {
            final Map response = request.get(Map.class).getEntity();
            final Map zones = (Map) response.get("zones");
            final List<Map> allZones = (List<Map>) zones.get("elements");
            final List<Map> dbaasZones = allZones.stream().filter(zone -> ((String) zone.get("service_family_type")).equalsIgnoreCase("dbaas")).collect(toList());

            for (Map<String, String> zone : dbaasZones) {
                final String name = zone.get("name");
                if (name.equalsIgnoreCase(zoneName)) {
                    return zone.get("uri");
                }
            }

            throw new RuntimeException("Unable to find zone with name " + zoneName);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get zone URI", e);
        }
    }

    private ClientRequest createRequest(String path) {
        ClientRequest request = new ClientRequest(oemUrl + path);
        request.header("Authorization", "Basic " + base64EncodeString(username + ":" + password));
        return request;
    }

    private static String base64EncodeString(String string) {
        return new String(Base64.getEncoder().encode(string.getBytes()));
    }

    // this is silly, but it's in order to mock a different response for deletions
    public Map getDeletionOrderStatus(String orderURI) {
        return getOrderStatus(orderURI);
    }

    public Map getOrderStatus(String orderURI) {
        log.debug("Getting status for order with URI {}", orderURI);
        final ClientRequest request = createRequest(orderURI);

        try {
            return request.get(Map.class).getEntity();
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if order with URI " + orderURI + " is finished", e);
        }
    }

}