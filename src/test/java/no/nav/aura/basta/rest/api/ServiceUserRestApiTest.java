package no.nav.aura.basta.rest.api;

import static com.jayway.restassured.RestAssured.given;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import no.nav.aura.basta.JettyTest;

public class ServiceUserRestApiTest extends JettyTest {

	@Before
    public void setup() {
		RestAssured.port = jetty.getPort();
    }

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
                .body(Matchers.containsString("No enum constant no.nav.aura.basta.domain.input.Zone.tullogtoys"))
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
