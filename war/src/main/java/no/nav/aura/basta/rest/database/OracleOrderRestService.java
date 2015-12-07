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
import javax.ws.rs.core.Response;

import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
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

        final String creationStatusUri = oracleClient.createDatabase(dbName, password);

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

        order = orderRepository.save(order);

        log.info("Done creating Oracle DB order (id = {})", order.getId());

        return Response.ok(createResponseWithId(order.getId())).build();
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
            final String responseUri = oracleClient.deleteDatabase(databaseName);
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
        } else {
            log.debug("The database name specified {} doesn't exist", databaseName);
            return Response.status(BAD_REQUEST).entity("The database name specified " + databaseName + " doesn't exist").build();
        }

        return Response.ok().entity(databaseName).build();
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
