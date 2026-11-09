package no.nav.aura.basta.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;

class RestClientTest {

    private static final String URL = "https://fasit.example.test/resources/1";
    private static final String RESPONSE = """
            {"id":1,"alias":"database","created":"2026-11-06T14:00:00.123","newField":true}
            """;

    private RestClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        client = new RestClient("user", "pass");
        server = MockRestServiceServer.bindTo(client.getRestTemplate()).build();
    }

    @Test
    void readsPlainTextJsonOnGet() {
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(RESPONSE, MediaType.TEXT_PLAIN));

        ResourcePayload resource = client.get(URL, ResourcePayload.class).orElseThrow();

        assertResource(resource);
        server.verify();
    }

    @Test
    void readsPlainTextJsonOnUpdate() {
        assertUpdateResponse(MediaType.TEXT_PLAIN);
    }

    @Test
    void readsJsonOnUpdate() {
        assertUpdateResponse(MediaType.APPLICATION_JSON);
    }

    private void assertUpdateResponse(MediaType contentType) {
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(RESPONSE, contentType));

        ResourcePayload resource = client.updateFasitResourceAndReturnResourcePayload(
                URL, "{}", null, null);

        assertResource(resource);
        server.verify();
    }

    private void assertResource(ResourcePayload resource) {
        assertEquals(1L, resource.id);
        assertEquals("database", resource.alias);
        assertEquals(LocalDateTime.of(2026, 11, 6, 14, 0, 0, 123_000_000), resource.created);
    }
}
