package no.nav.aura.basta.rest.api;

import com.jayway.restassured.http.ContentType;
import no.nav.aura.basta.ApplicationTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static com.jayway.restassured.RestAssured.given;

public class AdGroupRestServiceTest extends ApplicationTest {

    @Test
    public void createGroupHappyPath() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"environmentClass\": \"t\","
                        + "\"application\": \"test\","
                        + "\"groupUsage\": \"MQ\","
                        + "\"zone\": \"fss\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(201)
                .when()
                .post("/rest/orders/adgroups");
    }

    @Test
    public void GroupWithMissingRequiredPayloadParametersWillFail() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{}")
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(400)
                .body(Matchers.containsString("object has missing required properties ([\"application\",\"environmentClass\",\"groupUsage\",\"zone\"])"))
                .when()
                .post("/rest/orders/adgroups");
    }
}
