package no.nav.aura.basta.order;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import no.nav.aura.basta.persistence.ApplicationServerType;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.ServerSize;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.rest.SettingsDO;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public class OrderV2FactoryTest extends XMLTestCase {

    @Inject
    private AuthenticationManager authenticationManager;

    @Before
    public void primeXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void createWasOrder() throws Exception {
        createOrder(createRequest1Settings(), "orderv2_was_request.xml");
    }

    @SuppressWarnings("serial")
    private void createOrder(final SettingsDO settings, final String expectXml) {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                try {
                    FasitRestClient fasitRestClient = createFasitMock();
                    ProvisionRequest order = new OrderV2Factory(settings, "admin", new URI("http://thisisbasta/orders/vm"), new URI("http://thisisbasta/orders/results"), fasitRestClient).createOrder();
                    String xml = XmlUtils.prettyFormat(XmlUtils.generateXml(order), 2);
                    // System.out.println("### xml: " + xml);
                    Diff diff = new Diff(new InputStreamReader(getClass().getResourceAsStream(expectXml)), new StringReader(xml));
                    diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
                    assertXMLEqual(diff, true);
                } catch (JAXBException | SAXException | IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private FasitRestClient createFasitMock() {
        FasitRestClient fasitRestClient = mock(FasitRestClient.class);
        ResourceElement deploymentManager = new ResourceElement(ResourceTypeDO.DeploymentManager, "anything");
        deploymentManager.getProperties().add(new PropertyElement("hostname", "e34jbsl00995.devillo.no"));
        when(fasitRestClient.getResource(anyString(), anyString(), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString()))
                .thenReturn(deploymentManager);
        return fasitRestClient;
    }

    public static SettingsDO createRequest1Settings() {
        SettingsDO settings = new SettingsDO();
        settings.setApplicationServerType(ApplicationServerType.wa);
        settings.setEnvironmentName("lars_slett");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.m);
        settings.setDisk(true);
        settings.setZone(Zone.fss);
        settings.setApplicationName("autodeploy-test");
        settings.setEnvironmentClass(EnvironmentClass.u);
        return settings;
    }

}
