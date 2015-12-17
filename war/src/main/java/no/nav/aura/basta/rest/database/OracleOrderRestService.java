package no.nav.aura.basta.rest.database;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static no.nav.aura.basta.domain.input.database.DBOrderInput.APPLICATION_NAME;
import static no.nav.aura.basta.domain.input.database.DBOrderInput.ENVIRONMENT_NAME;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.WAITING;
import static no.nav.aura.basta.domain.result.database.DBOrderResult.*;
import static no.nav.aura.basta.util.JsonHelper.prettifyJson;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import no.nav.aura.basta.util.RandomStringGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Path("/db/orders/oracle")
public class OracleOrderRestService {

    private static final Logger log = LoggerFactory.getLogger(OracleOrderRestService.class);
    private OrderRepository orderRepository;
    private OracleClient oracleClient;

    @Inject
    public OracleOrderRestService(OrderRepository orderRepository, OracleClient oracleClient) {
        this.orderRepository = orderRepository;
        this.oracleClient = oracleClient;
    }

    @POST
    @Consumes("application/json")
    public Response createOracleDB(Map<String, String> request) {
        log.debug("Got request with payload {}", request);
        final EnvironmentClass environmentClass = EnvironmentClass.valueOf(request.get("environmentClass"));
        Guard.checkAccessToEnvironmentClass(environmentClass);

        final DBOrderInput inputs = new DBOrderInput(request);

        final String applicationName = inputs.get(APPLICATION_NAME);
        final String environmentName = inputs.get(ENVIRONMENT_NAME);
        final String dbName = createDBName(applicationName, environmentName);
        final String password = RandomStringGenerator.generate(12);

        String creationStatusUri;
        try {
            creationStatusUri = oracleClient.createDatabase(dbName, password);
        } catch (RuntimeException e) {
            return Response.serverError().entity("{\"message\": \"" + e.getMessage() + "\"}").build();
        }

        Order order = new Order(OrderType.DB, OrderOperation.CREATE, request);

        final DBOrderResult results = order.getResultAs(DBOrderResult.class);
        results.put(USERNAME, dbName);
        results.put(PASSWORD, password);
        results.put(OEM_STATUS_URI, creationStatusUri);
        results.put(FASIT_ALIAS, applicationName + "DB");

        order.setStatus(WAITING);
        final String payloadApproximation = OracleClient.createPayload(dbName, "*****", "templateURI");
        order.setExternalRequest(prettifyJson(payloadApproximation)); // TODO: remove if ace is replaced
                                                                      // (http://jira.adeo.no/browse/AURA-1577)
        order.setExternalId(creationStatusUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Creation request sent to Oracle EM, waiting for completion.", "provision:initiated"));

        Order savedOrder = orderRepository.save(order);

        log.info("Done creating Oracle DB order (id = {})", savedOrder.getId());

        return Response.ok(createResponseWithId(savedOrder.getId())).build();
    }

    @DELETE
    @Path("/{databaseName}")
    @Consumes("application/json")
    public Response deleteOracleDB(@PathParam("databaseName") String databaseName, Map<String, String> request) {
        log.debug("Got request with databaseName {} and payload {} ", databaseName, request);

        String fasitId = request.get("fasitId");

        if (fasitId == null) {
            return Response.status(BAD_REQUEST).entity("No fasitId was provided with the request").build();
        }

        if (!parsableAsLong(fasitId)) {
            return Response.status(BAD_REQUEST).entity("Provided fasitId " + fasitId + " is not a valid long").build();
        }

        if (oracleClient.exists(databaseName)) {
            log.debug("Database with name {} exists in OEM", databaseName);

            String responseUri;
            try {
                responseUri = oracleClient.deleteDatabase(databaseName);
            } catch (RuntimeException e) {
                return Response.serverError().entity("{\"message\": \"" + e.getMessage() + "\"}").build();
            }

            log.debug("Request sent to OEM, got response URI {}", responseUri);
            Order order = new Order(OrderType.DB, OrderOperation.DELETE, new HashMap<>());
            order.setStatus(WAITING);

            final DBOrderResult results = order.getResultAs(DBOrderResult.class);
            results.put(USERNAME, databaseName);
            results.put(OEM_STATUS_URI, responseUri);
            results.put(FASIT_ID, fasitId);
            results.put(NODE_STATUS, "DECOMMISSIONED");

            order.setExternalId(responseUri);
            order.addStatusLog(new OrderStatusLog("Basta", "Deletion request sent to Oracle EM, waiting for completion.", "deletion:initiated"));
            final Order savedOrder = orderRepository.save(order);
            log.debug("Done creating Oracle DB deletion order (id = {})", savedOrder.getId());
            return Response.ok(createResponseWithId(savedOrder.getId())).build();
        } else {
            log.debug("The database name specified {} doesn't exist", databaseName);
            return Response.status(BAD_REQUEST).entity("The database name specified " + databaseName + " doesn't exist").build();
        }

    }

    @POST
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stop(Map<String, String> request) {
        final String databaseName = request.get("databaseName");

        if (databaseName == null) {
            return Response.status(BAD_REQUEST).entity("No databaseName was provided with the request").build();
        }

        final Order order = new Order(OrderType.DB, OrderOperation.STOP, request);

        String responseUri;
        try {
            responseUri = oracleClient.stopDatabase(databaseName);
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

    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(Map<String, String> request) {
        final String databaseName = request.get("databaseName");

        if (databaseName == null) {
            return Response.status(BAD_REQUEST).entity("No databaseName was provided with the request").build();
        }

        final Order order = new Order(OrderType.DB, OrderOperation.START, request);

        String responseUri;
        try {
            responseUri = oracleClient.startDatabase(databaseName);
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

    @POST
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status(Map<String, String> request) {
        String databaseName = request.get("databaseName");

        if (databaseName == null) {
            return Response.status(BAD_REQUEST).entity("No databaseName was provided with the request").build();
        } else {
            return Response.ok(oracleClient.getStatus(databaseName)).build();
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
}
