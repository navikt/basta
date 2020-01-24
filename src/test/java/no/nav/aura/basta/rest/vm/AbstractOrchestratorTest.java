package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.payload.SecretPayload;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.rest.AbstractRestServiceTest;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
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

    protected static ResourceElement createResource(ResourceTypeDO type, String alias, PropertyElement... properties) {
        ResourceElement resource = new ResourceElement(type, alias);
        for (PropertyElement propertyElement : properties) {
            resource.addProperty(propertyElement);
        }
        return resource;
    }

    protected static void assertRequestXML(final OrchestatorRequest request, final String expectXml) {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);

        try {
            String requestXml = XmlUtils.generateXml(request);
            String xml = XmlUtils.prettyFormat(requestXml, 2);
            InputSource expectedXmlSource = new InputSource(AbstractOrchestratorTest.class.getResourceAsStream(expectXml));
            InputSource requestXmlSource = new InputSource(new StringReader(xml));
            XMLAssert.assertXMLEqual("compare request with file: " + expectXml, expectedXmlSource, requestXmlSource);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void mockOrcestrator() {
        orchestratorClient = Mockito.mock(OrchestratorClient.class);
    }

    public void cleanUp() {
        mockLogout();
    }

    private void mockLogout() {
        // TODO
    }

    protected void mockOrchestratorProvision() {
        String returnId = UUID.randomUUID().toString();
        when(orchestratorClient.provision(Mockito.anyObject())).thenReturn(Optional.of("http://" + returnId));
    }

    protected ProvisionRequest getAndValidateOrchestratorRequest(long orderid) {
        ArgumentCaptor<ProvisionRequest> argumentCaptor = ArgumentCaptor.forClass(ProvisionRequest.class);
        verify(orchestratorClient).provision(argumentCaptor.capture());
        ProvisionRequest request = argumentCaptor.getValue();
        assertEquals("https://unittest:666/api/orders/vm/" + orderid + "/vm", request.getResultCallbackUrl().toString
                ());
        assertEquals("https://unittest:666/api/orders/vm/" + orderid + "/statuslog", request.getStatusCallbackUrl()
                .toString());
        return request;
    }

}
