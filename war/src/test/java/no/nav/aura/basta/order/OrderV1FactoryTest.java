package no.nav.aura.basta.order;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.inject.Inject;

import no.nav.aura.basta.SpringUnitTestConfig;
import no.nav.aura.basta.rest.OrdersRestService;
import no.nav.aura.basta.rest.SettingsDO;
import no.nav.aura.basta.rest.SettingsDO.ApplicationServerType;
import no.nav.aura.basta.rest.SettingsDO.EnvironmentClassDO;
import no.nav.aura.basta.rest.SettingsDO.ServerSize;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public class OrderV1FactoryTest extends XMLTestCase {

    @Inject
    private AuthenticationManager authenticationManager;

    @Before
    public void primeXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @SuppressWarnings("serial")
    @Test
    public void createJbossOrder() throws Exception {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                SettingsDO settings = createRequest1Settings();
                String xml = new OrdersRestService().postOrder(settings, true);
                try {
                    Diff diff = new Diff(new InputStreamReader(getClass().getResourceAsStream("orderv1_jb_request.xml")), new StringReader(xml));
                    diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
                    assertXMLEqual(diff, true);
                } catch (SAXException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @SuppressWarnings("serial")
    @Test(expected = UnauthorizedException.class)
    public void notAuthenticated() throws Exception {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                SettingsDO settings = createRequest1Settings();
                settings.setEnvironmentClass(EnvironmentClassDO.prod);
                new OrdersRestService().postOrder(settings, true);
            }
        });
    }

    @SuppressWarnings("serial")
    @Test
    public void createWasOrder() throws Exception {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                SettingsDO settings = createRequest2Settings();
                String xml = new OrdersRestService().postOrder(settings, true);
                try {
                    Diff diff = new Diff(new InputStreamReader(getClass().getResourceAsStream("orderv1_wa_request.xml")), new StringReader(xml));
                    diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
                    assertXMLEqual(diff, true);
                } catch (SAXException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private SettingsDO createRequest1Settings() {
        SettingsDO settings = new SettingsDO();
        settings.setApplicationServerType(ApplicationServerType.jb);
        settings.setEnvironmentName("tpr-u2");
        settings.setServerCount(2);
        settings.setServerSize(ServerSize.s);
        settings.setDisk(false);
        settings.setZone(SettingsDO.Zone.fss);
        settings.setApplicationName("autodeploy-test");
        settings.setEnvironmentClass(EnvironmentClassDO.utv);
        return settings;
    }

    private SettingsDO createRequest2Settings() {
        SettingsDO settings = new SettingsDO();
        settings.setApplicationServerType(ApplicationServerType.wa);
        settings.setEnvironmentName("wastest");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.s);
        settings.setDisk(false);
        settings.setZone(SettingsDO.Zone.fss);
        settings.setApplicationName("wasdeploy-test");
        settings.setEnvironmentClass(EnvironmentClassDO.utv);
        return settings;
    }
}
