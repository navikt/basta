package no.nav.aura.basta.rest.api;

import com.jayway.restassured.http.ContentType;
import no.nav.aura.basta.ApplicationTest;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;

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
                .body(Matchers.containsString("maximum allowed: 20"))
                .when()
                .post("/rest/orders/serviceuser/customcredential");
    }
}
