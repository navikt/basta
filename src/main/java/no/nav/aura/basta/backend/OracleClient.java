package no.nav.aura.basta.backend;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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

public class OracleClient {
    private static final String PLUGGABLEDB_ORACLE_CONTENTTYPE = "application/oracle.com.cloud.common.PluggableDbPlatformInstance+json";
    private static final Logger log = LoggerFactory.getLogger(OracleClient.class);
    public static final String NONEXISTENT = "NONEXISTENT";

    private final String oemUrl;
    private final String username;
    private final String password;
    private final Client client;

    public OracleClient(String oemUrl, String username, String password) {
        this.oemUrl = oemUrl;
        this.username = username;
        this.password = password;
        this.client = ClientBuilder.newClient();
    }

    public String createDatabase(String dbName, String dbPassword, String zoneURI, String templateURI) {
        log.debug("Creating database with name {} in zone {}", dbName, zoneURI);
        WebTarget target = client.target(oemUrl).path(zoneURI);
        String payload = createPayload(dbName, dbPassword, templateURI);
        
        try {
        	log.debug("Sending HTTP POST to OEM with payload {}", payload.replace(password, "*****"));
            Response response = target.request(PLUGGABLEDB_ORACLE_CONTENTTYPE)
                    .header("Authorization", "Basic " + base64EncodeString(username + ":" + password))
                    .post(Entity.entity(payload, PLUGGABLEDB_ORACLE_CONTENTTYPE));
            

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                Map<String, Object> errorResponse = response.readEntity(Map.class);
                log.error("Unable to create database {}. {}", dbName, errorResponse);
                throw new RuntimeException("Unable to create database " + dbName + ". " + errorResponse);
            }

            Map<String, Object> successResponse = response.readEntity(Map.class);
            String uri = (String) successResponse.get("uri");

            log.debug("Successfully sent database creation order to OEM, got URI {}", uri);
            return uri;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
        finally {
			client.close();
		}
    }

    public String getStatus(String dbURI) {
    	WebTarget target = client.target(dbURI);
    	try {
			Response response = target.request().get();
			if (response.getStatus() != Response.Status.OK.getStatusCode()) {
				log.debug("Unable to get status from provided database URI {}, assuming it doesn't exist", dbURI);
				return NONEXISTENT;
			}
            final Map<String, Object> successResponse = response.readEntity(Map.class);
            final String status = (String) successResponse.get("status");
            log.debug("Got database status {} for DB with URI {}", status, dbURI);
            return status;
		} finally {
			// TODO: handle finally clause
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
    	WebTarget target = client.target(templateURI);

        try {
        	Response response = target.request().get();
        	
            final Map<String, Object> successResponse = response.readEntity(Map.class);
            final List<Map> workloads = (List<Map>) successResponse.get("workloads");

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
        finally {
			client.close();
		}
    }

    public List<Map<String, String>> getTemplatesForZone(String zoneURI) {
    	WebTarget target = client.target(zoneURI);

        try {
        	Response response = target.request().get();
            final Map<String, Object> zoneInfo = response.readEntity(Map.class);
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
        } finally {
			client.close();
		}
    }

    public List<String> getOEMZonesFor(final String environmentClass, final String zoneName) {
    	WebTarget target = client.target("/em/cloud");


        try {
        	Response response = target.request().get();
            final Map<String, Object> successResponse = response.readEntity(Map.class);

            final Map zones = (Map) successResponse.get("zones");
            final List<Map<String, String>> allZones = (List<Map<String, String>>) zones.get("elements");
            allZones.stream().forEach(z ->log.info(z.get("name")));
            return allZones.stream()
                    .filter(zone -> zone.get("service_family_type").equalsIgnoreCase("dbaas"))
                    .filter(zoneNameMathing(environmentClass, zoneName))
                    .map(oemZone -> oemZone.get("uri"))
                    .collect(toList());
        } catch (Exception e) {
            throw new RuntimeException("Unable to get zone URI", e);
        } finally {
			client.close();
		}
    }

    private Predicate<Map<String, String>> zoneNameMathing(String environmentClass, String zoneName) {
        return zone -> {
            final String oemZoneName = zone.get("name").toLowerCase();
            return oemZoneName.startsWith(environmentClass.toLowerCase()) && oemZoneName.endsWith(zoneName.toLowerCase());
        };
    }

    private static String base64EncodeString(String string) {
        return new String(Base64.getEncoder().encode(string.getBytes(StandardCharsets.UTF_8)));
    }

    public Map getOrderStatus(String orderURI) {
        log.debug("Getting status for order with URI {}", orderURI);
    	WebTarget target = client.target(orderURI);

        try {
        	Response response = target.request().get();
            final Map<String, Object> successResponse = response.readEntity(Map.class);
            return successResponse;
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if order with URI " + orderURI + " is finished", e);
        } finally {
			client.close();
		}
    }

}