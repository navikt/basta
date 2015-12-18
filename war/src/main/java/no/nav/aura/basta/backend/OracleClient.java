package no.nav.aura.basta.backend;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class OracleClient {
    private static final String PLUGGABLEDB_ORACLE_CONTENTTYPE = "application/oracle.com.cloud.common.PluggableDbPlatformInstance+json";
    private static final Logger log = LoggerFactory.getLogger(OracleClient.class);

    private final String oemUrl;
    private final String username;
    private final String password;

    public OracleClient(String oemUrl, String username, String password) {
        this.oemUrl = oemUrl;
        this.username = username;
        this.password = password;
    }

    public String createDatabase(String dbName, String password) {
        final String zoneURI = getZoneURI();
        log.debug("Creating database with name {} in zone {}", dbName, zoneURI);
        ClientRequest dbCreationRequest = createRequest(zoneURI).accept(PLUGGABLEDB_ORACLE_CONTENTTYPE);
        final String templateURIByName = getTemplateURIByName(zoneURI, "Pluggable Database 12c Bronze");
        final String payload = createPayload(dbName, password, templateURIByName);
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

    public String deleteDatabase(String databaseName) {
        final String databaseRequestURI = getDatabaseRequestURI(getZoneURI(), databaseName);
        log.debug("Got database request URI {}", databaseRequestURI);
        final ClientRequest request = createRequest(databaseRequestURI).accept(PLUGGABLEDB_ORACLE_CONTENTTYPE);
        try {
            final ClientResponse delete = request.delete();
            final Map response = (Map) delete.getEntity(Map.class);
            if (delete.getResponseStatus() != OK) {
                throw new RuntimeException("Unable to delete database " + databaseName + ". " + response);
            } else {
                return (String) response.get("uri");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete database", e);
        }
    }

    public String stopDatabase(String databaseName) {
        final String databaseRequestURI = getDatabaseRequestURI(getZoneURI(), databaseName);
        log.debug("Got database request URI {}", databaseRequestURI);
        final ClientRequest request = createRequest(databaseRequestURI).accept(PLUGGABLEDB_ORACLE_CONTENTTYPE);
        request.body(PLUGGABLEDB_ORACLE_CONTENTTYPE, "{\"operation\": \"SHUTDOWN\"}");
        try {
            final ClientResponse post = request.post();
            final Map response = (Map) post.getEntity(Map.class);

            final Map resource_state = (Map) response.get("resource_state");
            String state = (String) resource_state.get("state");

            if (!state.equalsIgnoreCase("initiated")) {
                throw new RuntimeException("Unable to stop database " + databaseName + ". " + response);
            } else {
                return (String) response.get("uri");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to stop database", e);
        }
    }

    public String startDatabase(String databaseName) {
        final String databaseRequestURI = getDatabaseRequestURI(getZoneURI(), databaseName);
        log.debug("Got database request URI {}", databaseRequestURI);
        final ClientRequest request = createRequest(databaseRequestURI).accept(PLUGGABLEDB_ORACLE_CONTENTTYPE);
        request.body(PLUGGABLEDB_ORACLE_CONTENTTYPE, "{\"operation\": \"STARTUP\"}");
        try {
            final ClientResponse post = request.post();
            final Map response = (Map) post.getEntity(Map.class);

            final Map resource_state = (Map) response.get("resource_state");
            String state = (String) resource_state.get("state");

            if (!state.equalsIgnoreCase("initiated")) {
                throw new RuntimeException("Unable to start database " + databaseName + ". " + response);
            } else {
                return (String) response.get("uri");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to start database", e);
        }
    }

    public String getStatus(String databaseName) {
        final String databaseRequestURI;

        try {
            databaseRequestURI = getDatabaseRequestURI(getZoneURI(), databaseName);
        } catch (RuntimeException e) {
            log.debug("Unable to get database request uri, assuming it doesn't exist", e);
            return "NONEXISTENT";
        }

        log.debug("Got database request URI {}", databaseRequestURI);
        final ClientRequest request = createRequest(databaseRequestURI);
        try {
            final ClientResponse get = request.get();
            final Map response = (Map) get.getEntity(Map.class);
            final String status = (String) response.get("status");

            log.debug("Got database status {}", status);

            return status;
        } catch (Exception e) {
            throw new RuntimeException("Unable to get database status", e);
        }
    }

    public static String createPayload(String databaseName, String password, String templateURIByName) {
        JsonArray tableSpaces = new JsonArray();
        tableSpaces.add(new JsonPrimitive(databaseName));

        JsonObject params = new JsonObject();
        params.add("tablespaces", tableSpaces);
        params.addProperty("pdb_name", databaseName);
        params.addProperty("username", databaseName);
        params.addProperty("password", password);
        params.addProperty("workload_name", "WORKLOAD PDB DEV"); // kan hentes fra
                                                                 // ...em/cloud/dbaas/pluggabledbplatformtemplate/<id>, bør
                                                                 // avklares. Konvensjon?

        params.addProperty("service_name", databaseName.replaceAll("[^A-Za-z0-9]", ""));
        params.addProperty("target_name", databaseName);

        JsonObject json = new JsonObject();
        json.add("params", params);
        json.addProperty("based_on", templateURIByName);
        json.addProperty("name", databaseName);
        return json.toString();
    }

    private String getDatabaseRequestURI(String zoneURI, String databaseName) {
        final ClientRequest request = createRequest(zoneURI);
        try {
            final Map zoneInfo = request.get(Map.class).getEntity();
            final Map service_instances = (Map) zoneInfo.get("service_instances");
            final List<Map> elements = (List<Map>) service_instances.get("elements");

            for (Map pluggableDatabase : elements) {
                final String pdbName = ((String) pluggableDatabase.get("name")).toLowerCase();
                if (pdbName.endsWith(databaseName.toLowerCase())) {
                    return (String) pluggableDatabase.get("uri");
                }
            }

            throw new RuntimeException("Unable to find request uri for database with name " + databaseName + " in zone " + zoneURI);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get database request URI", e);
        }
    }

    public boolean exists(String databaseName) {
        final ClientRequest request = createRequest(getZoneURI());
        try {
            final Map zoneInfo = request.get(Map.class).getEntity();
            final Map service_instances = (Map) zoneInfo.get("service_instances");
            final List<Map> elements = (List<Map>) service_instances.get("elements");

            for (Map pluggableDatabase : elements) {
                final String pdbName = ((String) pluggableDatabase.get("name")).toLowerCase();
                if (pdbName.endsWith(databaseName.toLowerCase())) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if database exists", e);
        }
    }

    private String getTemplateURIByName(String zoneURI, String templateName) {
        final ClientRequest request = createRequest(zoneURI);
        try {
            final Map zoneInfo = request.get(Map.class).getEntity();
            final Map templates = (Map) zoneInfo.get("templates");
            final List<Map> elements = (List<Map>) templates.get("elements");

            for (Map template : elements) {
                final String name = ((String) template.get("name")).toLowerCase();
                if (name.equalsIgnoreCase(templateName)) {
                    return (String) template.get("uri");
                }
            }

            throw new RuntimeException("Unable to find template with name " + templateName + " in zone " + zoneURI);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get database template URI", e);
        }
    }

    private String getZoneURI() {
        final String zoneName = "DEV_FSS"; // TODO: avklare konvensjon
        ClientRequest request = createRequest("/em/cloud");

        try {
            final Map response = request.get(Map.class).getEntity();
            final Map zones = (Map) response.get("zones");
            final List<Map> allZones = (List<Map>) zones.get("elements");
            final List<Map> dbaasZones = allZones.stream().filter(zone -> ((String) zone.get("service_family_type")).equalsIgnoreCase("dbaas")).collect(toList());

            for (Map zone : dbaasZones) {
                final String name = (String) zone.get("name");
                if (name.equalsIgnoreCase(zoneName)) {
                    return (String) zone.get("uri");
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
