package no.nav.aura.basta.rest;

import no.nav.aura.basta.ApplicationTest;
import org.junit.Test;
import org.springframework.http.*;

public class SpringSecurityTest extends ApplicationTest {


    @Test
    public void getCurrentUserShouldReturnOk() throws Exception {


       /* HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer atoken");

        HttpEntity<RequestEntity> requestEntity = new HttpEntity<>(requestHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                "/rest/orders/1234",
                HttpMethod.GET,
                requestEntity,
                String.class
        );*/

        //System.out.println("Done got " + response.getStatusCode() + " " + response.getBody());
        //assertThat(response.getStatusCode(), is(404));
    }
}
