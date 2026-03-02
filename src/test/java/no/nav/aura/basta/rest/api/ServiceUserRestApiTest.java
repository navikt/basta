package no.nav.aura.basta.rest.api;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.restassured.http.ContentType;
import no.nav.aura.basta.ApplicationTest;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceUserRestApiTest extends ApplicationTest {

	@AfterAll
	public void tearDown() {
		orderRepository.deleteAll();
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
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .body("{"
                        + "\"application\": \"app\","
                        + "\"zone\": \"tullogtoys\","
                        + "\"environmentClass\": \"u\""
                        + "}")
                .contentType(ContentType.JSON)
                .expect()
                .log().ifError()
                .statusCode(400)
                .body(Matchers.containsString("No enum constant no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone.tullogtoys"))
                .when()
                .post("/rest/api/orders/serviceuser/stop");
    }

    @Test
    public void stopServiceuserWithApplication() {
        when(fasitRestClient.findFasitResources(eq(ResourceType.Credential), any(), any(ScopePayload.class)))
        	.thenReturn(ResourcesListPayload.emptyResourcesList());

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
    	when(fasitRestClient.findFasitResources(eq(ResourceType.Credential), any(), any(ScopePayload.class)))
    		.thenReturn(ResourcesListPayload.emptyResourcesList());
    	
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
