package no.nav.aura.basta.metrics;

import no.nav.aura.basta.ApplicationTest;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;

public class PrometheusMetricsTest extends ApplicationTest {

    @Test
    public void metrics_endpoint() {
        expect()
                .statusCode(200)
                .when()
                .get("/metrics");
    }
}
