package no.nav.aura.basta.rest;

import no.nav.aura.basta.ApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Created by R137915 on 10/11/17.
 */
public class InternalRestServiceTest extends ApplicationTest {

    @Test
    public void getIsAliveShouldReturnOk() throws Exception {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/rest/internal/isAlive", String
                .class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getCurrentUserShouldReturnOk() throws Exception {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/rest/users/current", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void userShouldBeAnonymous() throws Exception {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/rest/users/current", String.class);
        assertTrue(response.getBody().contains("\"username\":\"anonymousUser\""));
    }
}
