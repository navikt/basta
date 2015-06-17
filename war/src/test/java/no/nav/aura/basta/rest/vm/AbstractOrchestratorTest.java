package no.nav.aura.basta.rest.vm;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.ProvisionRequest2;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.RestServiceTestUtils;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public abstract class AbstractOrchestratorTest {

    @Inject
    protected AuthenticationManager authenticationManager;

    @Inject
    protected OrderRepository orderRepository;

    protected FasitRestClient fasitRestClient;
    protected OrchestratorService orchestratorService;

    @BeforeClass
    public static void setFasitBaseUrl() {
        System.setProperty("fasit.rest.api.url", "http://e34apsl00136.devillo.no:8080/conf");
    }

    @Before
    public void initMocks() {
        fasitRestClient = Mockito.mock(FasitRestClient.class);
        orchestratorService = Mockito.mock(OrchestratorService.class);
    }

    public void cleanUp() {
        mockLogout();
    }

    private void mockLogout() {
        // TODO
    }

    protected void login(String userName, String password) {
        Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        SecurityContextHolder.getContext().setAuthentication(token);

    }

    /**
     * 
     */
    protected String mockOrchestratorProvision() {
        String returnId = UUID.randomUUID().toString();
        WorkflowToken workflowToken = new WorkflowToken();
        workflowToken.setId(returnId);
        when(orchestratorService.provision(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);
        return returnId;
    }

    protected ProvisionRequest2 getAndValidateOrchestratorRequest(long orderid) {
        ArgumentCaptor<ProvisionRequest2> argumentCaptor = ArgumentCaptor.forClass(ProvisionRequest2.class);
        verify(orchestratorService).provision(argumentCaptor.capture());
        ProvisionRequest2 request = argumentCaptor.getValue();
        assertEquals("http://unittest:666/api/orders/vm/" + orderid + "/vm", request.getResultCallbackUrl().toString());
        assertEquals("http://unittest:666/api/orders/vm/" + orderid + "/statuslog", request.getStatusCallbackUrl().toString());
        return request;
    }

    protected Order getCreatedOrderFromResponseLocation(Response response) {
        Long orderId = RestServiceTestUtils.getOrderIdFromMetadata(response);
        Order order = orderRepository.findOne(orderId);
        assertThat(order, notNullValue());
        return order;
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

        try {
            String requestXml = XmlUtils.generateXml(request);
            String xml = XmlUtils.prettyFormat(requestXml, 2);
            System.out.println("### xml: \n" + xml);

            InputSource expectedXmlSource = new InputSource(AbstractOrchestratorTest.class.getResourceAsStream(expectXml));
            InputSource requestXmlSource = new InputSource(new StringReader(xml));
            // Diff diff = new Diff(expectedXmlSource, requestXml);
            XMLAssert.assertXMLEqual("compare request with file: " + expectXml, expectedXmlSource, requestXmlSource);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
