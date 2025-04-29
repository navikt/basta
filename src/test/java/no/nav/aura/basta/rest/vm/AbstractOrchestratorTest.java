package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.deprecated.payload.SecretPayload;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.rest.AbstractRestServiceTest;
import no.nav.aura.basta.util.XmlUtils;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractOrchestratorTest extends AbstractRestServiceTest {

    protected OrchestratorClient orchestratorClient;
    
    protected static ResourcePayload createResource(ResourceType type, String alias, Map<String, String> properties) {
        return new ResourcePayload()
                .withType(type)
                .withAlias(alias)
                .withProperties(properties);
    }

    protected static ResourcePayload createResourceWithSecret(ResourceType type, String alias, Map<String, String> properties) {
        SecretPayload secret = new SecretPayload();
        secret.ref = "fasit/secret/123";
        secret.value = "password";
        HashMap<String, SecretPayload> password = new HashMap<>();
        password.put("password", secret);
        return createResource(type, alias, properties).withSecrets(password);
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
        orchestratorClient = Mockito.mock(OrchestratorClient.class);
    }

    protected void mockOrchestratorProvision() {
        String returnId = UUID.randomUUID().toString();
        when(orchestratorClient.provision(Mockito.any())).thenReturn(Optional.of("http://" + returnId));
    }

    protected ProvisionRequest getAndValidateOrchestratorRequest(long orderid) {
        ArgumentCaptor<ProvisionRequest> argumentCaptor = ArgumentCaptor.forClass(ProvisionRequest.class);
        verify(orchestratorClient).provision(argumentCaptor.capture());
        ProvisionRequest request = argumentCaptor.getValue();
        Assertions.assertEquals("https://unittest:666/api/orders/vm/" + orderid + "/vm", request.getResultCallbackUrl().toString
                ());
        Assertions.assertEquals("https://unittest:666/api/orders/vm/" + orderid + "/statuslog", request.getStatusCallbackUrl()
                .toString());
        return request;
    }

}
