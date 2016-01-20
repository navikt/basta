package no.nav.aura.basta.rest.database;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static no.nav.aura.basta.backend.OracleClient.NONEXISTENT;
import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.WAITING;
import static no.nav.aura.basta.domain.result.database.DBOrderResult.*;
import static no.nav.aura.basta.util.JsonHelper.prettifyJson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.database.DBOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.JsonHelper;
import no.nav.aura.basta.util.RandomStringGenerator;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.base.Joiner;

@Component
@Path("/v1/oracledb")
public class OracleOrderRestService {

    private static final Logger log = LoggerFactory.getLogger(OracleOrderRestService.class);
    public static final String CREATE_ORACLE_DB_JSONSCHEMA = "/createOracleDBSchema.json";
    private OrderRepository orderRepository;
    private OracleClient oracleClient;
    private FasitUpdateService fasitClient;

    @Inject
    public OracleOrderRestService(OrderRepository orderRepository, OracleClient oracleClient, FasitUpdateService fasitClient) {
        this.orderRepository = orderRepository;
        this.oracleClient = oracleClient;
        this.fasitClient = fasitClient;
    }

    @POST
    @Consumes("application/json")
    public Response createOracleDB(Map<String, String> request) {
        log.debug("Got request with payload {}", request);

        validateRequest(CREATE_ORACLE_DB_JSONSCHEMA, request);

        final DBOrderInput inputs = new DBOrderInput(request);

        final String applicationName = inputs.get(APPLICATION_NAME);
        final String environmentName = inputs.get(ENVIRONMENT_NAME);
        final String environmentClass = inputs.get(ENVIRONMENT_CLASS);
        final String templateURI = inputs.get(TEMPLATE_URI);
        final String zone = inputs.get(ZONE);

        Guard.checkAccessToEnvironmentClass(EnvironmentClass.valueOf(environmentClass));

        final String oemZone = getOEMZoneNameFrom(environmentClass, zone);

        verifyOEMZoneExists(oemZone);
        verifyOEMZoneHasTemplate(oemZone, templateURI);

        final String dbName = createDBName(applicationName, environmentName);
        final String password = RandomStringGenerator.generate(12);

        String creationStatusUri;
        try {
            creationStatusUri = oracleClient.createDatabase(dbName, password, oemZone, templateURI);
        } catch (RuntimeException e) {
            return Response.serverError().entity("{\"message\": \"" + e.getMessage() + "\"}").build();
        }

        Order order = new Order(OrderType.DB, OrderOperation.CREATE, request);

        final DBOrderResult results = order.getResultAs(DBOrderResult.class);
        results.put(USERNAME, dbName);
        results.put(PASSWORD, password);
        results.put(OEM_ENDPOINT, creationStatusUri);
        results.put(FASIT_ALIAS, applicationName + "DB");

        order.setStatus(WAITING);
        final String payloadApproximation = oracleClient.createPayload(dbName, "*****", templateURI);
        order.setExternalRequest(prettifyJson(payloadApproximation)); // TODO: remove if ace is replaced
                                                                      // (http://jira.adeo.no/browse/AURA-1577)
        order.setExternalId(creationStatusUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Creation request sent to Oracle EM, waiting for completion.", "provision:initiated"));

        Order savedOrder = orderRepository.save(order);

        log.info("Done creating Oracle DB order (id = {})", savedOrder.getId());

        return Response.ok(createResponseWithId(savedOrder.getId())).build();
    }

    protected void verifyOEMZoneHasTemplate(String oemZone, String templateURI) {
        final List<Map<String, String>> zoneTemplates = oracleClient.getTemplatesForZone(oemZone);
        for (Map<String, String> zoneTemplateURI : zoneTemplates) {
            if (zoneTemplateURI.get("uri").equalsIgnoreCase(templateURI)) {
                return;
            }
        }

        throw new BadRequestException("Provided templateURI " + templateURI + " was not found in OEM zone " + oemZone + ". Valid templateURIs are\n"
                + Joiner.on("\n").join(oracleClient.getTemplatesForZone(oemZone)));
    }

    @DELETE
    @Path("/{fasitId}")
    @Consumes("application/json")
    public Response deleteOracleDB(@PathParam("fasitId") String fasitId) {
        log.debug("Got deletion request for Oracle DB with fasitId {}", fasitId);

        final String oemEndpoint = getOEMEndpointFromFasit(fasitId);

        verifyExists(oemEndpoint);

        log.debug("Database with URI {} exists in OEM", oemEndpoint);

        String responseUri;
        try {
            responseUri = oracleClient.deleteDatabase(oemEndpoint);
        } catch (RuntimeException e) {
            return Response.serverError().entity("{\"message\": \"" + e.getMessage() + "\"}").build();
        }

        log.debug("Request sent to OEM, got response URI {}", responseUri);
        Order order = new Order(OrderType.DB, OrderOperation.DELETE, new HashMap<>());
        order.setStatus(WAITING);

        final DBOrderResult results = order.getResultAs(DBOrderResult.class);
        results.put(USERNAME, fasitId);
        results.put(OEM_ENDPOINT, responseUri);
        results.put(FASIT_ID, fasitId);
        results.put(NODE_STATUS, "DECOMMISSIONED");

        order.setExternalId(responseUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Deletion request sent to Oracle EM, waiting for completion.", "deletion:initiated"));
        final Order savedOrder = orderRepository.save(order);
        log.debug("Done creating Oracle DB deletion order (id = {})", savedOrder.getId());
        return Response.ok(createResponseWithId(savedOrder.getId())).build();
    }

    @PUT
    @Path("/{fasitId}/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stop(@PathParam("fasitId") String fasitId) {
        log.debug("Got stop request for Oracle DB with fasitId {}", fasitId);

        final String oemEndpoint = getOEMEndpointFromFasit(fasitId);
        verifyExists(oemEndpoint);

        final Order order = new Order(OrderType.DB, OrderOperation.STOP, new HashMap<>());

        String responseUri;
        try {
            responseUri = oracleClient.stopDatabase(oemEndpoint);
        } catch (RuntimeException e) {
            return Response.serverError().entity("{\"message\": \"" + e.getMessage() + "\"}").build();
        }

        log.debug("Request sent to OEM, got response URI {}", responseUri);

        order.setExternalId(responseUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Stop request sent to Oracle EM. Check status on URL " + responseUri, "stop:completed"));
        order.setStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);

        return Response.ok(responseUri).build();
    }

    @PUT
    @Path("/{fasitId}/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(@PathParam("fasitId") String fasitId) {
        log.debug("Got start request for Oracle DB with fasitId {}", fasitId);

        final String oemEndpoint = getOEMEndpointFromFasit(fasitId);
        verifyExists(oemEndpoint);

        final Order order = new Order(OrderType.DB, OrderOperation.START, new HashMap<>());

        String responseUri;
        try {
            responseUri = oracleClient.startDatabase(oemEndpoint);
        } catch (RuntimeException e) {
            return Response.serverError().entity("{\"message\": \"" + e.getMessage() + "\"}").build();
        }

        log.debug("Request sent to OEM, got response URI {}", responseUri);

        order.setExternalId(responseUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Start request sent to Oracle EM. Check status on URL " + responseUri, "start:completed"));
        order.setStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);

        return Response.ok(responseUri).build();
    }

    @GET
    @Path("/{fasitId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status(@PathParam("fasitId") String fasitId) {
        log.debug("Got status request for Oracle DB with fasitId {}", fasitId);
        final String oemEndpoint = getOEMEndpointFromFasit(fasitId);

        verifyExists(oemEndpoint);

        return Response.ok(oracleClient.getStatus(oemEndpoint)).build();
    }

    @GET
    @Path("/templates")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTemplates(@QueryParam("environmentClass") String environmentClass, @QueryParam("zone") String zone) {
        if (isEmpty(environmentClass)) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'environmentClass' cannot be empty").build();
        }
        if (isEmpty(zone)) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'zone' cannot be empty").build();
        }

        environmentClass = environmentClass.toLowerCase();
        zone = zone.toLowerCase();

        if (!newArrayList("u", "t", "q", "p").contains(environmentClass)) {
            return Response.status(NOT_FOUND).entity("Unknown environment class: " + environmentClass + ". Unable to provide correct OEM zone").build();
        }

        if (!newArrayList("sbs", "fss").contains(zone)) {
            return Response.status(NOT_FOUND).entity("Unknown zone: " + zone + ". Unable to provide correct OEM zone").build();
        }

        String oemZoneName = getOEMZoneNameFrom(environmentClass, zone);

        verifyOEMZoneExists(oemZoneName);

        final List<Map<String, String>> templatesForZone = oracleClient.getTemplatesForZone(oemZoneName);

        return Response.ok().entity(templatesForZone).build();
    }

