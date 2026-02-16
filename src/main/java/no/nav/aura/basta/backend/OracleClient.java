package no.nav.aura.basta.backend;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.networknt.org.apache.commons.validator.routines.DomainValidator;

public class OracleClient {
    private static final String PLUGGABLEDB_ORACLE_CONTENTTYPE = "application/oracle.com.cloud.common.PluggableDbPlatformInstance+json";
    private static final Logger log = LoggerFactory.getLogger(OracleClient.class);
    public static final String NONEXISTENT = "NONEXISTENT";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final URI oemUrl;
    private final RestTemplate restTemplate;
    private DomainValidator validator = DomainValidator.getInstance();

    public OracleClient(String oemUrl, String username, String password) throws URISyntaxException {
        this.oemUrl = new URI(oemUrl);
        this.restTemplate = new RestTemplate();
        
     // Configure basic authentication
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, authHeader);
            return execution.execute(request, body);
        });
    }

    public String createDatabase(String dbName, String password, String zoneURI, String templateURI) {
        log.debug("Creating database with name {} in zone {}", dbName, zoneURI);
        final String url = oemUrl + zoneURI;
        final String payload = createPayload(dbName, password, templateURI);

        try {
            log.debug("Sending HTTP POST to OEM with payload {}", payload.replace(password, "*****"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(PLUGGABLEDB_ORACLE_CONTENTTYPE));
            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("Unable to create database {}. {}", dbName, response.getBody());
                throw new RuntimeException("Unable to create database " + dbName + ". " + response.getBody());
            }
            
            final String uri = (String) response.getBody().get("uri");

            log.debug("Successfully sent database creation order to OEM, got URI {}", uri);
            return uri;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getStatus(String dbURI) {
        final String url = oemUrl + dbURI;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            final String status = (String) response.getBody().get("status");

            if (response.getStatusCode() != HttpStatus.OK || status == null) {
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
        try {
            ArrayNode tableSpaces = objectMapper.createArrayNode();
            tableSpaces.add(databaseName.toUpperCase());

            ObjectNode params = objectMapper.createObjectNode();
            params.set("tablespaces", tableSpaces);
            params.put("pdb_name", databaseName);
            params.put("username", databaseName);
            params.put("password", password);
            params.put("workload_name", getWorkloadNameFor(templateURI));
            params.put("service_name", databaseName.replaceAll("[^A-Za-z0-9]", ""));
            params.put("target_name", databaseName);

            ObjectNode json = objectMapper.createObjectNode();
            json.set("params", params);
            json.put("based_on", templateURI);
            json.put("name", databaseName);
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payload", e);
        }
    }

    private String getWorkloadNameFor(String templateURI) {
        final String url = oemUrl + templateURI;
        try {
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            final List<Map> workloads = (List<Map>) response.getBody().get("workloads");

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
        final String url = oemUrl + zoneURI;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            final Map zoneInfo = response.getBody();
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
        final String url = oemUrl + "/em/cloud";

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            final Map zones = (Map) response.getBody().get("zones");
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

    public Map getOrderStatus(String orderURI) {
        log.debug("Getting status for order with URI {}", orderURI);
        final String url = oemUrl + orderURI;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if order with URI " + orderURI + " is finished", e);
        }
    }

}