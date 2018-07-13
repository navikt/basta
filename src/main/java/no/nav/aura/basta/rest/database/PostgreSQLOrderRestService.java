package no.nav.aura.basta.rest.database;

import com.google.gson.JsonObject;
import no.nav.aura.basta.backend.PostgreSQLClient;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.security.Guard;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Map;

import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.domain.result.database.DBOrderResult.*;

@Component
@Path("/v1/postgresql")
public class PostgreSQLOrderRestService {



    private final PostgreSQLClient client;

    @Inject
    public PostgreSQLOrderRestService(PostgreSQLClient client) {
        this.client = client;
    }

    @POST
    @Consumes("application/json")
    public Response createPostgreSQLDB(Map<String, String> request) {
        //log.debug("Got request with payload {}", request);

        final DBOrderInput inputs = new DBOrderInput(request);

        final String environmentClass = inputs.get(ENVIRONMENT_CLASS);
        final String dbName = inputs.get(DATABASE_NAME);
        final String zone = inputs.get(ZONE);
        final String fasitAlias = inputs.get(FASIT_ALIAS);

        Guard.checkAccessToEnvironmentClass(EnvironmentClass.valueOf(environmentClass));

        PostgreSQLClient.CreateDBResponse response;
        try {
            response = client.createDatabase(dbName, environmentClass, zone);
        } catch (RuntimeException e) {
            JsonObject json = new JsonObject();
            json.addProperty("message", e.getMessage());
            return Response.serverError().entity(json.toString()).build();
        }

        return Response.ok(createResponseWithId(123L)).build();
    }

    private String createResponseWithId(Long id) {
        return "{\"id\": " + id + "}";
    }
}
