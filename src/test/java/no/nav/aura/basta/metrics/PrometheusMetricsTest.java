package no.nav.aura.basta.metrics;

import no.nav.aura.basta.ApplicationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.expect;

public class PrometheusMetricsTest extends ApplicationTest {

    @Test
    public void metrics_endpoint() {
        expect()
                .statusCode(200)
                .when()
                .get("/metrics");
    }
}
