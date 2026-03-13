package no.nav.aura.basta.rest.database;

import com.google.common.base.Joiner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationMessage;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.result.database.DBOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.StringHelper;
import no.nav.aura.basta.util.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.inject.Inject;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.WAITING;
import static no.nav.aura.basta.domain.result.database.DBOrderResult.*;
import static no.nav.aura.basta.util.StringHelper.isEmpty;

@Component
@RestController
@RequestMapping("/rest/v1/oracledb")
@Transactional
public class OracleOrderRestService {

    private static final Logger log = LoggerFactory.getLogger(OracleOrderRestService.class);
    public static final String CREATE_ORACLE_DB_JSONSCHEMA = "/createOracleDBSchema.json";
    private OrderRepository orderRepository;
    private OracleClient oracleClient;
    private FasitUpdateService fasitClient;

    public OracleOrderRestService() {}

    @Inject
    public OracleOrderRestService(OrderRepository orderRepository, OracleClient oracleClient, FasitUpdateService fasitClient) {
        this.orderRepository = orderRepository;
        this.oracleClient = oracleClient;
        this.fasitClient = fasitClient;
    }

    @PostMapping
    public ResponseEntity<?> createOracleDB(@RequestBody Map<String, String> request) {
        log.debug("Got request with payload {}", request);

        validateRequest(CREATE_ORACLE_DB_JSONSCHEMA, request);

        final DBOrderInput inputs = new DBOrderInput(request);

        final String environmentClass = inputs.get(ENVIRONMENT_CLASS);
        final String dbName = inputs.get(DATABASE_NAME);
        final String templateURI = inputs.get(TEMPLATE_URI);
        final String zoneURI = inputs.get(ZONE_URI);
        final String fasitAlias = inputs.get(FASIT_ALIAS);

        Guard.checkAccessToEnvironmentClass(EnvironmentClass.valueOf(environmentClass));
        verifyOEMZoneHasTemplate(zoneURI, templateURI);

        final String password = StringHelper.generateRandom(12);
        String creationStatusUri;

        try {
            creationStatusUri = oracleClient.createDatabase(dbName, password, zoneURI, templateURI);
        } catch (RuntimeException e) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();
            json.put("message", e.getMessage());
            try {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapper.writeValueAsString(json));
            } catch (Exception ex) {
                throw new RuntimeException("Failed to serialize error response", ex);
            }
        }

        Order order = new Order(OrderType.OracleDB, OrderOperation.CREATE, request);

        final DBOrderResult results = order.getResultAs(DBOrderResult.class);
        results.put(USERNAME, dbName);
        results.put(PASSWORD, password);
        results.put(OEM_ENDPOINT, creationStatusUri);
        results.put(FASIT_ALIAS, fasitAlias);

        order.setStatus(WAITING);

        order.setExternalId(creationStatusUri);
        order = orderRepository.save(order.addStatuslogInfo("Creation request sent to Oracle EM, waiting for completion."));

        log.info("Done creating Oracle DB order (id = {})", order.getId());

        return ResponseEntity.ok(createResponseWithId(order.getId()));
    }

    protected void verifyOEMZoneHasTemplate(String zoneURI, String templateURI) {
        final List<Map<String, String>> zoneTemplates = oracleClient.getTemplatesForZone(zoneURI);
        for (Map<String, String> zoneTemplateURI : zoneTemplates) {
            if (zoneTemplateURI.get("uri").equalsIgnoreCase(templateURI)) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Provided templateURI " + templateURI + " was not found in OEM zone " + zoneURI + ". Valid templateURIs are\n"
                + Joiner.on("\n").join(oracleClient.getTemplatesForZone(zoneURI)));
    }

    @GetMapping("/templates")
    public ResponseEntity<?> getTemplates(
            @RequestParam String environmentClass, 
            @RequestParam String zone) {
        if (isEmpty(environmentClass)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Query parameter 'environmentClass' cannot be empty");
        }
        if (isEmpty(zone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Query parameter 'zone' cannot be empty");
        }

        environmentClass = environmentClass.toLowerCase();
        zone = zone.toLowerCase();

        if (!newArrayList("u", "t", "q", "p").contains(environmentClass)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Unknown environment class: " + environmentClass + ". Unable to provide correct OEM zone");
        }

        if (!newArrayList("sbs", "fss", "iapp").contains(zone)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Unknown zone: " + zone + ". Unable to provide correct OEM zone");
        }

        List<Map<String, String>> templatesForZone = new ArrayList<>();
        List<String> oemZonesFor = oracleClient.getOEMZonesFor(environmentClass, zone);

        for(String oemZoneUri : oemZonesFor) {
            log.debug("Looking for templates for zone uri " + oemZoneUri);
            templatesForZone.addAll(oracleClient.getTemplatesForZone(oemZoneUri));
        }
        return ResponseEntity.ok(filterTemplatesForEnvironmentClassInZone(environmentClass, templatesForZone));
    }

    // Due to a limitation in servers on the Oracle-side, u and t environment-class needs to share a OEM zone.
    // To be able to map a PDB to the correct CDB, templates naming convention. (e.g. u_<templatename> -> u)
    private static List<Map<String, String>> filterTemplatesForEnvironmentClassInZone(String environmentClass, List<Map<String, String>> templatesForZone) {
        return templatesForZone
                .stream()
                .filter(template -> template.get("name").startsWith(environmentClass + "_"))
                .collect(toList());
    }

    protected String getOEMEndpointFromFasit(String fasitId) {
        if (!parsableAsLong(fasitId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Provided fasitId " + fasitId + " is not a valid long");
        }

        Optional<ResourcePayload> resource = fasitClient.getResource(Long.parseLong(fasitId));
        if (resource == null || !resource.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Unable to find Fasit resource with id " + fasitId);
        }

        if (resource.get().getType() != ResourceType.DataSource) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Resource with fasitId " + fasitId + " is not of type 'DataSource'");
        }

        final String oemEndpoint = getPropertyValue("oemEndpoint", resource.get());

        if (isEmpty(oemEndpoint)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Resource with fasitId " + fasitId + " does not contain the property 'oemEndpoint'");
        } else {
            log.debug("Found oemEndpoint {} for Fasit resource with id {}", oemEndpoint, fasitId);
            return oemEndpoint;
        }
    }

    private static String getPropertyValue(String propertyKey, ResourcePayload resource) {
        final Map<String, String>  properties = resource.getProperties();
        
        for(Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(propertyKey)) {
                return entry.getValue();
            }
        }
        
        return null;
    }

    protected static void validateRequest(String jsonSchema, Map<String, ?> request) {
        final Set<ValidationMessage> validation;

        try {
            validation = ValidationHelper.validate(jsonSchema, request);
        } catch (RuntimeException e) {
            log.error("Unable to validate request: " + request + " against schema " + jsonSchema, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Unable to validate request");
        }

        if (!validation.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Input did not pass validation. " + validation.toString());
        }
    }

    private static boolean parsableAsLong(String string) {
        try {
            Long.parseLong(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String createResponseWithId(Long id) {
        return "{\"id\": " + id + "}";
    }
}
