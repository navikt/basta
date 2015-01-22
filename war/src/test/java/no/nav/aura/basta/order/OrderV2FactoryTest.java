package no.nav.aura.basta.order;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import no.nav.aura.basta.domain.FasitProperties;
import no.nav.aura.basta.domain.Input;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.vminput.HostnamesInputResolver;
import no.nav.aura.basta.domain.vminput.VMOrderInputResolver;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.util.SystemPropertiesTest;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.*;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.PropertyElement.Type;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
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
        Order order = Order.newProvisionOrder(NodeType.WAS_NODES);
        Input input = new Input(new HashMap());
        VMOrderInputResolver resolver = new VMOrderInputResolver(input);
        resolver.setMiddleWareType(MiddleWareType.wa);
        resolver.setEnvironmentName("t5");
        resolver.setServerCount(1);
        resolver.setServerSize(ServerSize.m);
        resolver.setZone(Zone.fss);
        resolver.setApplicationMappingName("autodeploy-test");
        resolver.setEnvironmentClass(EnvironmentClass.t);
        resolver.addDisk();
        resolver.setWasAdminCredential("wsadminUser");
        order.setInput(input);
        orderRepository.save(order);

        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "temmelig hemmelig", 1);
        resolver.setLdapUserCredential("theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 1);
        assertRequestXML(createRequest(order), "orderv2_was_request.xml");
        assertThat(resolver.getDisks(), is(2));
        verify(fasitRestClient).getResource(anyString(), Mockito.eq("wasDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString());
        verifyWasAdminCredential.perform();
        verifyLDAPCredential.perform();
    }

    @Test
    public void createWasDeploymentManagerOrder() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.WAS_DEPLOYMENT_MANAGER);
        Input input = new Input(new HashMap());
        VMOrderInputResolver resolver = new VMOrderInputResolver(input);

        resolver.setEnvironmentName("t5");
        resolver.setEnvironmentClass(EnvironmentClass.t);
        resolver.setZone(Zone.fss);
        resolver.setWasAdminCredential("wsadminUser");
        order.setInput(input);
        orderRepository.save(order);

        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "temmelig hemmelig", 1);
        resolver.setLdapUserCredential("theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 1);

        assertRequestXML(createRequest(order), "orderv2_was_deployment_manager_request.xml");
        verifyWasAdminCredential.perform();
        verifyLDAPCredential.perform();
        assertThat(resolver.getDisks(), is(1));
    }

    @Test
    public void createDeploymentManagerOrderWithExtraCredentialsForSTSBecauseSBS_thehorror() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.WAS_DEPLOYMENT_MANAGER);
        Input input = new Input(new HashMap());
        VMOrderInputResolver resolver = new VMOrderInputResolver(input);
        resolver.setEnvironmentName("t5");
        resolver.setEnvironmentClass(EnvironmentClass.t);
        resolver.setZone(Zone.sbs);
        resolver.setWasAdminCredential("wsadminUser");
        order.setInput(input);
        orderRepository.save(order);

        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "temmelig hemmelig", 1, DomainDO.OeraT);
        resolver.setLdapUserCredential("theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 1, DomainDO.OeraT);
        Effect verifyLDAPCredentialFSS = prepareCredential("theldapAliasBarePaaLat", "navnFSS", "utrolig hemmelig FSS", 1, DomainDO.TestLocal);
        assertRequestXML(createRequest(order), "orderv2_was_deployment_manager_request_sbs_zone.xml");
        verifyWasAdminCredential.perform();
        verifyLDAPCredential.perform();
        verifyLDAPCredentialFSS.perform();
        assertThat(resolver.getDisks(), is(1));
    }

    @Test
    public void createBpmDeploymentManagerOrder() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.BPM_DEPLOYMENT_MANAGER);
        Input input = new Input(new HashMap());
        VMOrderInputResolver resolver = new VMOrderInputResolver(input);
        resolver.setEnvironmentName("t5");
        resolver.setEnvironmentClass(EnvironmentClass.t);
        resolver.setZone(Zone.fss);
        resolver.setBpmCommonDatasource("bpmCommonDatasource");
        resolver.setCellDatasource("bpmCellDatasource");
        resolver.setWasAdminCredential("wsadminUser");
        resolver.setBpmServiceCredential("servicebrukerFraFasitBarePaaLat");
        order.setInput(input);
        orderRepository.save(order);

        Effect verifyCommonDataSource = prepareDatasource("bpmCommonDatasource", "jdbc:h3:db", "kjempehemmelig", 1);
        ResourceElement cellDatasource = new ResourceElement(ResourceTypeDO.DataSource, "bpmDatabase");
        cellDatasource.addProperty(new PropertyElement("url", "jdbc:h3:db"));
        Effect verifyCellDataSource = prepareDatasource("bpmCellDatasource", "jdbc:h3:db", "superhemmelig", 1);
        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "passe hemmelig", 1);
        Effect verifyBpmServiceCredential = prepareCredential("servicebrukerFraFasitBarePaaLat", "navn", "ganske hemmelig", 1);
        resolver.setLdapUserCredential("theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 1);
        assertRequestXML(createRequest(order), "orderv2_bpm_deployment_manager_request.xml");
        verifyCommonDataSource.perform();
        verifyCellDataSource.perform();
        verifyWasAdminCredential.perform();
        verifyBpmServiceCredential.perform();
        verifyLDAPCredential.perform();
        assertThat(resolver.getDisks(), is(1));
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
        VMOrderInputResolver input = new VMOrderInputResolver(order.getInput());
        input.setEnvironmentName("t5");
        input.setEnvironmentClass(EnvironmentClass.t);
        input.setZone(Zone.fss);
        input.setServerSize(ServerSize.l);
        input.setServerCount(2);
        input.setBpmCommonDatasource("bpmCommonDatasource");
        input.setBpmFailoverDatasource("bpmFailoverDb");
        input.setBpmRecoveryDatasourceAlias("bpmRecoveryDb");
        input.setBpmServiceCredential("servicebrukerFraFasitBarePaaLat");
        input.setWasAdminCredential("wsadminUser");
        orderRepository.save(order);

        ResourceElement deploymentManager = new ResourceElement(ResourceTypeDO.DeploymentManager, "bpmDmgr");
        deploymentManager.getProperties().add(new PropertyElement("hostname", "e34jbsl00995.devillo.no"));
        when(fasitRestClient.getResource(anyString(), Mockito.eq("bpmDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString()))
                .thenReturn(deploymentManager);

        Effect verifyWasAdminCredential = prepareCredential("wsadminUser", "srvWASLdap", "temmelig hemmelig", 2);
        Effect verifyBpmServiceCredential = prepareCredential("servicebrukerFraFasitBarePaaLat", "brukernavn", "temmelig hemmelig", 2);
        input.setLdapUserCredential("theldapAliasBarePaaLat");
        Effect verifyLDAPCredential = prepareCredential("theldapAliasBarePaaLat", "navn", "utrolig hemmelig", 2);

        Effect verifyCommonDataSource = prepareDatasource("bpmCommonDatasource", "jdbc:h3:db", null, 2);
        Effect verifyFailoverDataSource = prepareDatasource("bpmFailoverDb", "jdbc:h3:db", null, 2);
        Effect verifyRecoveryDataSource = prepareDatasource("bpmRecoveryDb", "jdbc:h3:db", "superhemmelig", 2);
        assertRequestXML(createRequest(order), "orderv2_bpm_nodes_request.xml");
        verify(fasitRestClient, times(2)).getResource(anyString(), Mockito.eq("bpmDmgr"), Mockito.eq(ResourceTypeDO.DeploymentManager), Mockito.<DomainDO> any(), anyString());
        verifyCommonDataSource.perform();
        verifyFailoverDataSource.perform();
        verifyRecoveryDataSource.perform();
        verifyBpmServiceCredential.perform();
        verifyWasAdminCredential.perform();
        verifyLDAPCredential.perform();
        assertThat(input.getDisks(), is(1));
    }

    @Test
    public void createJbossOrder() throws Exception {
        Order order = createRequestJbossSettings();
        assertRequestXML(createRequest(order), "orderv2_jboss_request.xml");
        assertThat(new VMOrderInputResolver(order.getInput()).getDisks(), is(0));
    }

    @SuppressWarnings("serial")
    @Test
    public void createJbossOrderFromU() throws Exception {
        SystemPropertiesTest.doWithProperty("environment.class", "u", new Effect() {
            public void perform() {
                Order order = createRequestJbossSettings();
                VMOrderInputResolver resolver = new VMOrderInputResolver(order.getInput());
                resolver.addDisk();
                assertRequestXML(createRequest(order), "orderv2_jboss_request_from_u.xml");
                assertThat(resolver.getDisks(), is(1));
            }
        });
    }

    @Test
    public void createPlainLinux() throws Exception {
        Order order = Order.newProvisionOrder(NodeType.PLAIN_LINUX);
        Input input = new Input(new HashMap());
        VMOrderInputResolver resolver = new VMOrderInputResolver(input);
        resolver.setEnvironmentClass(EnvironmentClass.u);
        resolver.setZone(Zone.fss);
        resolver.setServerSize(ServerSize.m);
        order.setInput(input);
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
                        VMOrderInputResolver resolver = new VMOrderInputResolver(order.getInput());
                        if (multisite) {
                            resolver.setEnvironmentName("q3");
                        } else {
                            resolver.setEnvironmentName("q2");
                        }
                        resolver.setEnvironmentClass(environmentClass);
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
                    new VMOrderInputResolver(order.getInput()).setEnvironmentClass(environmentClass);
                    ProvisionRequest request = (ProvisionRequest) createRequest(order);
                    assertThat(request.getChangeDeployerPassword(), equalTo(environmentClass != EnvironmentClass.u));
                }
            }
        });
    }

    @Test
    public void createDecommissionOrder() {
        Order order = Order.newDecommissionOrder("host1.devillo.no", "host2.devillo.no", "host3");
        orderRepository.save(order);
        DecomissionRequest request = new DecomissionRequest(HostnamesInputResolver.getHostnames(order.getInput()),
                createURI("http://thisisbasta/orders/decommission"),
                createURI("http://thisisbasta/orders/results"));
        assertRequestXML(request, "orderv2_decommission_request.xml");
    }

    @Test
    public void createStopOrder() {
        Order order = Order.newStopOrder("host1.devillo.no", "host2.devillo.no", "host3");
        orderRepository.save(order);
        StopRequest request = new StopRequest(HostnamesInputResolver.getHostnames(order.getInput()),
                createURI("http://thisisbasta/orders/stop"),
                createURI("http://thisisbasta/orders/results"));
        assertRequestXML(request, "orderv2_stop_request.xml");
    }

    @Test
    public void createStartOrder() {
        Order order = Order.newStartOrder("host1.devillo.no", "host2.devillo.no", "host3");
        orderRepository.save(order);
        StartRequest request = new StartRequest(HostnamesInputResolver.getHostnames(order.getInput()),
                createURI("http://thisisbasta/orders/start"),
                createURI("http://thisisbasta/orders/results"));
        assertRequestXML(request, "orderv2_start_request.xml");
    }

    @SuppressWarnings("serial")
    private void assertRequestXML(final OrchestatorRequest request, final String expectXml) {
        SpringRunAs.runAs(authenticationManager, "user_operations", "admin", new Effect() {
            public void perform() {
                try {
                    String xml = XmlUtils.prettyFormat(XmlUtils.generateXml(request), 2);
                    System.out.println("### xml: " + xml);

                    Diff diff = new Diff(new InputSource(getClass().getResourceAsStream(expectXml)), new InputSource(new StringReader(xml)));
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
        Input input = order.getInput();
        VMOrderInputResolver resolver = new VMOrderInputResolver(input);
        resolver.setMiddleWareType(MiddleWareType.jb);
        resolver.setEnvironmentName("lars_slett");
        resolver.setServerCount(1);
        resolver.setServerSize(ServerSize.s);
        resolver.setZone(Zone.fss);
        resolver.setApplicationMappingName("autodeploy-test");
        resolver.setEnvironmentClass(EnvironmentClass.u);
        order.setInput(input);

        return order;
    }
}
