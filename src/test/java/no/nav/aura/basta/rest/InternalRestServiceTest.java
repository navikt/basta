package no.nav.aura.basta.rest;

import no.nav.aura.basta.ApplicationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

/**
 * Created by R137915 on 10/11/17.
 */
public class InternalRestServiceTest extends ApplicationTest {

    @Test
    public void getIsAliveShouldReturnOk() {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/rest/internal/isAlive", String
                .class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getCurrentUserShouldReturnOk() {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/rest/users/current", String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void userShouldBeAnonymous() {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/rest/users/current", String.class);
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).contains("\"username\":\"anonymousUser\""));
    }
}
