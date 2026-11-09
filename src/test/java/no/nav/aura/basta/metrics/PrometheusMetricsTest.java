package no.nav.aura.basta.metrics;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import no.nav.aura.basta.ApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrometheusMetricsTest extends ApplicationTest {

    @LocalServerPort
    private int localServerPort;

    @Test
    public void metrics_endpoint() throws Exception {
        Gauge gauge = Gauge.builder()
                .name("basta_test_value")
                .help("Test metric for the servlet exporter")
                .register();
        try {
            gauge.set(42);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + localServerPort + "/metrics"))
                    .header("Accept", "text/plain; version=0.0.4")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElseThrow().startsWith("text/plain"));
            assertTrue(response.body().contains("# TYPE basta_test_value gauge"));
            assertTrue(response.body().contains("basta_test_value 42.0"));
        } finally {
            PrometheusRegistry.defaultRegistry.unregister(gauge);
        }
    }
}
