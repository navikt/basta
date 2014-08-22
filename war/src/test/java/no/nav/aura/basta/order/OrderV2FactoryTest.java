package no.nav.aura.basta.order;

import com.google.common.collect.Lists;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.rest.ApplicationMapping;
import no.nav.aura.basta.rest.OrderDetailsDO;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.util.SystemPropertiesTest;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.DecomissionRequest;
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

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
        Order order = Order.newProvisionOrder(NodeType.WAS_NODES);
        Settings settings = new Settings();
        settings.setMiddleWareType(MiddleWareType.wa);
        settings.setEnvironmentName("t5");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.m);
        settings.setZone(Zone.fss);
        settings.setApplicationMappingName("autodeploy-test");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.addDisk();
        settings.setProperty(FasitProperties.WAS_ADMIN_CREDENTIAL_ALIAS, "wsadminUser");
        order.setSettings(settings);
        orderRepository.save(order);

        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "temmelig hemmelig", 1);
        settings.setProperty(FasitProperties.LDAP_USER_CREDENTIAL_ALIAS, "theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 1);
        assertRequestXML(createRequest(order), "orderv2_was_request.xml");
        assertThat(settings.getDisks(), is(2));
        verify(fasitRestClient).getResource(anyString(), Mockito.eq("wasDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString());
        verifyWasAdminCredential.perform();
        verifyLDAPCredential.perform();
    }

    @Test
    public void createWasDeploymentManagerOrder() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.WAS_DEPLOYMENT_MANAGER);
        Settings settings = new Settings();
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.fss);
        settings.setProperty(FasitProperties.WAS_ADMIN_CREDENTIAL_ALIAS, "wsadminUser");
        order.setSettings(settings);
        orderRepository.save(order);

        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "temmelig hemmelig", 1);
        settings.setProperty(FasitProperties.LDAP_USER_CREDENTIAL_ALIAS, "theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 1);

        assertRequestXML(createRequest(order), "orderv2_was_deployment_manager_request.xml");
        verifyWasAdminCredential.perform();
        verifyLDAPCredential.perform();
        assertThat(settings.getDisks(), is(1));
    }

    @Test
    public void createDeploymentManagerOrderWithExtraCredentialsForSTSBecauseSBS_thehorror() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.WAS_DEPLOYMENT_MANAGER);
        Settings settings = new Settings();
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.sbs);
        settings.setProperty(FasitProperties.WAS_ADMIN_CREDENTIAL_ALIAS, "wsadminUser");
        order.setSettings(settings);
        orderRepository.save(order);

        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "temmelig hemmelig", 1, DomainDO.OeraT);
        settings.setProperty(FasitProperties.LDAP_USER_CREDENTIAL_ALIAS, "theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 1, DomainDO.OeraT);
        Effect verifyLDAPCredentialFSS = prepareCredential("theldapAliasBarePaaLat", "navnFSS", "utrolig hemmelig FSS", 1, DomainDO.TestLocal);
        assertRequestXML(createRequest(order), "orderv2_was_deployment_manager_request_sbs_zone.xml");
        verifyWasAdminCredential.perform();
        verifyLDAPCredential.perform();
        verifyLDAPCredentialFSS.perform();
        assertThat(settings.getDisks(), is(1));
    }

    @Test
    public void createBpmDeploymentManagerOrder() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.BPM_DEPLOYMENT_MANAGER);
        Settings settings = new Settings();
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.fss);
        settings.setProperty(FasitProperties.BPM_COMMON_DATASOURCE_ALIAS, "bpmCommonDatasource");
        settings.setProperty(FasitProperties.BPM_CELL_DATASOURCE_ALIAS, "bpmCellDatasource");
        settings.setProperty(FasitProperties.WAS_ADMIN_CREDENTIAL_ALIAS, "wsadminUser");
        settings.setProperty(FasitProperties.BPM_SERVICE_CREDENTIAL_ALIAS, "servicebrukerFraFasitBarePaaLat");
        order.setSettings(settings);
        orderRepository.save(order);

        Effect verifyCommonDataSource = prepareDatasource("bpmCommonDatasource", "jdbc:h3:db", "kjempehemmelig", 1);
        ResourceElement cellDatasource = new ResourceElement(ResourceTypeDO.DataSource, "bpmDatabase");
        cellDatasource.addProperty(new PropertyElement("url", "jdbc:h3:db"));
        Effect verifyCellDataSource = prepareDatasource("bpmCellDatasource", "jdbc:h3:db", "superhemmelig", 1);
        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "passe hemmelig", 1);
        Effect verifyBpmServiceCredential = prepareCredential("servicebrukerFraFasitBarePaaLat", "navn", "ganske hemmelig", 1);
        settings.setProperty(FasitProperties.LDAP_USER_CREDENTIAL_ALIAS, "theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 1);
        assertRequestXML(createRequest(order), "orderv2_bpm_deployment_manager_request.xml");
        verifyCommonDataSource.perform();
        verifyCellDataSource.perform();
        verifyWasAdminCredential.perform();
        verifyBpmServiceCredential.perform();
        verifyLDAPCredential.perform();
        assertThat(settings.getDisks(), is(1));
    }

    private Effect prepareCredential(String resourceAlias, String username, String secret, int calls) throws URISyntaxException {
        return prepareCredential(resourceAlias, username, secret, calls, DomainDO.TestLocal);
    }

    private Effect prepareCredential(String resourceAlias, String username, String secret, int calls, DomainDO domain) throws URISyntaxException {
        final URI secretUri = new URI("http://der/" + UUID.randomUUID().toString());
        ResourceElement datasource = new ResourceElement(ResourceTypeDO.DataSource, resourceAlias);
        datasource.addProperty(new PropertyElement("username", username));
        datasource.addProperty(new PropertyElement("password", secretUri, Type.SECRET));
        return prepareResource(resourceAlias, secret, calls, datasource, secretUri, ResourceTypeDO.Credential, domain);
    }

    private Effect prepareDatasource(final String resourceAlias, String url, final String secret, final int calls) throws URISyntaxException {
        final URI secretUri = new URI("http://der/" + UUID.randomUUID().toString());
        ResourceElement datasource = new ResourceElement(ResourceTypeDO.DataSource, resourceAlias);
        datasource.addProperty(new PropertyElement("url", url));
        datasource.addProperty(new PropertyElement("password", secretUri, Type.SECRET));
        return prepareResource(resourceAlias, secret, calls, datasource, secretUri, ResourceTypeDO.DataSource, DomainDO.TestLocal);
    }

    @SuppressWarnings("serial")
    private Effect prepareResource(final String resourceAlias, final String secret, final int calls, ResourceElement resource, final URI secretUri, final ResourceTypeDO resourceType, final DomainDO domain) {
        for (int i = 0; i < calls; ++i) {
            when(fasitRestClient.getResource(anyString(), Mockito.eq(resourceAlias), Mockito.eq(resourceType), Mockito.eq(domain), anyString()))
                    .thenReturn(resource);
            if (secret != null) {
                when(fasitRestClient.getSecret(secretUri)).thenReturn(secret);
            }
        }
        return new Effect() {
            public void perform() {
                verify(fasitRestClient, times(calls)).getResource(anyString(), Mockito.eq(resourceAlias), Mockito.eq(resourceType), Mockito.eq(domain), anyString());
                if (secret != null) {
                    verify(fasitRestClient, times(calls)).getSecret(secretUri);
                }
            }
        };
    }

    @Test
    public void createBpmNodes() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.BPM_NODES);
        Settings settings = new Settings();
        settings.setEnvironmentName("t5");
        settings.setEnvironmentClass(EnvironmentClass.t);
        settings.setZone(Zone.fss);
        settings.setServerSize(ServerSize.l);
        settings.setServerCount(2);
        settings.setProperty(FasitProperties.BPM_COMMON_DATASOURCE_ALIAS, "bpmCommonDatasource");
        settings.setProperty(FasitProperties.BPM_FAILOVER_DATASOURCE_ALIAS, "bpmFailoverDb");
        settings.setProperty(FasitProperties.BPM_SERVICE_CREDENTIAL_ALIAS, "servicebrukerFraFasitBarePaaLat");
        settings.setProperty(FasitProperties.WAS_ADMIN_CREDENTIAL_ALIAS, "wsadminUser");
        order.setSettings(settings);
        orderRepository.save(order);

        ResourceElement deploymentManager = new ResourceElement(ResourceTypeDO.DeploymentManager, "bpmDmgr");
        deploymentManager.getProperties().add(new PropertyElement("hostname", "e34jbsl00995.devillo.no"));
        when(fasitRestClient.getResource(anyString(), Mockito.eq("bpmDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString()))
                .thenReturn(deploymentManager);

        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "temmelig hemmelig", 2);
        Effect verifyBpmServiceCredential = prepareCredential("servicebrukerFraFasitBarePaaLat", "brukernavn", "temmelig hemmelig", 2);
        settings.setProperty(FasitProperties.LDAP_USER_CREDENTIAL_ALIAS, "theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 2);

        Effect verifyCommonDataSource = prepareDatasource("bpmCommonDatasource", "jdbc:h3:db", null, 2);
        Effect verifyFailoverDataSource = prepareDatasource("bpmFailoverDb", "jdbc:h3:db", null, 2);
        assertRequestXML(createRequest(order), "orderv2_bpm_nodes_request.xml");
        verify(fasitRestClient, times(2)).getResource(anyString(), Mockito.eq("bpmDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString());
        verifyCommonDataSource.perform();
        verifyFailoverDataSource.perform();
        verifyBpmServiceCredential.perform();
        verifyWasAdminCredential.perform();
        verifyLDAPCredential.perform();
        assertThat(settings.getDisks(), is(1));
    }

    @Test
    public void createJbossOrder() throws Exception {
        Order order = createRequestJbossSettings();
        assertRequestXML(createRequest(order), "orderv2_jboss_request.xml");
        assertThat(order.getSettings().getDisks(), is(0));
    }

    @Test
    public void createJbossOrderForApplicationGroup() throws Exception {
        assertRequestXML(createApplicationGroupRequest(), "orderv2_applicationgroup_request.xml");
        System.out.println("");
    }

    @SuppressWarnings("serial")
    @Test
    public void createJbossOrderFromU() throws Exception {
        SystemPropertiesTest.doWithProperty("environment.class", "u", new Effect() {
            public void perform() {
                Order order = createRequestJbossSettings();
                order.getSettings().addDisk();
                assertRequestXML(createRequest(order), "orderv2_jboss_request_from_u.xml");
                assertThat(order.getSettings().getDisks(), is(1));
            }
        });
    }

    @Test
    public void createPlainLinux() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.PLAIN_LINUX);
        Settings settings = new Settings();
        settings.setEnvironmentClass(EnvironmentClass.u);
        settings.setZone(Zone.fss);
        settings.setServerSize(ServerSize.m);
        order.setSettings(settings);
        orderRepository.save(order);
        assertRequestXML(createRequest(order), "orderv2_plain_linux_request.xml");
    }



    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void createMultisite() {
        SpringRunAs.runAs(authenticationManager, "user_operations", "admin", new Effect() {
            public void perform() {
                for (EnvironmentClass environmentClass : EnvironmentClass.values()) {
                    for (Boolean multisite : Lists.newArrayList(true, false)) {
                        Order order = createRequestJbossSettings();
                        Settings settings = order.getSettings();
                        if (multisite) {
                            settings.setEnvironmentName("q3");
                        } else {
                            settings.setEnvironmentName("q2");
                        }
                        settings.setEnvironmentClass(environmentClass);
                        ProvisionRequest request = (ProvisionRequest) createRequest(order);
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
        SpringRunAs.runAs(authenticationManager, "user_operations", "admin", new Effect() {
            public void perform() {
                for (EnvironmentClass environmentClass : EnvironmentClass.values()) {
                    Order order = createRequestJbossSettings();
                    order.getSettings().setEnvironmentClass(environmentClass);
                    ProvisionRequest request = (ProvisionRequest) createRequest(order);
                    assertThat(request.getChangeDeployerPassword(), equalTo(environmentClass != EnvironmentClass.u));
                }
            }
        });
    }

    @Test
    public void createDecommissionOrder() {
        Order order = Order.newDecommissionOrder("host1.devillo.no" , "host2.devillo.no", "host3");
        orderRepository.save(order);
        DecomissionRequest request = new DecomissionRequest(order.getSettings().getHostNames(),
                                                            createURI("http://thisisbasta/orders/decommission"),
                                                            createURI("http://thisisbasta/orders/results"));
        assertRequestXML(request, "orderv2_decommission_request.xml");
    }


    @SuppressWarnings("serial")
    private void assertRequestXML(final OrchestatorRequest request, final String expectXml) {
        SpringRunAs.runAs(authenticationManager, "user_operations", "admin", new Effect() {
            public void perform() {
                try {
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

    private URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private OrchestatorRequest createRequest(Order order) {
        return new OrderV2Factory(order, "admin", createURI("http://thisisbasta/orders/vm"), createURI("http://thisisbasta/orders/results"), fasitRestClient)
                .createProvisionOrder();
    }

    public static Order createRequestJbossSettings() {
        Order order = Order.newProvisionOrder(NodeType.APPLICATION_SERVER);
        Settings settings = new Settings();
        settings.setMiddleWareType(MiddleWareType.jb);
        settings.setEnvironmentName("lars_slett");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.s);
        settings.setZone(Zone.fss);
        settings.setApplicationMappingName("autodeploy-test");
        settings.setEnvironmentClass(EnvironmentClass.u);
        settings.setDisks(0);
        order.setSettings(settings);

        return order;
    }

    private OrchestatorRequest createApplicationGroupRequest() {
        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setNodeType(NodeType.APPLICATION_SERVER);
        orderDetails.setApplicationMapping(new ApplicationMapping("myAppGrp", Lists.newArrayList("myApp1", "myApp2")));
        orderDetails.setMiddleWareType(MiddleWareType.jb);
        orderDetails.setEnvironmentClass(EnvironmentClass.u);
        orderDetails.setEnvironmentName("mydevenv");
        orderDetails.setServerCount(1);
        orderDetails.setServerSize(ServerSize.s);
        orderDetails.setZone(Zone.fss);
        Order order = Order.newProvisionOrder(NodeType.APPLICATION_SERVER);
        order.setSettings(new Settings(orderDetails));
        return createRequest(order);
    }

}
