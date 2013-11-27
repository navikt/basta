package no.nav.aura.basta.order;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import no.nav.aura.basta.rest.SettingsDO;
import no.nav.aura.basta.rest.SettingsDO.ApplicationServerType;
import no.nav.aura.basta.rest.SettingsDO.EnvironmentClassDO;
import no.nav.aura.basta.rest.SettingsDO.ServerSize;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
                    ProvisionRequest order = new OrderV2Factory(settings, "admin", new URI("http://thisisfasit/conf"), new URI("http://thisisbasta/orders/results")).createOrder();
                    String xml = XmlUtils.prettyFormat(XmlUtils.generateXml(order), 2);
                    System.out.println("### xml: " + xml);
                    Diff diff = new Diff(new InputStreamReader(getClass().getResourceAsStream(expectXml)), new StringReader(xml));
                    diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
                    assertXMLEqual(diff, true);
                } catch (JAXBException | SAXException | IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static SettingsDO createRequest1Settings() {
        SettingsDO settings = new SettingsDO();
        settings.setApplicationServerType(ApplicationServerType.wa);
        settings.setEnvironmentName("lars_slett");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.m);
        settings.setDisk(true);
        settings.setZone(SettingsDO.Zone.fss);
        settings.setApplicationName("autodeploy-test");
        settings.setEnvironmentClass(EnvironmentClassDO.utv);
        return settings;
    }

}
