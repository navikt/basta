package no.nav.aura.basta.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.restassured.http.ContentType;
import no.nav.aura.basta.ApplicationTest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrdersListRestServiceTest extends ApplicationTest {

    private Order testOrder1;
    private Order testOrder2;
    private Order testOrder3;
    private Order testOrder4;
    private Order deletedOrder;

    @BeforeEach
    public void setupTestData() {
        // Create test orders with different data
        Map<String, String> input1 = new HashMap<>();
        input1.put("applicationName", "testapp1");
        input1.put("environmentClass", "u");
        input1.put("zone", "fss");
        testOrder1 = new Order(OrderType.VM, OrderOperation.CREATE, input1);
        testOrder1.setStatus(OrderStatus.SUCCESS);
        testOrder1.getStatusLogs().add(new OrderStatusLog("Test", "Order created", "info", StatusLogLevel.info));
        testOrder1.getStatusLogs().add(new OrderStatusLog("Test", "Order processing", "info", StatusLogLevel.info));
        testOrder1 = orderRepository.save(testOrder1);

        Map<String, String> input2 = new HashMap<>();
        input2.put("applicationName", "testapp2");
        input2.put("environmentClass", "t");
        testOrder2 = new Order(OrderType.BIGIP, OrderOperation.CREATE, input2);
        testOrder2.setStatus(OrderStatus.PROCESSING);
        testOrder2.getStatusLogs().add(new OrderStatusLog("Test", "Order submitted", "info", StatusLogLevel.info));
        testOrder2 = orderRepository.save(testOrder2);

        Map<String, String> input3 = new HashMap<>();
        input3.put("applicationName", "testapp3");
        input3.put("environmentClass", "p");
        input3.put("zone", "fss");
        testOrder3 = new Order(OrderType.VM, OrderOperation.CREATE, input3);
        testOrder3.setStatus(OrderStatus.FAILURE);
        testOrder3.setErrorMessage("Test error message");
        testOrder3.getStatusLogs().add(new OrderStatusLog("Test", "Order failed", "error", StatusLogLevel.error));
        testOrder3 = orderRepository.save(testOrder3);
        
        Map<String, String> oracleInput = new HashMap<>();
        oracleInput.put("applicationName", "app");
        oracleInput.put("environmentName", "env");
        oracleInput.put("environmentClass", "u");
        oracleInput.put("databaseName", "x_y");
        oracleInput.put("templateURI", "a.b/c");
        oracleInput.put("zoneURI", "b.c/d");
        testOrder4 = new Order(OrderType.OracleDB, OrderOperation.CREATE, oracleInput);
        testOrder4.setStatus(OrderStatus.SUCCESS);
        testOrder4.getStatusLogs().add(new OrderStatusLog("Test", "Order created", "info", StatusLogLevel.info));
        Map<String, String> oracleResultMap = new HashMap<>();
        oracleResultMap.put("username", "dbuser");
        oracleResultMap.put("password", "dbpass");
        oracleResultMap.put("fasitAlias", "dbAlias");
        testOrder4.setResults(oracleResultMap);
        testOrder4 = orderRepository.save(testOrder4);
        
        Map<String, String> deleteInput = new HashMap<>();
        deleteInput.put("hostName", "test.example.com");
        deletedOrder = new Order(OrderType.VM, OrderOperation.DELETE, oracleInput);
        deletedOrder.setStatus(OrderStatus.SUCCESS);
        deletedOrder = orderRepository.save(deletedOrder);

    }

	@AfterAll
	public void tearDown() {
		orderRepository.deleteAll();
	}

    @Test
    public void testGetOrdersInPages_firstPage() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/page/0/10")
        .then()
            .log().ifError()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .header("total_count", notNullValue())
            .header("page_count", notNullValue())
            .body("size()", greaterThanOrEqualTo(3))
            .body("[0].id", notNullValue())
            .body("[0].orderType", notNullValue())
            .body("[0].orderOperation", notNullValue());
    }

    @Test
    public void testGetOrdersInPages_withCacheControl() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/page/0/10")
        .then()
            .log().ifError()
            .statusCode(200)
            .header("Cache-Control", containsString("max-age=30"));
    }

    @Test
    public void testGetOrdersInPages_secondPageWithSmallSize() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/page/0/2")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(2));
    }

    @Test
    public void testGetOrdersInPages_emptyPage() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/page/1000/10")
        .then()
            .log().ifError()
            .statusCode(204); // NO_CONTENT when page is empty
    }

    @Test
    public void testGetOrdersInPages_pagination() {
        // Get first page
        String totalCount = given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/page/0/2")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(2))
            .extract()
            .header("total_count");

        assertNotNull(totalCount);
        assertTrue(Integer.parseInt(totalCount) >= 3, "Total count should be at least 3");
    }

    @Test
    public void testGetOrder_existingOrder() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/" + testOrder1.getId())
        .then()
            .log().all()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(testOrder1.getId().intValue()))
            .body("orderType", equalTo("VM"))
            .body("orderOperation", equalTo("CREATE"))
            .body("status", equalTo("SUCCESS"))
            .body("input", notNullValue())
            .body("input.applicationName", equalTo("testapp1"))
            .body("results", notNullValue());
    }

    @Test
    public void testGetOrder_withFailedStatus() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/" + testOrder3.getId())
        .then()
            .log().ifError()
            .statusCode(200)
            .body("id", equalTo(testOrder3.getId().intValue()))
            .body("status", equalTo("FAILURE"))
            .body("errorMessage", equalTo("Test error message"))
            .body("orderType", equalTo("VM"));
    }

    @Test
    public void testGetOrder_nonExistingOrder() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/999999")
        .then()
            .log().ifError()
            .statusCode(404); // NOT_FOUND
    }

    @Test
    public void testGetOrder_invalidId() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/invalid")
        .then()
            .log().ifError()
            .statusCode(400); // BAD_REQUEST for invalid ID format
    }

    @Test
    public void testGetStatusLog_existingOrder() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/" + testOrder1.getId() + "/statuslog")
        .then()
            .log().ifError()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .header("Cache-Control", containsString("no-store, must-revalidate"))
            .body("size()", equalTo(2))
            .body("[0].text", notNullValue())
            .body("[0].type", notNullValue())
            .body("[1].text", notNullValue());
    }

    @Test
    public void testGetStatusLog_verifyLogContent() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/" + testOrder1.getId() + "/statuslog")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("text", hasItems("Order created", "Order processing"))
            .body("type", everyItem(equalTo("info")));
    }

    @Test
    public void testGetStatusLog_orderWithSingleLog() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/" + testOrder2.getId() + "/statuslog")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].text", equalTo("Order submitted"));
    }

    @Test
    public void testGetStatusLog_errorLevel() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/" + testOrder3.getId() + "/statuslog")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].text", equalTo("Order failed"))
            .body("[0].type", equalTo("error"));
    }

    @Test
    public void testGetStatusLog_nonExistingOrder() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/999999/statuslog")
        .then()
            .log().ifError()
            .statusCode(404);
    }

    @Test
    public void testGetStatusLog_noCacheHeaders() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/" + testOrder1.getId() + "/statuslog")
        .then()
            .log().all()
            .statusCode(200)
            .header("Cache-Control", containsString("no-store"))
            .header("Cache-Control", containsString("must-revalidate"));
    }

    @Test
    public void testGetOrder_verifyRichOrderDO() {
        // This test verifies that the createRichOrderDO method properly populates
        // both input and result history
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/orders/" + testOrder1.getId())
        .then()
            .log().ifError()
            .statusCode(200)
            .body("input", notNullValue())
            .body("results", notNullValue());
    }

    @Test
    public void testMultipleOrderTypes() {
        // Verify we can retrieve orders of different types
        given().get("/rest/orders/" + testOrder1.getId())
            .then().statusCode(200).body("orderType", equalTo("VM"));

        given().get("/rest/orders/" + testOrder2.getId())
            .then().statusCode(200).body("orderType", equalTo("BIGIP"));

        given().get("/rest/orders/" + testOrder3.getId())
            .then().statusCode(200).body("orderType", equalTo("VM"));

        given().get("/rest/orders/" + testOrder4.getId())
        	.then().log().all().statusCode(200).body("orderType", equalTo("OracleDB"));
    }

    @Test
    public void testOrderOperations() {
        // Verify different order operations
        given().get("/rest/orders/" + testOrder1.getId())
            .then().statusCode(200).body("orderOperation", equalTo("CREATE"));

        given().get("/rest/orders/" + deletedOrder.getId())
            .then().statusCode(200).body("orderOperation", equalTo("DELETE"));
    }
}
