package no.nav.aura.basta.rest.api;

import no.nav.aura.basta.ApplicationTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;

public class CustomUserCredentialRestServiceTest extends ApplicationTest {

    @Test
    public void createServiceUserWithCustomUsernameHappyPath() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"environmentClass\": \"t\","
                        + "\"username\": \"srvusername\","
                        + "\"zone\": \"fss\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(201)
                .when()
                .post("/rest/orders/serviceuser/customcredential");
    }

    @Test
    public void customUsernameWithMissingRequiredPayloadParametersWillFail() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{}")
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(400)
                .body(Matchers.containsString("object has missing required properties ([\"environmentClass\",\"username\",\"zone\"])"))
                .when()
                .post("/rest/orders/serviceuser/customcredential");
    }

    @Test
    public void customUsernameThatDoesNotStartWithSrvWilFail() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"environmentClass\": \"t\","
                        + "\"username\": \"customusername\","
                        + "\"zone\": \"fss\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(400)
                .body(Matchers.containsString("Input did not pass validation"))
                .when()
                .post("/rest/orders/serviceuser/customcredential");
    }

    @Test
    public void customUsernameThatIsMoreThen20CharactersLongWilFail() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"environmentClass\": \"t\","
                        + "\"username\": \"usernamelongerthantwentycharacters\","
                        + "\"zone\": \"fss\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(400)
                .body(Matchers.containsString("must be at most 20 characters long"))
                .when()
                .post("/rest/orders/serviceuser/customcredential");
    }
}
