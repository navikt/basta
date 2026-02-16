package no.nav.aura.basta.spring;

import static io.restassured.RestAssured.given;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import no.nav.aura.basta.ApplicationTest;


public class SpringSecurityTest  extends ApplicationTest {
	@Test
    public void testGetRestWithAuthenticatedUser_shouldReturnOk() {
        given()
        	.auth().preemptive().basic("user", "user")
			.expect()
			.statusCode(200)
			.log().ifError()
			.when()
			.get("/rest/users/current");
    }

    @Test
    public void testGetRestWithoutUser_shouldReturnOk() {
        given()
	    	.auth().preemptive().basic("user", "user")
			.expect()
			.statusCode(200)
			.log().ifError()
			.when()
			.get("/rest/users/current");
    }

    @Test
    public void testGetRestWithUnauthorizedUser_shouldReturnUnauthorized() {
        given()
	    	.auth().preemptive().basic("test", "test")
			.expect()
			.statusCode(401)
			.log().ifError()
			.when()
			.get("/rest/users/current");
    }

    @Test
    public void testGetRolesWithoutUser_shouldReturnRoleAnonymous() {
        given()
			.expect()
			.statusCode(200)
			.body(Matchers.containsString("\"roles\":[\"ROLE_ANONYMOUS\"]"))
			.log().ifError()
			.when()
			.get("/rest/users/current");
    }

    @Test
    public void testGetRolesWithUser_shouldReturnRoleUser() {
        given()
        	.auth().preemptive().basic("user", "user")
			.expect()
			.statusCode(200)
			.body(Matchers.containsString("\"roles\":[\"ROLE_USER\"]"))
			.log().ifError()
			.when()
			.get("/rest/users/current");
    }

    @Test
    public void testGetRolesWithProdadmin_shouldReturnMultipleRoles() {
        given()
	    	.auth().preemptive().basic("prodadmin", "prodadmin")
			.expect()
			.statusCode(200)
			.body(Matchers.containsString("\"roles\":[\"ROLE_USER\",\"ROLE_OPERATIONS\",\"ROLE_PROD_OPERATIONS\"]"))
			.log().ifError()
			.when()
			.get("/rest/users/current");
    }

    @Test
    public void testGetRolesWithSuperuser_shouldReturnMultipleRoles() {
        
        given()
	    	.auth().preemptive().basic("superuser", "superuser")
			.expect()
			.statusCode(200)
			.body(Matchers.containsString("\"roles\":[\"ROLE_SUPERUSER\",\"ROLE_USER\",\"ROLE_OPERATIONS\",\"ROLE_PROD_OPERATIONS\"]"))
			.log().ifError()
			.when()
			.get("/rest/users/current");
    }
}
