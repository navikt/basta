package no.nav.aura.basta.metrics;

import com.jayway.restassured.RestAssured;
import no.nav.aura.basta.JettyTest;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;

 public class PrometheusMetricsTest extends JettyTest {

    @Before
    public void setup() {
        RestAssured.port = jetty.getPort();
    }

    @Test
    public void metrics_endpoint() {
        expect()
                .statusCode(200)
                .when()
                .get("/metrics");
    }

}
