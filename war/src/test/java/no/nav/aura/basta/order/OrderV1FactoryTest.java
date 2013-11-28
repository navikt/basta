package no.nav.aura.basta.order;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

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
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.requestv1.ProvisionRequest;

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

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public class OrderV1FactoryTest extends XMLTestCase {

    @Inject
    private AuthenticationManager authenticationManager;

    @Before
    public void primeXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void createJbossOrder() throws Exception {
        createOrder(createRequest1Settings(), "orderv1_jb_request.xml");
    }

    @SuppressWarnings("serial")
    private void createOrder(final SettingsDO settings, final String expectXml) {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                try {
                    ProvisionRequest order = new OrderV1Factory(settings, "admin").createOrder();
                    String xml = XmlUtils.prettyFormat(XmlUtils.generateXml(order), 2);
                    Diff diff = new Diff(new InputStreamReader(getClass().getResourceAsStream(expectXml)), new StringReader(xml));
                    diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
                    assertXMLEqual(diff, true);
                } catch (JAXBException | SAXException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test
    public void createWasOrder() throws Exception {
        createOrder(createRequest2Settings(), "orderv1_wa_request.xml");
    }

    @Test
    public void createJbossWithDisk() {
        SettingsDO settings = createRequest1Settings();
        settings.setDisk(true);
        createOrder(settings, "orderv1_jb_with_disk_request.xml");
    }

    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void createMultisite() {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                for (EnvironmentClass environmentClass : EnvironmentClass.values()) {
                    for (Boolean multisite : Lists.newArrayList(true, false)) {
                        SettingsDO settings = createRequest1Settings();
                        settings.setMultisite(multisite);
                        settings.setEnvironmentClass(environmentClass);
                        ProvisionRequest order = new OrderV1Factory(settings, "admin").createOrder();
                        if (environmentClass == EnvironmentClass.p || (multisite && environmentClass == EnvironmentClass.q)) {
                            assertThat(order.getvApps().size(), is(2));
                            assertThat(order.getvApps(), containsInAnyOrder(
                                    hasProperty("site", equalTo(Site.u89.name())),
                                    hasProperty("site", equalTo(Site.so8.name()))));
                        } else {
                            assertThat(order.getvApps().size(), is(1));
                            assertThat(order.getvApps().get(0).getSite(), equalTo(Site.so8.name()));
                        }
                    }
                }
            }
        });
    }

    public static SettingsDO createRequest1Settings() {
        SettingsDO settings = new SettingsDO();
        settings.setApplicationServerType(ApplicationServerType.jb);
        settings.setEnvironmentName("tpr-u2");
        settings.setServerCount(2);
        settings.setServerSize(ServerSize.s);
        settings.setDisk(false);
        settings.setZone(Zone.fss);
        settings.setApplicationName("autodeploy-test");
        settings.setEnvironmentClass(EnvironmentClass.u);
        return settings;
    }

    private SettingsDO createRequest2Settings() {
        SettingsDO settings = new SettingsDO();
        settings.setApplicationServerType(ApplicationServerType.wa);
        settings.setEnvironmentName("wastest");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.s);
        settings.setDisk(false);
        settings.setZone(Zone.fss);
        settings.setApplicationName("wasdeploy-test");
        settings.setEnvironmentClass(EnvironmentClass.u);
        return settings;
    }

}
