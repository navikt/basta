package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;

import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.ProvisionRequest2;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@TransactionConfiguration
@Transactional()
public class LinuxOrderRestServiceTest {

	@Inject
	private AuthenticationManager authenticationManager;

	@Inject
	private OrderRepository orderRepository;

	@Inject
    private LinuxOrderRestService ordersRestService;

	@Inject
	private FasitRestClient fasitRestClient;

	@Inject
	private OrchestratorService orchestratorService;

	@BeforeClass
	public static void setFasitBaseUrl() {
		System.setProperty("fasit.rest.api.url", "http://e34apsl00136.devillo.no:8080/conf");
	}

	@Before
	public void setup() {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
	}

	@After
	public void resetMockito() {
		Mockito.reset(fasitRestClient, orchestratorService);
	}

	@SuppressWarnings("serial")
	@Test
	public void orderPlainLinuxhsouldgiveNiceXml() {
		SpringRunAs.runAs(authenticationManager, "user", "user", new Effect() {
			public void perform() {
                VMOrderInput input = new VMOrderInput();
				input.setEnvironmentClass(EnvironmentClass.u);
				input.setServerCount(1);
                input.setMemory(1024);
                input.setCpuCount(1);
				

				String orchestratorOrderId = UUID.randomUUID().toString();
				WorkflowToken workflowToken = new WorkflowToken();
				workflowToken.setId(orchestratorOrderId);
				when(orchestratorService.provision(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);

				ordersRestService.createNewPlainLinux(input.copy(), createUriInfo());
				ArgumentCaptor<ProvisionRequest2> argumentCaptor = ArgumentCaptor.forClass(ProvisionRequest2.class);
				verify(orchestratorService).provision(argumentCaptor.capture());
				Order order = orderRepository.findByExternalId(orchestratorOrderId);
                assertThat(order, notNullValue());
                
                ProvisionRequest2 argument = argumentCaptor.getValue();
                assertEquals("http://unittest:666/api/orders/vm/" + order.getId() + "/vm", argument.getResultCallbackUrl().toString());
                assertEquals("http://unittest:666/api/orders/vm/" + order.getId() + "/statuslog", argument.getStatusCallbackUrl().toString());
				
                // mock out urls for xml matching
                argument.setResultCallbackUrl(URI.create("http://callback/result"));
                argument.setStatusCallbackUrl(URI.create("http://callback/status"));
				assertRequestXML(argument, "/orchestrator/request/linux_order.xml");
			}
		});

	}

	private static void assertRequestXML(final OrchestatorRequest request, final String expectXml) {

		try {
			String requestXml = XmlUtils.generateXml(request);
			String xml = XmlUtils.prettyFormat(requestXml, 2);
            // System.out.println("### xml: \n" + xml);

			InputSource expectedXmlSource = new InputSource(LinuxOrderRestServiceTest.class.getResourceAsStream(expectXml));
			InputSource requestXmlSource = new InputSource(new StringReader(xml));
			// Diff diff = new Diff(expectedXmlSource, requestXml);
			XMLAssert.assertXMLEqual("compare request with file: " + expectXml, expectedXmlSource, requestXmlSource);
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}



	
}
