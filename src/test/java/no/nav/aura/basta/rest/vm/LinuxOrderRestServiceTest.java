package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import org.junit.jupiter.api.Test;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class LinuxOrderRestServiceTest extends AbstractOrchestratorTest {

    @Test
    public void orderPlainLinuxShouldgiveNiceXml() {
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
    	input.put("zone", "fss");
    	input.put("serverCount", "1");
    	input.put("memory", "1");
    	input.put("cpuCount", "1");
    	input.put("ibmSw", "false");

        int ord = given()
                .auth().preemptive().basic("user", "user")
                .body(input)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/vm/orders/linux")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        long orderId = Long.valueOf(ord);
        Order order = getCreatedOrderFromResponseLocation(orderId);

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());

        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/linux_order.xml");

    }

	@Test
    public void orderPlainLinuxRhel9ShouldgiveNiceXml() {
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
    	input.put("zone", "fss");
    	input.put("osType", "rhel90");
    	input.put("serverCount", "1");
    	input.put("memory", "1");
    	input.put("cpuCount", "1");
    	input.put("ibmSw", "false");
    	
        int ord = given()
                .auth().preemptive().basic("user", "user")
                .body(input)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/vm/orders/linux")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        long orderId = Long.valueOf(ord);
        Order order = getCreatedOrderFromResponseLocation(orderId);

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());

        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/linux_order_rhel9.xml");

    }

    @Test
    public void orderFlatcarLinuxShouldgiveNiceXml() {
        Map<String, String> input = new HashMap<>();
        input.put("environmentClass", "u");
		input.put("zone", "fss");
		input.put("serverCount", "1");
		input.put("memory", "1");
		input.put("cpuCount", "1");
		input.put("ibmSw", "false");
		input.put("osType", "flatcar");
        		
        int ord = given()
                .auth().preemptive().basic("user", "user")
                .body(input)
                .contentType(ContentType.JSON)
                .when()
                .post("/rest/vm/orders/flatcarlinux")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        long orderId = Long.valueOf(ord);
        Order order = getCreatedOrderFromResponseLocation(orderId);
        
        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());

        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/flatcarlinux_order.xml");

    }
}
