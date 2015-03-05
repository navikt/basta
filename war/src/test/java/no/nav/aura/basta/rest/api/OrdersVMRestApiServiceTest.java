package no.nav.aura.basta.rest.api;

import static com.jayway.restassured.RestAssured.given;
import no.nav.aura.basta.domain.Order;

import org.junit.Test;

import com.jayway.restassured.http.ContentType;

public class OrdersVMRestApiServiceTest extends RestTest {

    public void setup() {

    }

    @Test
    public void checkDecommisionCallback() {
        Order order = repository.save(Order.newDecommissionOrder("host1.devillo.no"));
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
        Order order = repository.save(Order.newStartOrder("host1.devillo.no"));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<vm><hostName>host1.devillo.no</hostName></vm>")
                .contentType(ContentType.XML)
                .expect()
                .log().ifError()
                .statusCode(204)
                .when()
                .put("/rest/api/orders/vm/{orderId}/start", order.getId());

    }

    @Test
    public void checkStopCallback() {
        Order order = repository.save(Order.newStopOrder("host1.devillo.no"));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<vm><hostName>host1.devillo.no</hostName></vm>")
                .contentType(ContentType.XML)
                .expect()
                .log().ifError()
                .statusCode(204)
                .when()
                .put("/rest/api/orders/vm/{orderId}/stop", order.getId());
    }
}