    protected String getOEMEndpointFromFasit(String fasitId) {
        if (!parsableAsLong(fasitId)) {
            throw new BadRequestException("Provided fasitId " + fasitId + " is not a valid long");
        }

        final ResourceElement resource = fasitClient.getResource(Long.parseLong(fasitId));
        if (resource == null) {
            throw new NotFoundException("Unable to find Fasit resource with id " + fasitId);
        }

        if (resource.getType() != ResourceTypeDO.DataSource) {
            throw new BadRequestException("Resource with fasitId " + fasitId + " is not of type 'DataSource'");
        }

        final String oemEndpoint = getPropertyValue("oemEndpoint", resource);

        if (isEmpty(oemEndpoint)) {
            throw new BadRequestException("Resource with fasitId " + fasitId + " does not contain the property 'oemEndpoint'");
        } else {
            log.debug("Found oemEndpoint {} for Fasit resource with id {}", oemEndpoint, fasitId);
            return oemEndpoint;
        }
    }

    private static String getPropertyValue(String propertyKey, ResourceElement resource) {
        final Set<PropertyElement> properties = resource.getProperties();
        for (PropertyElement property : properties) {
            if (property.getName().equalsIgnoreCase(propertyKey)) {
                return property.getValue();
            }
        }
        return null;
    }

