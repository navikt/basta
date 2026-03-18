package no.nav.aura.basta.rest.vm;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.InputSource;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SecretPayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.rest.AbstractRestServiceTest;
import no.nav.aura.basta.util.XmlUtils;

public abstract class AbstractOrchestratorTest extends AbstractRestServiceTest {

    @Autowired
    protected OrchestratorClient orchestratorClient;

    
    protected static ResourcePayload createResource(ResourceType type, String alias, Map<String, String> properties) {
    	ResourcePayload resource = new ResourcePayload(type, alias);
    	resource.setProperties(properties);
        return resource;
        		
    }

    protected static ResourcePayload createResourceWithSecret(ResourceType type, String alias, Map<String, String> properties) {
        SecretPayload secret = SecretPayload.withVaultPath("fasit/secret/123");
        secret.value = "password";
        secret.ref = URI.create("http://unittest:666/api/v2/secret/123");
        HashMap<String, SecretPayload> password = new HashMap<>();
        password.put("password", secret);
        
        ResourcePayload resource = createResource(type, alias, properties);
        resource.setSecrets(password);
        return resource;
    }

    protected static void assertRequestXML(final OrchestatorRequest request, final String expectXml) {
        String requestXml = XmlUtils.generateXml(request);
		String xml = XmlUtils.prettyFormat(requestXml, 2);
		InputSource source = new InputSource(AbstractOrchestratorTest.class.getResourceAsStream(expectXml));
		String expectedXmlSource = null;
		try {
			expectedXmlSource = new String(source.getByteStream().readAllBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		XmlAssert.assertThat(xml).and(expectedXmlSource)
			.ignoreWhitespace()
			.ignoreComments()
			.ignoreChildNodesOrder()
			.withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes))
			.areIdentical()
		    .withFailMessage("compare request with file: " + expectXml);
    }

    @BeforeEach
    public void mockOrchestrator() {
        // Use Spring-managed mock bean from test configuration
//    	orchestratorClient = Mockito.mock(OrchestratorClient.class);
    }

    @AfterEach
    public void reset() {
        Mockito.reset(orchestratorClient);
    }

    protected ProvisionRequest getAndValidateOrchestratorRequest(long orderid) {
        ArgumentCaptor<ProvisionRequest> argumentCaptor = ArgumentCaptor.forClass(ProvisionRequest.class);
        verify(orchestratorClient).provision(argumentCaptor.capture());
        ProvisionRequest request = argumentCaptor.getValue();
        Assertions.assertEquals("http://localhost:1337/rest/api/orders/vm/" + orderid + "/vm", request.getResultCallbackUrl().toString
                ());
        Assertions.assertEquals("http://localhost:1337/rest/api/orders/vm/" + orderid + "/statuslog", request.getStatusCallbackUrl()
                .toString());
        return request;
    }

}