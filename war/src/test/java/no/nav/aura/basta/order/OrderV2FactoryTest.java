package no.nav.aura.basta.order;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import no.nav.aura.basta.persistence.BpmProperties;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.ServerSize;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.PropertyElement.Type;
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

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public class OrderV2FactoryTest extends XMLTestCase {

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private FasitRestClient fasitRestClient;

    @Before
    public void primeXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void createWasOrder() throws Exception {
        ResourceElement deploymentManager = new ResourceElement(ResourceTypeDO.DeploymentManager, "wasDmgr");
        deploymentManager.getProperties().add(new PropertyElement("hostname", "e34jbsl00995.devillo.no"));
        when(fasitRestClient.getResource(anyString(), Mockito.eq("wasDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString()))
                .thenReturn(deploymentManager);
        Settings settings = new Settings();
        settings.setNodeType(NodeType.APPLICATION_SERVER);
        settings.setMiddleWareType(MiddleWareType.wa);
        settings.setEnvironmentName("lars_slett");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.m);
        settings.setDisk(true);
        settings.setZone(Zone.fss);
        settings.setApplicationName("autodeploy-test");
        settings.setEnvironmentClass(EnvironmentClass.u);
        createOrder(settings, "orderv2_was_request.xml");
        verify(fasitRestClient).getResource(anyString(), Mockito.eq("wasDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString());
    }

    @Test
    public void createWasDeplomentManagerOrder() throws Exception {
        Settings settings = new Settings();
        settings.setNodeType(NodeType.WAS_DEPLOYMENT_MANAGER);
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.fss);
        createOrder(settings, "orderv2_was_deployment_manager_request.xml");
    }

    @Test
    public void createBpmDeploymentManagerOrder() throws Exception {
        Settings settings = new Settings();
        settings.setNodeType(NodeType.BPM_DEPLOYMENT_MANAGER);
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.fss);
        settings.setProperty(BpmProperties.BPM_COMMON_DATASOURCE_ALIAS, "bpmCommonDatasource");
        settings.setProperty(BpmProperties.BPM_CELL_DATASOURCE_ALIAS, "bpmCellDatasource");
        ResourceElement commonDatasource = new ResourceElement(ResourceTypeDO.DataSource, "bpmDatabase");
        commonDatasource.addProperty(new PropertyElement("url", "jdbc:h3:db"));
        URI kjempehemmeligUri = new URI("http://der/kjempehemmelig");
        commonDatasource.addProperty(new PropertyElement("password", kjempehemmeligUri, Type.SECRET));
        when(fasitRestClient.getResource(anyString(), Mockito.eq("bpmCommonDatasource"), Mockito.eq(ResourceTypeDO.DataSource), Mockito.<DomainDO> any(), anyString()))
                .thenReturn(commonDatasource);
        when(fasitRestClient.getSecret(kjempehemmeligUri)).thenReturn("kjempehemmelig");
        ResourceElement cellDatasource = new ResourceElement(ResourceTypeDO.DataSource, "bpmDatabase");
        cellDatasource.addProperty(new PropertyElement("url", "jdbc:h3:db"));
        URI superhemmeligUri = new URI("http://her/superhemmelig");
        cellDatasource.addProperty(new PropertyElement("password", superhemmeligUri, Type.SECRET));
        when(fasitRestClient.getResource(anyString(), Mockito.eq("bpmCellDatasource"), Mockito.eq(ResourceTypeDO.DataSource), Mockito.<DomainDO> any(), anyString()))
                .thenReturn(cellDatasource);
        when(fasitRestClient.getSecret(superhemmeligUri)).thenReturn("superhemmelig");
        createOrder(settings, "orderv2_bpm_deployment_manager_request.xml");
    }

    @Test
    public void createBpmNodes() throws Exception {
        Settings settings = new Settings();
        settings.setNodeType(NodeType.BPM_NODES);
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.fss);
        settings.setServerSize(ServerSize.l);
        ResourceElement deploymentManager = new ResourceElement(ResourceTypeDO.DeploymentManager, "bpmDmgr");
        deploymentManager.getProperties().add(new PropertyElement("hostname", "e34jbsl00995.devillo.no"));
        when(fasitRestClient.getResource(anyString(), Mockito.eq("bpmDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString()))
                .thenReturn(deploymentManager);
        createOrder(settings, "orderv2_bpm_nodes_request.xml");
        verify(fasitRestClient, times(2)).getResource(anyString(), Mockito.eq("bpmDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString());
    }

    @Test
    public void createJbossOrder() throws Exception {
        createOrder(createRequestJbossSettings(), "orderv2_jboss_request.xml");
    }

    @Test
    public void createPlainLinux() throws Exception {
        Settings settings = new Settings();
        settings.setNodeType(NodeType.PLAIN_LINUX);
        settings.setEnvironmentClass(EnvironmentClass.u);
        settings.setZone(Zone.fss);
        settings.setServerSize(ServerSize.m);
        createOrder(settings, "orderv2_plain_linux_request.xml");
    }

    @SuppressWarnings("serial")
    private void createOrder(final Settings settings, final String expectXml) {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                try {
                    ProvisionRequest order = createOrder(settings);
                    String xml = XmlUtils.prettyFormat(XmlUtils.generateXml(order), 2);
                    // System.out.println("### xml: " + xml);
                    Diff diff = new Diff(new InputStreamReader(getClass().getResourceAsStream(expectXml)), new StringReader(xml));
                    diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
                    assertXMLEqual(diff, true);
                } catch (JAXBException | SAXException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void createMultisite() {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                for (EnvironmentClass environmentClass : EnvironmentClass.values()) {
                    for (Boolean multisite : Lists.newArrayList(true, false)) {
                        Settings settings = createRequestJbossSettings();
                        if (multisite) {
                            settings.setEnvironmentName("q3");
                        } else {
                            settings.setEnvironmentName("q2");
                        }
                        settings.setEnvironmentClass(environmentClass);
                        ProvisionRequest order = createOrder(settings);
                        if (environmentClass == EnvironmentClass.p || (multisite && environmentClass == EnvironmentClass.q)) {
                            assertThat(order.getvApps().size(), is(2));
                            assertThat(order.getvApps(), containsInAnyOrder(
                                    hasProperty("site", equalTo(Site.u89)),
                                    hasProperty("site", equalTo(Site.so8))));
                        } else {
                            assertThat(order.getvApps().size(), is(1));
                            assertThat(order.getvApps().get(0).getSite(), equalTo(Site.so8));
                        }
                    }
                }
            }
        });
    }

    @SuppressWarnings({ "serial" })
    @Test
    public void createWithNewDeployerPassword() {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                for (EnvironmentClass environmentClass : EnvironmentClass.values()) {
                    Settings settings = createRequestJbossSettings();
                    settings.setEnvironmentClass(environmentClass);
                    assertThat(createOrder(settings).getChangeDeployerPassword(), equalTo(environmentClass != EnvironmentClass.u));
                }
            }
        });
    }

    private URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private ProvisionRequest createOrder(Settings settings) {
        return new OrderV2Factory(settings, "admin", createURI("http://thisisbasta/orders/vm"), createURI("http://thisisbasta/orders/results"), fasitRestClient).createOrder();
    }

    public static Settings createRequestJbossSettings() {
        Settings settings = new Settings();
        settings.setNodeType(NodeType.APPLICATION_SERVER);
        settings.setMiddleWareType(MiddleWareType.jb);
        settings.setEnvironmentName("lars_slett");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.s);
        settings.setDisk(false);
        settings.setZone(Zone.fss);
        settings.setApplicationName("autodeploy-test");
        settings.setEnvironmentClass(EnvironmentClass.u);
        return settings;
    }

}
