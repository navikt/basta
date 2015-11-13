package no.nav.aura.basta.rest.database;

import static no.nav.aura.basta.domain.input.database.DBOrderInput.APPLICATION_NAME;
import static no.nav.aura.basta.domain.input.database.DBOrderInput.ENVIRONMENT_NAME;
import static no.nav.aura.basta.util.JsonHelper.prettifyJson;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
        final EnvironmentClass environmentClass = EnvironmentClass.valueOf(request.get("environmentClass"));
        Guard.checkAccessToEnvironmentClass(environmentClass);

        final DBOrderInput inputs = new DBOrderInput(request);

        final String applicationName = inputs.get(APPLICATION_NAME);
        final String environmentName = inputs.get(ENVIRONMENT_NAME);
        final String dbName = createDBName(applicationName, environmentName);
        final String password = RandomStringGenerator.generate(12);
        final String creationStatusUri = oracleClient.createDatabase(dbName, password);

        Order dbOrder = new Order(OrderType.DB, OrderOperation.CREATE, request);

        final DBOrderResult results = dbOrder.getResultAs(DBOrderResult.class);
        results.put(DBOrderResult.USERNAME, dbName);
        results.put(DBOrderResult.PASSWORD, password);
        results.put(DBOrderResult.OEM_STATUS_URI, creationStatusUri);
        results.put(DBOrderResult.FASIT_ALIAS, applicationName + "DB");

        dbOrder.setStatus(OrderStatus.WAITING);
        final String payloadApproximation = OracleClient.createPayload(dbName, "*****", "templateURI");
        dbOrder.setExternalRequest(prettifyJson(payloadApproximation)); // TODO: remove if ace is replaced
        dbOrder.setExternalId(creationStatusUri);
        dbOrder.addStatusLog(new OrderStatusLog("Basta", "Creation request sent to Oracle EM, waiting for completion.", "provision:initiated"));

        dbOrder = orderRepository.save(dbOrder);

        log.info("Creating new oracle order {} with input {}", dbOrder.getId(), request);

        return Response.ok(createResponseWithId(dbOrder.getId())).build();
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
