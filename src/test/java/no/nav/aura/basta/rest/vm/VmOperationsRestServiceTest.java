package no.nav.aura.basta.rest.vm;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import io.restassured.http.ContentType;
import no.nav.aura.basta.ApplicationTest;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.domain.Order;

import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;

public class VmOperationsRestServiceTest extends ApplicationTest {

    // Hostnames whose prefix determines the EnvironmentClass (see findEnvironmentFromHostame):
    //   e* -> u (user access), d* -> t, b* -> q, a*/c* -> p (prodadmin access)
    private static final String U_HOST = "ehost1.devillo.no";  // EnvironmentClass.u – 'user' can access
    private static final String P_HOST = "ahost1.adeo.no";     // EnvironmentClass.p – only prodadmin can access

    @Autowired
    private OrchestratorClient orchestratorClient;

    @AfterEach
    public void resetMockAndCleanup() {
        Mockito.reset(orchestratorClient);
        orderRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // /rest/vm/operations/decommission
    // -------------------------------------------------------------------------

    @Test
    public void decommissionHappyPath() {
        when(orchestratorClient.decomission(any()))
                .thenReturn(Optional.of("http://orchestrator/execution/decommission/1"));

        int orderId = given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .log().ifError()
                .statusCode(201)
                .when()
                .post("/rest/vm/operations/decommission")
                .then()
                .extract().path("orderId");

        Order order = orderRepository.findById((long) orderId).orElseThrow();
        assertThat(order.getExternalId()).isEqualTo("http://orchestrator/execution/decommission/1");
    }

    @Test
    public void decommissionSetsOrderToFailureWhenOrchestratorReturnsEmpty() {
        when(orchestratorClient.decomission(any())).thenReturn(Optional.empty());

        int orderId = given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .log().ifError()
                .statusCode(201)
                .when()
                .post("/rest/vm/operations/decommission")
                .then()
                .extract().path("orderId");

        Order order = orderRepository.findById((long) orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(FAILURE);
    }

    @Test
    public void decommissionWithoutAuthFails() {
        given()
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .statusCode(401)
                .when()
                .post("/rest/vm/operations/decommission");
    }

    @Test
    public void decommissionWithInsufficientRoleFails() {
        // 'user' only has ROLE_USER (EnvironmentClass.u), cannot access p-class hosts
        given()
                .auth().preemptive().basic("user", "user")
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .statusCode(401)
                .when()
                .post("/rest/vm/operations/decommission");
    }

    @Test
    public void decommissionUserClassHostWithUserRole() {
        when(orchestratorClient.decomission(any()))
                .thenReturn(Optional.of("http://orchestrator/execution/decommission/2"));

        given()
                .auth().preemptive().basic("user", "user")
                .contentType(ContentType.JSON)
                .body("[\"" + U_HOST + "\"]")
                .expect()
                .log().ifError()
                .statusCode(201)
                .when()
                .post("/rest/vm/operations/decommission");
    }

    // -------------------------------------------------------------------------
    // /rest/vm/operations/stop
    // -------------------------------------------------------------------------

    @Test
    public void stopHappyPath() {
        when(orchestratorClient.stop(any()))
                .thenReturn(Optional.of("http://orchestrator/execution/stop/1"));

        int orderId = given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .log().ifError()
                .statusCode(201)
                .when()
                .post("/rest/vm/operations/stop")
                .then()
                .extract().path("orderId");

        Order order = orderRepository.findById((long) orderId).orElseThrow();
        assertThat(order.getExternalId()).isEqualTo("http://orchestrator/execution/stop/1");
    }

    @Test
    public void stopSetsOrderToFailureWhenOrchestratorReturnsEmpty() {
        when(orchestratorClient.stop(any())).thenReturn(Optional.empty());

        int orderId = given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .log().ifError()
                .statusCode(201)
                .when()
                .post("/rest/vm/operations/stop")
                .then()
                .extract().path("orderId");

        Order order = orderRepository.findById((long) orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(FAILURE);
    }

    @Test
    public void stopWithoutAuthFails() {
        given()
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .statusCode(401)
                .when()
                .post("/rest/vm/operations/stop");
    }

    @Test
    public void stopWithInsufficientRoleFails() {
        given()
                .auth().preemptive().basic("user", "user")
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .statusCode(401)
                .when()
                .post("/rest/vm/operations/stop");
    }

    // -------------------------------------------------------------------------
    // /rest/vm/operations/start
    // -------------------------------------------------------------------------

    @Test
    public void startHappyPath() {
        when(orchestratorClient.start(any()))
                .thenReturn(Optional.of("http://orchestrator/execution/start/1"));

        int orderId = given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .log().ifError()
                .statusCode(201)
                .when()
                .post("/rest/vm/operations/start")
                .then()
                .extract().path("orderId");

        Order order = orderRepository.findById((long) orderId).orElseThrow();
        assertThat(order.getExternalId()).isEqualTo("http://orchestrator/execution/start/1");
    }

    @Test
    public void startSetsOrderToFailureWhenOrchestratorReturnsEmpty() {
        when(orchestratorClient.start(any())).thenReturn(Optional.empty());

        int orderId = given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .log().ifError()
                .statusCode(201)
                .when()
                .post("/rest/vm/operations/start")
                .then()
                .extract().path("orderId");

        Order order = orderRepository.findById((long) orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(FAILURE);
    }

    @Test
    public void startWithoutAuthFails() {
        given()
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .statusCode(401)
                .when()
                .post("/rest/vm/operations/start");
    }

    @Test
    public void startWithInsufficientRoleFails() {
        given()
                .auth().preemptive().basic("user", "user")
                .contentType(ContentType.JSON)
                .body("[\"" + P_HOST + "\"]")
                .expect()
                .statusCode(401)
                .when()
                .post("/rest/vm/operations/start");
    }
}
