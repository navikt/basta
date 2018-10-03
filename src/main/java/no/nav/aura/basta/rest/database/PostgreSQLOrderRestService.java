package no.nav.aura.basta.rest.database;

import com.google.gson.JsonObject;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.PostgreSQLClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.domain.result.database.DBOrderResult.*;

@Component
@Path("/v1/postgresql")
public class PostgreSQLOrderRestService {
    private final OrderRepository orderRepository;
    private final FasitUpdateService fasitUpdateService;
    private final PostgreSQLClient client;

    @Inject
    public PostgreSQLOrderRestService(
            OrderRepository orderRepository,
            FasitUpdateService fasitUpdateService,
            PostgreSQLClient client
    ) {
        this.orderRepository = orderRepository;
        this.fasitUpdateService = fasitUpdateService;
        this.client = client;
    }

    @POST
    @Consumes("application/json")
    public Response createPostgreSQLDB(Map<String, String> request) {
        try {
            final String environmentClass = request.get(ENVIRONMENT_CLASS);
            final String dbName = request.get(DATABASE_NAME);
            final String zone = request.get(ZONE);
            final String fasitAlias = request.get(FASIT_ALIAS);

            Guard.checkAccessToEnvironmentClass(EnvironmentClass.valueOf(environmentClass));

            PostgreSQLClient.CreateDBResponse response = client.createDatabase(dbName, environmentClass, zone);

            Order order = new Order(OrderType.OracleDB, OrderOperation.CREATE, request);
            order.setStatus(OrderStatus.SUCCESS);
            order.addStatuslogInfo("Database created.");
            order.setResults(mapWithMaskedPassword(response));

            order = orderRepository.save(order);

            ResourceElement fasitResource = new ResourceElement(ResourceTypeDO.DataSource, fasitAlias);
            fasitResource.addProperty(new PropertyElement("hosts", response.server));
            fasitResource.addProperty(new PropertyElement("username", response.username));
            fasitResource.addProperty(new PropertyElement("password", response.password));
            fasitResource.setEnvironmentName(request.get(ENVIRONMENT_NAME));
            fasitResource.setEnvironmentClass(environmentClass);
            fasitResource.setApplication(request.get(APPLICATION_NAME));

            fasitUpdateService.createResource(fasitResource, order);

            return Response.ok("{\"id\": " + order.getId() + "}").build();
        } catch (RuntimeException e) {
            JsonObject json = new JsonObject();
            json.addProperty("message", e.getMessage());
            return Response.serverError().entity(json.toString()).build();
        }
    }

    private Map<String, String> mapWithMaskedPassword(PostgreSQLClient.CreateDBResponse response) {
        Map<String, String> result = new HashMap<>();
        result.put("db_name", response.db_name);
        result.put("version", response.version);
        result.put("username", response.username);
        result.put("password", "******");
        result.put("server", response.server);
        return result;
    }
}
