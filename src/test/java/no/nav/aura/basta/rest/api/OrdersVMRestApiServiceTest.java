package no.nav.aura.basta.rest.api;

import io.restassured.http.ContentType;
import no.nav.aura.basta.ApplicationTest;
import no.nav.aura.basta.JaxrsApplication;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;

import static org.assertj.core.api.Assertions.assertThat;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import static io.restassured.RestAssured.given;

public class OrdersVMRestApiServiceTest extends ApplicationTest {
    
	@Inject
	VmOrdersRestApi vmOrdersRestApi;

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();
    @Test
    void testConditionalBean() {
        contextRunner.withUserConfiguration(JaxrsApplication.class)
                     .run(context -> Assertions.assertThat(context).hasSingleBean(JaxrsApplication.class));
    }
	@Test
	void componentShouldBeLoaded() {
	    assertThat(vmOrdersRestApi).isNotNull();
	}
    
	@Test
	public void checkDecommissionCallbackServiceWithoutAuth() {
		Order order = orderRepository.save(VmOrderTestData.newDecommissionOrder("host1.devillo.no"));
		
		OrchestratorNodeDO orcNode = new OrchestratorNodeDO();
		orcNode.setHostName("host1.devillo.no");
		
		vmOrdersRestApi.removeCallback(order.getId(), orcNode);
	}
	
    @Test
    public void checkDecommissionCallback() {
        Order order = orderRepository.save(VmOrderTestData.newDecommissionOrder("host1.devillo.no"));
  
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<vm><hostName>host1.devillo.no</hostName></vm>")
                .contentType(ContentType.XML)
                .expect()
                .statusCode(204)
                .log().ifError()
                .when()
                .put("rest/api/orders/vm/{orderId}/decommission", order.getId());
        
    }

    @Test
    public void checkStartCallback() {
        Order order = orderRepository.save(VmOrderTestData.newStartOrder("host2.devillo.no"));
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
        VMOrderResult result = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId())).getResultAs(VMOrderResult.class);
        MatcherAssert.assertThat(result.hostnames(), Matchers.contains("host2.devillo.no"));
//        assertThat(result.hostnames(), Matchers.contains("host2.devillo.no"));

    }

    @Test
    public void checkStopCallback() {
        Order order = orderRepository.save(VmOrderTestData.newStopOrder("host3.devillo.no"));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<operationResponse><vm><hostName>host3.devillo.no</hostName><result>off</result></vm></operationResponse>")
                .contentType(ContentType.XML)
                .expect()
                .log().ifError()
                .statusCode(204)
                .when()
                .put("/rest/api/orders/vm/{orderId}/stop", order.getId());

        VMOrderResult result = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId())).getResultAs(VMOrderResult.class);
        MatcherAssert.assertThat(result.hostnames(), Matchers.contains("host3.devillo.no"));
    }

    @Test
    public void checkCreateCallback() {
        Order order = orderRepository.save(VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<vms><vm><hostName>newserver.devillo.no</hostName><deployUser>deployer</deployUser><deployerPassword>secret</deployerPassword></vm></vms>")
                .contentType(ContentType.XML)
                .expect()
                .log().ifError()
                .statusCode(204)
                .when()
                .put("/rest/api/orders/vm/{orderId}/vm", order.getId());

        VMOrderResult result = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId())).getResultAs(VMOrderResult.class);
        MatcherAssert.assertThat(result.hostnames(), Matchers.contains("newserver.devillo.no"));
    }

    @Test
    public void checkLogCallback() {
        Order order = orderRepository.save(VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS));
        given()
                .auth().basic("prodadmin", "prodadmin")
                .body("<status><text>hallo verden</text> <type>puppetverify:ok</type> <option/> </status>")
                .contentType(ContentType.XML)
                .expect()
                .log().ifError()
                .statusCode(204)
                .when()
                .post("/rest/api/orders/vm/{orderId}/statuslog", order.getId());
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
