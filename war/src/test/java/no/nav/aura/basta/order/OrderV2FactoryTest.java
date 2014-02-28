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
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import no.nav.aura.basta.persistence.BpmProperties;
import no.nav.aura.basta.persistence.DecommissionProperties;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.persistence.ServerSize;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.util.SystemPropertiesTest;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;
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
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@TransactionConfiguration
@Transactional
public class OrderV2FactoryTest extends XMLTestCase {

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasitRestClient;

    @Before
    public void primeXmlUnitAndMockito() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        Mockito.reset(fasitRestClient);
    }

    @Test
    public void createWasOrder() throws Exception {
        ResourceElement deploymentManager = new ResourceElement(ResourceTypeDO.DeploymentManager, "wasDmgr");
        deploymentManager.getProperties().add(new PropertyElement("hostname", "e34jbsl00995.devillo.no"));
        when(fasitRestClient.getResource(anyString(), Mockito.eq("wasDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString()))
                .thenReturn(deploymentManager);
        Order order = new Order(NodeType.APPLICATION_SERVER);
        Settings settings = new Settings(orderRepository.save(order));
        settings.setMiddleWareType(MiddleWareType.wa);
        settings.setEnvironmentName("lars_slett");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.m);
        settings.setDisk(true);
        settings.setZone(Zone.fss);
        settings.setApplicationName("autodeploy-test");
        settings.setEnvironmentClass(EnvironmentClass.u);
        createRequest(settings, "orderv2_was_request.xml");
        verify(fasitRestClient).getResource(anyString(), Mockito.eq("wasDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString());
    }

    @Test
    public void createWasDeplomentManagerOrder() throws Exception {
        Order order = new Order(NodeType.WAS_DEPLOYMENT_MANAGER);
        Settings settings = new Settings(orderRepository.save(order));
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.fss);
        settings.setProperty(BpmProperties.WAS_ADMIN_CREDENTIAL_ALIAS, "wsadminUser");
        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "temmelig hemmelig", 1);
        createRequest(settings, "orderv2_was_deployment_manager_request.xml");
        verifyWasAdminCredential.perform();
    }

    @Test
    public void createBpmDeploymentManagerOrder() throws Exception {
        Order order = new Order(NodeType.BPM_DEPLOYMENT_MANAGER);
        Settings settings = new Settings(orderRepository.save(order));
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.fss);
        settings.setProperty(BpmProperties.BPM_COMMON_DATASOURCE_ALIAS, "bpmCommonDatasource");
        settings.setProperty(BpmProperties.BPM_CELL_DATASOURCE_ALIAS, "bpmCellDatasource");
        settings.setProperty(BpmProperties.WAS_ADMIN_CREDENTIAL_ALIAS, "wsadminUser");
        settings.setProperty(BpmProperties.BPM_SERVICE_CREDENTIAL_ALIAS, "servicebrukerFraFasitBarePaaLat");
        Effect verifyCommonDataSource = prepareDatasource("bpmCommonDatasource", "jdbc:h3:db", "kjempehemmelig", 1);
        ResourceElement cellDatasource = new ResourceElement(ResourceTypeDO.DataSource, "bpmDatabase");
        cellDatasource.addProperty(new PropertyElement("url", "jdbc:h3:db"));
        Effect verifyCellDataSource = prepareDatasource("bpmCellDatasource", "jdbc:h3:db", "superhemmelig", 1);
        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "nokså hemmelig", 1);
        Effect verifyBpmServiceCredential = prepareCredential("servicebrukerFraFasitBarePaaLat", "navn", "ganske hemmelig", 1);
        createRequest(settings, "orderv2_bpm_deployment_manager_request.xml");
        verifyCommonDataSource.perform();
        verifyCellDataSource.perform();
        verifyWasAdminCredential.perform();
        verifyBpmServiceCredential.perform();
    }

    private Effect prepareCredential(String resourceAlias, String username, String secret, int calls) throws URISyntaxException {
        final URI secretUri = new URI("http://der/" + UUID.randomUUID().toString());
        ResourceElement datasource = new ResourceElement(ResourceTypeDO.DataSource, resourceAlias);
        datasource.addProperty(new PropertyElement("username", username));
        datasource.addProperty(new PropertyElement("password", secretUri, Type.SECRET));
        return prepareResource(resourceAlias, secret, calls, datasource, secretUri, ResourceTypeDO.Credential);
    }

    private Effect prepareDatasource(final String resourceAlias, String url, final String secret, final int calls) throws URISyntaxException {
        final URI secretUri = new URI("http://der/" + UUID.randomUUID().toString());
        ResourceElement datasource = new ResourceElement(ResourceTypeDO.DataSource, resourceAlias);
        datasource.addProperty(new PropertyElement("url", url));
        datasource.addProperty(new PropertyElement("password", secretUri, Type.SECRET));
        return prepareResource(resourceAlias, secret, calls, datasource, secretUri, ResourceTypeDO.DataSource);
    }

    @SuppressWarnings("serial")
    private Effect prepareResource(final String resourceAlias, final String secret, final int calls, ResourceElement resource, final URI secretUri, final ResourceTypeDO resourceType) {
        for (int i = 0; i < calls; ++i) {
            when(fasitRestClient.getResource(anyString(), Mockito.eq(resourceAlias), Mockito.eq(resourceType), Mockito.<DomainDO> any(), anyString()))
                    .thenReturn(resource);
            if (secret != null) {
                when(fasitRestClient.getSecret(secretUri)).thenReturn(secret);
            }
        }
        return new Effect() {
            public void perform() {
                verify(fasitRestClient, times(calls)).getResource(anyString(), Mockito.eq(resourceAlias), Mockito.eq(resourceType), Mockito.<DomainDO> any(), anyString());
                if (secret != null) {
                    verify(fasitRestClient, times(calls)).getSecret(secretUri);
                }
            }
        };
    }

    @Test
    public void createBpmNodes() throws Exception {
        Order order = new Order(NodeType.BPM_NODES);
        Settings settings = new Settings(orderRepository.save(order));
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.fss);
        settings.setServerSize(ServerSize.l);
        settings.setProperty(BpmProperties.BPM_COMMON_DATASOURCE_ALIAS, "bpmCommonDatasource");
        settings.setProperty(BpmProperties.BPM_SERVICE_CREDENTIAL_ALIAS, "servicebrukerFraFasitBarePaaLat");
        ResourceElement deploymentManager = new ResourceElement(ResourceTypeDO.DeploymentManager, "bpmDmgr");
        deploymentManager.getProperties().add(new PropertyElement("hostname", "e34jbsl00995.devillo.no"));
        when(fasitRestClient.getResource(anyString(), Mockito.eq("bpmDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString()))
                .thenReturn(deploymentManager);

        Effect verifyBpmServiceCredential = prepareCredential("servicebrukerFraFasitBarePaaLat", "brukernavn", "temmelig hemmelig", 2);
        Effect verifyCommonDataSource = prepareDatasource("bpmCommonDatasource", "jdbc:h3:db", null, 2);
        createRequest(settings, "orderv2_bpm_nodes_request.xml");
        verify(fasitRestClient, times(2)).getResource(anyString(), Mockito.eq("bpmDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString());
        verifyCommonDataSource.perform();
        verifyBpmServiceCredential.perform();
    }

    @Test
    public void createJbossOrder() throws Exception {
        createRequest(createRequestJbossSettings(), "orderv2_jboss_request.xml");
    }

    @SuppressWarnings("serial")
    @Test
    public void createJbossOrderFromU() throws Exception {
        SystemPropertiesTest.doWithProperty("environment.class", "u", new Effect() {
            public void perform() {
                createRequest(createRequestJbossSettings(), "orderv2_jboss_request_from_u.xml");
            }
        });
    }

    @Test
    public void createPlainLinux() throws Exception {
        Settings settings = new Settings(orderRepository.save(new Order(NodeType.PLAIN_LINUX)));
        settings.setEnvironmentClass(EnvironmentClass.u);
        settings.setZone(Zone.fss);
        settings.setServerSize(ServerSize.m);
        createRequest(settings, "orderv2_plain_linux_request.xml");
    }

    @SuppressWarnings("serial")
    private void createRequest(final Settings settings, final String expectXml) {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                try {
                    OrchestatorRequest request = createRequest(settings);
                    String xml = XmlUtils.prettyFormat(XmlUtils.generateXml(request), 2);
                     System.out.println("### xml: " + xml);
                    Diff diff = new Diff(new InputSource(getClass().getResourceAsStream(expectXml)), new InputSource(new StringReader(xml)));
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
                        ProvisionRequest request = (ProvisionRequest) createRequest(settings);
                        if (environmentClass == EnvironmentClass.p || (multisite && environmentClass == EnvironmentClass.q)) {
                            assertThat(request.getvApps().size(), is(2));
                            assertThat(request.getvApps(), containsInAnyOrder(
                                    hasProperty("site", equalTo(Site.u89)),
                                    hasProperty("site", equalTo(Site.so8))));
                        } else {
                            assertThat(request.getvApps().size(), is(1));
                            assertThat(request.getvApps().get(0).getSite(), equalTo(Site.so8));
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
                    ProvisionRequest request = (ProvisionRequest) createRequest(settings);
                    assertThat(request.getChangeDeployerPassword(), equalTo(environmentClass != EnvironmentClass.u));
                }
            }
        });
    }

    @Test
    public void createDecommissionOrder() {
        Settings settings = new Settings(new Order(NodeType.DECOMMISSIONING));
        settings.setProperty(DecommissionProperties.DECOMMISSION_HOSTS_PROPERTY_KEY, " ,  host1.devillo.no , host2.devillo.no, host3,   ");
        createRequest(settings, "orderv2_decommission_request.xml");
    }

    private URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private OrchestatorRequest createRequest(Settings settings) {
        return new OrderV2Factory(settings, "admin", createURI("http://thisisbasta/orders/vm"), createURI("http://thisisbasta/orders/results"), fasitRestClient).createOrder();
    }

    public static Settings createRequestJbossSettings() {
        Order order = new Order(NodeType.APPLICATION_SERVER);
        Settings settings = new Settings(order);
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
