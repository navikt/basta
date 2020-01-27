package no.nav.aura.basta.rest.api;

import com.jayway.restassured.http.ContentType;
import no.nav.aura.basta.ApplicationTest;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;

public class ServiceUserRestApiTest extends ApplicationTest {

	@Test
    public void stopServiceuserWithMissingInputParamsFail() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"application\": \"app\","
                        + "\"zone\": \"fss\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(400)
                .body(Matchers.containsString("Missing required input"))
                .when()
                .post("/rest/api/orders/serviceuser/stop");
    }
	
	@Test
    public void stopServiceuserWithWrongInputParams() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"application\": \"app\","
                        + "\"zone\": \"tullogtoys\","
                        + "\"environmentClass\": \"u\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .log().ifError()
                .statusCode(400)
                .body(Matchers.containsString("No enum constant no.nav.aura.basta.backend.fasit.payload.Zone.tullogtoys"))
                .when()
                .post("/rest/api/orders/serviceuser/stop");
    }

    @Test
    public void stopServiceuserWithApplication() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"application\": \"app\","
                        + "\"zone\": \"fss\","
                        + "\"environmentClass\": \"u\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .log().ifError()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/api/orders/serviceuser/stop");
    }
    
    @Test
    public void stopServiceuserWithFasitAlias() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"fasitAlias\": \"srvapp\","
                        + "\"zone\": \"fss\","
                        + "\"environmentClass\": \"u\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .log().ifError()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/api/orders/serviceuser/stop");
    }
    
    @Test
    public void deleteServiceuserWithApplication() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"application\": \"app\","
                        + "\"zone\": \"fss\","
                        + "\"environmentClass\": \"u\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .log().ifError()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/api/orders/serviceuser/remove");
    }

}
