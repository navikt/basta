package no.nav.aura.basta.spring;

import no.nav.aura.basta.ApplicationTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class SpringSecurityTest  extends ApplicationTest {
	@Test
    public void testGetRestWithAuthenticatedUser_shouldReturnOk() {
        ResponseEntity<String> result = testRestTemplate.withBasicAuth("user", "user").getForEntity
                ("/rest/users/current", String.class);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void testGetRestWithoutUser_shouldReturnOk() {
        ResponseEntity<String> result = testRestTemplate.getForEntity
                ("/rest/users/current", String.class);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void testGetRestWithUnauthorizedUser_shouldReturnUnauthorized() {
        ResponseEntity<String> result = testRestTemplate.withBasicAuth("test", "test").getForEntity
                ("/rest/users/current", String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    public void testGetRolesWithoutUser_shouldReturnRoleAnonymous() {
        ResponseEntity<String> result = testRestTemplate.getForEntity
                ("/rest/users/current", String.class);
        Assertions.assertTrue(result.toString().contains("\"roles\":[\"ROLE_ANONYMOUS\"]"));
    }

    @Test
    public void testGetRolesWithUser_shouldReturnRoleUser() {
        ResponseEntity<String> result = testRestTemplate.withBasicAuth("user", "user").getForEntity
                ("/rest/users/current", String.class);
        Assertions.assertTrue(result.toString().contains("\"roles\":[\"ROLE_USER\"]"));
    }

    @Test
    public void testGetRolesWithProdadmin_shouldReturnMultipleRoles() {
        ResponseEntity<String> result = testRestTemplate.withBasicAuth("prodadmin", "prodadmin").getForEntity
                ("/rest/users/current", String.class);
        Assertions.assertTrue(result.toString().contains("\"roles\":[\"ROLE_USER\",\"ROLE_OPERATIONS\"," +
                "\"ROLE_PROD_OPERATIONS\"]"));
    }

    @Test
    public void testGetRolesWithSuperuser_shouldReturnMultipleRoles() {
        ResponseEntity<String> result = testRestTemplate.withBasicAuth("superuser", "superuser").getForEntity
                ("/rest/users/current", String.class);
        Assertions.assertTrue(result.toString().contains("\"roles\":[\"ROLE_SUPERUSER\",\"ROLE_USER\",\"ROLE_OPERATIONS\"," +
                "\"ROLE_PROD_OPERATIONS\"]"));
    }
}
