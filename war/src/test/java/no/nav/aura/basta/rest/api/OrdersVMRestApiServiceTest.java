package no.nav.aura.basta.rest.api;

import static com.jayway.restassured.RestAssured.given;
import no.nav.aura.basta.JettyTest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.order.VmOrderTestData;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

public class OrdersVMRestApiServiceTest extends JettyTest {

	@Before
    public void setup() {
		RestAssured.port = jetty.getPort();
    }

    @Test
    public void checkDecommisionCallback() {
        Order order = repository.save(VmOrderTestData.newDecommissionOrder("host1.devillo.no"));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<vm><hostName>host1.devillo.no</hostName></vm>")
                .contentType(ContentType.XML)
                .expect()
                .statusCode(204)
                .log().ifError()
                .when()
                .put("/rest/api/orders/vm/{orderId}/decommission", order.getId());
    }

    @Test
    public void checkStartCallback() {
        Order order = repository.save(VmOrderTestData.newStartOrder("host2.devillo.no"));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<operationResponse>"
                        + "<vm><hostName>host2.devillo.no</hostName><result>on</result></vm>"
                        + "<vm><hostName>unknown.devillo.no</hostName><result>error</result></vm>"
                        + "</operationResponse>")
                .contentType(ContentType.XML)
                .expect()
                .log().ifError()
                .statusCode(204)
                .when()
                .put("/rest/api/orders/vm/{orderId}/start", order.getId());
        VMOrderResult result = repository.findOne(order.getId()).getResultAs(VMOrderResult.class);
        Assert.assertThat(result.hostnames(), Matchers.contains("host2.devillo.no"));

    }

    @Test
    public void checkStopCallback() {
        Order order = repository.save(VmOrderTestData.newStopOrder("host3.devillo.no"));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<operationResponse><vm><hostName>host3.devillo.no</hostName><result>off</result></vm></operationResponse>")
                .contentType(ContentType.XML)
                .expect()
                .log().ifError()
                .statusCode(204)
                .when()
                .put("/rest/api/orders/vm/{orderId}/stop", order.getId());

        VMOrderResult result = repository.findOne(order.getId()).getResultAs(VMOrderResult.class);
        Assert.assertThat(result.hostnames(), Matchers.contains("host3.devillo.no"));
    }

    @Test
    public void createStopOrder() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("[\"ehost1.devillo.no\", \"ehost2.devillo.no\"]")
                .contentType(ContentType.JSON)
                .expect()
                .body(Matchers.containsString("orderId"))
                .log().ifError()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/api/orders/vm/stop");
    }

    @Test
    public void createDecommionOrder() {
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("[\"ehost1.devillo.no\", \"ehost2.devillo.no\"]")
                .contentType(ContentType.JSON)
                .header("accept", "application/json")
                .expect()
                .body(Matchers.containsString("orderId"))
                .log().ifError()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/api/orders/vm/remove");
    }
}
