package no.nav.aura.basta.rest.api;

import static com.jayway.restassured.RestAssured.given;

import java.util.Set;

import no.nav.aura.basta.JettyTest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.order.VmOrderTestData;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

public class OrdersVMRestApiServiceTest extends JettyTest {

    @Before
    public void setup() {
		RestAssured.port = jetty.getPort();
        System.out.println("Katalog er: " + System.getProperty("user.dir"));
    }


    @Test
    public void checkDecommisionCallback() {
        System.out.println("Katalog er: " + System.getProperty("user.dir"));
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
    public void checkCreateCallback() {
        Order order = repository.save(VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<vms><vm><hostName>newserver.devillo.no</hostName><deployUser>deployer</deployUser><deployerPassword>secret</deployerPassword></vm></vms>")
                .contentType(ContentType.XML)
                .expect()
                .log().ifError()
                .statusCode(204)
                .when()
                .put("/rest/api/orders/vm/{orderId}/vm", order.getId());

        VMOrderResult result = repository.findOne(order.getId()).getResultAs(VMOrderResult.class);
        Assert.assertThat(result.hostnames(), Matchers.contains("newserver.devillo.no"));
    }

    @Test
    public void checkLogCallback() {
        Order order = repository.save(VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<status><text>hallo verden</text> <type>puppetverify:ok</type> <option/> </status>")
                .contentType(ContentType.XML)
                .expect()
                .log().ifError()
                .statusCode(204)
                .when()
                .post("/rest/api/orders/vm/{orderId}/statuslog", order.getId());

        // Order result = repository.findOne(order.getId());
        // assertThatLogContains("hallo verden", result.getStatusLogs());
    }

    private void assertThatLogContains(String string, Set<OrderStatusLog> statusLogs) {
        for (OrderStatusLog log : statusLogs) {
            if (log.getStatusText().equals(string)) {
                return;
            }
        }
        Assert.fail("String " + string + "not found in statuslogs " + statusLogs);

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
