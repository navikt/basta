package no.nav.aura.basta.backend;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.networknt.org.apache.commons.validator.routines.DomainValidator;
import com.sun.jdi.InternalException;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.net.URISyntaxException;

import static jakarta.ws.rs.core.Response.Status.OK;

public class OracleClient {
    private static final String PLUGGABLEDB_ORACLE_CONTENTTYPE = "application/oracle.com.cloud.common.PluggableDbPlatformInstance+json";
    private static final Logger log = LoggerFactory.getLogger(OracleClient.class);
    public static final String NONEXISTENT = "NONEXISTENT";

    private final URI oemUrl;
    private final String username;
    private final String password;
    private DomainValidator validator = DomainValidator.getInstance();

    public OracleClient(String oemUrl, String username, String password) throws URISyntaxException {
        this.oemUrl = new URI(oemUrl);
        this.username = username;
        this.password = password;
    }

    public String createDatabase(String dbName, String password, String zoneURI, String templateURI) {
        log.debug("Creating database with name {} in zone {}", dbName, zoneURI);
        final WebTarget dbCreationRequest = createRequest(zoneURI);
        final String payload = createPayload(dbName, password, templateURI);

        try {
            log.debug("Sending HTTP POST to OEM with payload {}", payload.replace(password, "*****"));
            final Response postResponse = dbCreationRequest.request(PLUGGABLEDB_ORACLE_CONTENTTYPE).post(Entity.entity(payload, PLUGGABLEDB_ORACLE_CONTENTTYPE));
            Map response = (Map) postResponse.readEntity(Map.class);

            if (postResponse.getStatusInfo() != OK) {
                log.error("Unable to create database {}. {}", dbName, response);
                throw new RuntimeException("Unable to create database " + dbName + ". " + response);
            }
            final String uri = (String) response.get("uri");

            log.debug("Successfully sent database creation order to OEM, got URI {}", uri);
            return uri;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getStatus(String dbURI) {
        final WebTarget request = createRequest(dbURI);
        try {
            final Response getResponse = request.request().get();
            final Map response = (Map) getResponse.readEntity(Map.class);
            final String status = (String) response.get("status");

            if (getResponse.getStatusInfo() != OK || status == null) {
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
        final WebTarget request = createRequest(templateURI);
        try {
            final Map response = request.request().get(Map.class);
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

    public List<Map<String, String>> getTemplatesForZone(String zoneURI) {
        final WebTarget request = createRequest(zoneURI);

        try {
            final Map zoneInfo = request.request().get(Map.class);
            final Map templates = (Map) zoneInfo.get("templates");
            final List<Map> allElements = (List<Map>) templates.get("elements");
            final List<Map> dbaasElements = allElements.stream().filter(element -> ((String) element.get("type")).equalsIgnoreCase("dbaas")).collect(toList());

            List<Map<String, String>> templatesList = Lists.newArrayList();

            for (Map<String, String> template : dbaasElements) {
                templatesList.add(ImmutableMap.of(
                        "uri", template.get("uri"),
                        "description", template.get("description"),
                        "name", template.get("name").toLowerCase(),
                        "zoneuri", zoneURI));
            }

            return templatesList;
        } catch (Exception e) {
            throw new RuntimeException("Unable to get database templates for zone with URI " + zoneURI, e);
        }
    }

    public List<String> getOEMZonesFor(final String environmentClass, final String zoneName) {
    	WebTarget request = createRequest("/em/cloud");

        try {
            final Map response = request.request().get(Map.class);
            final Map zones = (Map) response.get("zones");
            final List<Map<String, String>> allZones = (List<Map<String, String>>) zones.get("elements");
            allZones.stream().forEach(z ->log.info(z.get("name")));
            return allZones.stream()
                    .filter(zone -> zone.get("service_family_type").equalsIgnoreCase("dbaas"))
                    .filter(zoneNameMathing(environmentClass, zoneName))
                    .map(oemZone -> oemZone.get("uri"))
                    .collect(toList());
        } catch (Exception e) {
            throw new RuntimeException("Unable to get zone URI", e);
        }
    }

    private Predicate<Map<String, String>> zoneNameMathing(String environmentClass, String zoneName) {
        return zone -> {
            final String oemZoneName = zone.get("name").toLowerCase();
            return oemZoneName.startsWith(environmentClass.toLowerCase()) && oemZoneName.endsWith(zoneName.toLowerCase());
        };
    }

    private WebTarget createRequest(String path) {
    	if ( !validator.isValid(path)) {
    		throw new InternalException();
    	}
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(oemUrl +path);
        target.property("Authorization", "Basic " + base64EncodeString(username + ":" + password));
        
        log.info("Created OEM request " + oemUrl + path);
        return target;
    }

    private static String base64EncodeString(String string) {
        return new String(Base64.getEncoder().encode(string.getBytes(StandardCharsets.UTF_8)));
    }

    public Map getOrderStatus(String orderURI) {
        log.debug("Getting status for order with URI {}", orderURI);
        final WebTarget request = createRequest(orderURI);

        try {
            return request.request().get(Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if order with URI " + orderURI + " is finished", e);
        }
    }

}