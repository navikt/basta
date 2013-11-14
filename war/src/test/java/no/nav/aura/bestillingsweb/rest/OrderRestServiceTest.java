package no.nav.aura.bestillingsweb.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.inject.Inject;

import no.nav.aura.bestillingsweb.SpringUnitTestConfig;
import no.nav.aura.bestillingsweb.rest.SettingsDO.ServerSize;
import no.nav.aura.bestillingsweb.util.Effect;
import no.nav.aura.bestillingsweb.util.SpringRunAs;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public class OrderRestServiceTest extends XMLTestCase {

    @Inject
    private AuthenticationManager authenticationManager;

    @SuppressWarnings("serial")
    @Test
    public void test() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                SettingsDO settings = new SettingsDO();
                settings.setEnvironmentName("tpr-u2");
                settings.setServerCount(2);
                settings.setServerSize(ServerSize.s);
                settings.setDisk(false);
                settings.setZone(SettingsDO.Zone.fss);
                settings.setApplicationName("autodeploy-test");
                settings.setEnvironmentClass("utv");
                String xml = new OrdersRestService().postOrder(settings, true);
                try {
                    Diff diff = new Diff(new InputStreamReader(getClass().getResourceAsStream("request.xml")), new StringReader(xml));
                    diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
                    assertXMLEqual(diff, true);
                } catch (SAXException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
