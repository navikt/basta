package no.nav.aura.basta.rest;

import static io.restassured.RestAssured.given;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import no.nav.aura.basta.ApplicationTest;


/**
 * Created by R137915 on 10/11/17.
 */
public class InternalRestServiceTest extends ApplicationTest {

    @Test
    public void getIsAliveShouldReturnOk() {
        given()
			.expect()
			.statusCode(200)
			.log().ifError()
			.when()
			.get("/rest/internal/isAlive");
    }

    @Test
    public void getCurrentUserShouldReturnOk() {
        given()
			.expect()
			.statusCode(200)
			.log().ifError()
			.when()
			.get("/rest/users/current");
    }

    @Test
    public void userShouldBeUnauthenticated() {
        given()
    			.expect()
				.statusCode(200)
				.body(Matchers.containsString("\"roles\":[\"ROLE_ANONYMOUS\"]"))
				.log().ifError()
				.when()
				.get("/rest/users/current");
    }
}