    protected static void validateRequest(String jsonSchema, Map<String, ?> request) {
        final ProcessingReport validation;

        try {
            validation = JsonHelper.validate(jsonSchema, request);
        } catch (RuntimeException e) {
            log.error("Unable to validate request: " + request + " against schema " + jsonSchema, e);
            throw new InternalServerErrorException("Unable to validate request");
        }

        if (!validation.isSuccess()) {
            throw new BadRequestException("Input did not pass validation. " + validation.toString());
        }
    }

    private void verifyExists(String oemEndpoint) {
        try {
            if (oracleClient.getStatus(oemEndpoint).equals(NONEXISTENT)) {
                log.debug("Unable to find any matching DB instances for URI {})", oemEndpoint);
                throw new NotFoundException("Unable to find any matching DB instances for URI " + oemEndpoint);
            }
        } catch (RuntimeException e) {
            throw new InternalServerErrorException("Unable to check if database with URI " + oemEndpoint + " exists in OEM");
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

    // assumes valid envclass and zone
    protected static String getOEMZoneNameFrom(String environmentClass, String zone) {
        if (environmentClass.equalsIgnoreCase("u") || environmentClass.equalsIgnoreCase("t")) {
            return "u_t_" + zone;
        } else {
            return environmentClass + "_" + zone;
        }
    }

    private String createResponseWithId(Long id) {
        return "{\"id\": " + id + "}";
    }

    protected static String createDBName(String application, String environment) {
        String name = environment + "_" + application;
        return trimToLength(removeIllegalCharacters(name), 28);
    }

    private static String removeIllegalCharacters(String string) {
        return string.replaceAll("[^A-Za-z0-9_]", "");
    }

    private static String trimToLength(String string, int length) {
        if (string.length() <= length) {
            return string;
        } else {
            return string.substring(0, length);
        }
    }

    private static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    protected void verifyOEMZoneExists(String oemZone) {
        try {
            oracleClient.getZoneURIFrom(oemZone);
        } catch (RuntimeException e) {
            throw new NotFoundException("Unable to find a zone in OEM with name " + oemZone);
        }
    }
}
