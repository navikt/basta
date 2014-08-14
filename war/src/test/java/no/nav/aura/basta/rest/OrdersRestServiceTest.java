package no.nav.aura.basta.rest;

import static org.hamcrest.Matchers.*;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.UnmarshalException;

import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV2FactoryTest;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.request.*;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.OSType;
import no.nav.aura.envconfig.client.ApplicationDO;
import no.nav.aura.envconfig.client.ApplicationGroupDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.xml.sax.SAXParseException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@TransactionConfiguration
@Transactional
public class OrdersRestServiceTest {

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private OrdersRestService ordersRestService;

    @Inject
    private NodeRepository nodeRepository;

    @Inject
    private SettingsRepository settingsRepository;

    @Inject
    private FasitRestClient fasitRestClient;

    @Inject
    private OrchestratorService orchestratorService;

    private Settings defaultSettings;

    @Before
    public void setup() {
        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setApplicationMapping(new ApplicationMapping("myApp"));
        defaultSettings = new Settings(new Order(NodeType.APPLICATION_SERVER), orderDetails);
    }

    @After
    public void resetMockito() {
        Mockito.reset(fasitRestClient, orchestratorService);
    }

    @Test(expected = UnauthorizedException.class)
    public void notAuthorisedOrderPosted() throws Exception {
        orderWithEnvironmentClass(EnvironmentClass.p, false);
    }

    @Test
    public void authorisedOrderPosted() throws Exception {
        orderWithEnvironmentClass(EnvironmentClass.u, true);
    }

    @SuppressWarnings("serial")
    private void orderWithEnvironmentClass(final EnvironmentClass environmentClass, final boolean expectChanges) {
        SpringRunAs.runAs(authenticationManager, "user", "user", new Effect() {
            public void perform() {
                Settings settings = OrderV2FactoryTest.createRequestJbossSettings();
                settings.setEnvironmentClass(environmentClass);
                String orchestratorOrderId = UUID.randomUUID().toString();
                if (expectChanges) {
                    WorkflowToken workflowToken = new WorkflowToken();
                    workflowToken.setId(orchestratorOrderId);
                    when(orchestratorService.send(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);
                }
                ordersRestService.postOrder(new OrderDetailsDO(settings), createUriInfo(), null);
                if (expectChanges) {
                    verify(orchestratorService).send(Mockito.<ProvisionRequest> anyObject());
                    assertThat(orderRepository.findByOrchestratorOrderId(orchestratorOrderId), notNullValue());
                }
            }
        });
    }

    @Test(expected = UnauthorizedException.class)
    public void orderingPlainLinuxAsNormalUser_shouldFail() throws Exception {
        orderPlainLinux("admin", "admin");
    }

    @Test
    public void orderingPlainLinuxAsSuperUser_shouldWork() throws Exception {
        orderPlainLinux("superuser", "superuser");
    }


    @Test
    public void ordering_qa_as_superuser_with_qa_access_in_not_u_should_be_ok() throws Exception {
        System.setProperty("environment.class", "u");
        ordering_using_putXMLOrder("ikt\\qa", 200);
        System.clearProperty("environment.class");
    }

    @Test
    public void ordering_qa_as_superuser_with_qa_access_in_not_u_should_return_400() throws Exception {
          ordering_using_putXMLOrder("ikt\\qa", 400);
    }

    @Test(expected = UnauthorizedException.class)
    public void ordering_prod_as_superuser_without_prod_access_should_fail() throws Exception {
       ordering_using_putXMLOrder("prod",0);
    }

    private void ordering_using_putXMLOrder(final String orchestratorEnvironmentClass, final int expectedStatus){
        SpringRunAs.runAs(authenticationManager, "superuser_without_prod", "superuser2", new Effect() {
            public void perform() {
                Settings settings = OrderV2FactoryTest.createRequestJbossSettings();
                settings.setEnvironmentClass(EnvironmentClass.t);

                WorkflowToken workflowToken = new WorkflowToken();
                workflowToken.setId(UUID.randomUUID().toString());
                when(orchestratorService.send(Mockito.<OrchestatorRequest>anyObject())).thenReturn(workflowToken);
                OrderDO orderDO = ordersRestService.postOrder(new OrderDetailsDO(settings), createUriInfo(), true);

                String requestXML;
                try {
                    ProvisionRequest provisionRequest = XmlUtils.parseAndValidateXmlString(ProvisionRequest.class, orderDO.getRequestXml());
                    provisionRequest.setEnvironmentClass(orchestratorEnvironmentClass);
                    requestXML = XmlUtils.prettyFormat(XmlUtils.generateXml(provisionRequest), 2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Response response = ordersRestService.putXMLOrder(requestXML, orderDO.getId(), createUriInfo());
                assertThat(response.getStatus(), is(expectedStatus));
            }
        });
    }



    @Test
    public void OrderingNodeForApplicationGroup() {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                WorkflowToken workflowToken = new WorkflowToken();
                String orchestratorOrderId = UUID.randomUUID().toString();
                workflowToken.setId(orchestratorOrderId);

                when(orchestratorService.send(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);
                when(fasitRestClient.getApplicationGroup(anyString())).thenReturn(new ApplicationGroupDO("myAppGrp", createApplications()));
                Order order = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
                createApplicationGroupSettings(order);
                ordersRestService.postOrder(new OrderDetailsDO(settingsRepository.findByOrderId(order.getId())), createUriInfo(), null);
                verify(orchestratorService).send(Mockito.<ProvisionRequest> anyObject());
                assertThat(orderRepository.findByOrchestratorOrderId(orchestratorOrderId), notNullValue());
            }
        });
    }

    @Test
    public void createFasitResourceForNodeMappedToApplicationGroup() {
        whenRegisterNodeCalledAddRef();

        Order order = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        createApplicationGroupSettings(order);
        OrchestratorNodeDO vm = new OrchestratorNodeDO();
        vm.setMiddlewareType(MiddleWareType.jb);
        when(fasitRestClient.getApplicationGroup(anyString())).thenReturn(new ApplicationGroupDO("myAppGrp", createApplications()));
        ordersRestService.putVmInformation(order.getId(), vm, mock(HttpServletRequest.class));
        verify(fasitRestClient).registerNode(Mockito.<NodeDO> anyObject(), anyString() );
    }

    @Test
    public void whenOrderIsForApplicationGroup_applicationsInGroupShoulBeGetchedFromFasit() {
        ApplicationGroupDO applicationGroupDO = new ApplicationGroupDO("myAppGrp", createApplications());
        when(fasitRestClient.getApplicationGroup(anyString())).thenReturn(applicationGroupDO);
        Order order = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        createApplicationGroupSettings(order);
        OrderDO savedOrder = (OrderDO) ordersRestService.getOrder(order.getId(), createUriInfo()).getEntity();
        assertThat(savedOrder.getSettings().getApplicationMapping().getApplications(), contains("myApp1", "myApp2"));
    }

    private Set<ApplicationDO> createApplications() {
        return Sets.newHashSet(new ApplicationDO("myApp2", null, null), new ApplicationDO("myApp1", null, null));
    }

    private Settings createApplicationGroupSettings(Order order)  {
        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setNodeType(NodeType.APPLICATION_SERVER);
        // We create an empty list of applications for application group to emulate how it will look in the DB. 
        // Since applications in an applicationGroup can change, we do not store this but fetches the list from Fasit
        orderDetails.setApplicationMapping(new ApplicationMapping("myAppGrp", Lists.<String>newArrayList()));
        orderDetails.setMiddleWareType(MiddleWareType.jb);
        orderDetails.setEnvironmentClass(EnvironmentClass.t);
        orderDetails.setEnvironmentName("test");
        orderDetails.setServerCount(1);
        orderDetails.setServerSize(ServerSize.s);
        orderDetails.setZone(Zone.fss);
        return settingsRepository.save(new Settings(order, orderDetails));
    }

    @SuppressWarnings("serial")
    private void orderPlainLinux(final String username, final String password) {
        SpringRunAs.runAs(authenticationManager, username, password, new Effect() {
            public void perform() {
                WorkflowToken workflowToken = new WorkflowToken();
                String orchestratorOrderId = UUID.randomUUID().toString();
                workflowToken.setId(orchestratorOrderId);

                when(orchestratorService.send(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);
                ordersRestService.postOrder(new OrderDetailsDO(createPlainLinuxSettings()), createUriInfo(), null);
                verify(orchestratorService).send(Mockito.<ProvisionRequest> anyObject());
                assertThat(orderRepository.findByOrchestratorOrderId(orchestratorOrderId), notNullValue());
            }
        });
    }

    private static Settings createPlainLinuxSettings() {
        Order order = new Order(NodeType.PLAIN_LINUX);
        Settings settings = new Settings(order);
        settings.setMiddleWareType(MiddleWareType.ap);
        settings.setEnvironmentName("env");
        settings.setServerCount(1);
        settings.setServerSize(ServerSize.s);
        settings.setZone(Zone.fss);
        settings.setApplicationMappingName("jenkins");
        settings.setEnvironmentClass(EnvironmentClass.t);
        return settings;
    }

    private UriInfo createUriInfo() {
        try {
            UriInfo uriInfo = mock(UriInfo.class);
            when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(new URI("http://unittest:666/")));
            return uriInfo;
        } catch (IllegalArgumentException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void vmReceiveApplicationServer_createsFasitNode() {
        whenRegisterNodeCalledAddRef();
        receiveVm(NodeType.APPLICATION_SERVER, MiddleWareType.jb);
        verify(fasitRestClient).registerNode(Mockito.<NodeDO> any(), Mockito.anyString());
    }

    @Test
    public void vmReceiveWASDeploymentManager_createsFasitResourceFor() {
        whenRegisterResourceCalledAddRef();
        receiveVm(NodeType.WAS_DEPLOYMENT_MANAGER, MiddleWareType.wa);
        verify(fasitRestClient).registerResource(Mockito.<ResourceElement> any(), Mockito.anyString());
    }

    @Test
    public void vmReceiveBPMDeploymentManager_createsFasitResourceFor() {
        whenRegisterResourceCalledAddRef();
        receiveVm(NodeType.BPM_DEPLOYMENT_MANAGER, MiddleWareType.wa);
        verify(fasitRestClient).registerResource(Mockito.<ResourceElement> any(), Mockito.anyString());
    }

    @Test
    public void vmReceiveBPMNodes_createsFasitNode() {
        whenRegisterNodeCalledAddRef();
        receiveVm(NodeType.BPM_NODES, MiddleWareType.wa);
        verify(fasitRestClient).registerNode(Mockito.<NodeDO> any(), Mockito.anyString());
    }

    @Test
    public void statusLogReceive() {
        Order order = createMinimalOrderAndSettings(NodeType.APPLICATION_SERVER);
        ordersRestService.putResult(order.getId(), new OrderStatusLogDO(new OrderStatusLog(order, "o", "text1", "type1", "option1")), mock(HttpServletRequest.class));
        ordersRestService.putResult(order.getId(), new OrderStatusLogDO(new OrderStatusLog(order, "o", "text2", "type2", "option2")), mock(HttpServletRequest.class));
        Response statusLog = ordersRestService.getStatusLog(order.getId(), createUriInfo());
        System.out.println(statusLog);
    }

    @SuppressWarnings("serial")
    @Test
    public void order_decommissionSuccess() {
        SpringRunAs.runAs(authenticationManager, "user", "user", new Effect() {
            public void perform() {
                createNode(EnvironmentClass.u, "dill");
                OrderDetailsDO orderDetails = new OrderDetailsDO(defaultSettings);
                orderDetails.setNodeType(NodeType.DECOMMISSIONING);
                orderDetails.setHostnames(new String[] { "dill", "dall" });
                WorkflowToken workflowToken = new WorkflowToken();
                workflowToken.setId(UUID.randomUUID().toString());
                when(orchestratorService.decommission(Mockito.<DecomissionRequest> anyObject())).thenReturn(workflowToken);
                OrderDO orderDO = ordersRestService.postOrder(orderDetails, createUriInfo(), null);
                Long id = orderDO.getId();
                OrchestratorNodeDO vm = new OrchestratorNodeDO();
                vm.setHostName("dill");
                ordersRestService.removeVmInformation(id, vm, mock(HttpServletRequest.class));
                assertThat(nodeRepository.findByHostname("dill").iterator().next().getDecommissionOrder(), notNullValue());
            }
        });
    }

    @Test(expected = UnauthorizedException.class)
    public void order_decommissionFailure() {
        createNode(EnvironmentClass.u, "dill");
        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setNodeType(NodeType.DECOMMISSIONING);
        orderDetails.setHostnames(new String[] { "dill", "dall" });
        OrderDO orderDO = ordersRestService.postOrder(orderDetails, createUriInfo(), null);
        assertThat(nodeRepository.findByHostname("dill").iterator().next().getDecommissionOrder(), nullValue());
    }

    private void createNode(EnvironmentClass environmentClass, String hostname) {
        Order order = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        Node node = nodeRepository.save(new Node(order, hostname, null, 1, 1024, null, null, null));
        Settings settings = new Settings(node.getOrder());
        settings.setEnvironmentClass(environmentClass);
        settingsRepository.save(settings);
    }

    private void whenRegisterNodeCalledAddRef() {
        when(fasitRestClient.registerNode(Mockito.<NodeDO> any(), Mockito.anyString())).then(new Answer<NodeDO>() {
            public NodeDO answer(InvocationOnMock invocation) throws Throwable {
                NodeDO node = (NodeDO) invocation.getArguments()[0];
                node.setRef(new URI("http://her/eller/der"));
                return node;
            }
        });
    }

    private void whenRegisterResourceCalledAddRef() {
        when(fasitRestClient.registerResource(Mockito.<ResourceElement> any(), Mockito.anyString())).then(new Answer<ResourceElement>() {
            public ResourceElement answer(InvocationOnMock invocation) throws Throwable {
                ResourceElement resourceElement = (ResourceElement) invocation.getArguments()[0];
                resourceElement.setRef(new URI("http://her/eller/der"));
                return resourceElement;
            }
        });
    }

    @Test
    public void convertXmlToCensoredString() {
        Vm vm = new Vm(OSType.rhel60, MiddleWareType.wa, 1, 2048);
        String drithemmelig = "drithemmelig";
        vm.setCustomFacts(Lists.newArrayList(new Fact(FactType.cloud_app_bpm_cellpwd, drithemmelig)));
        VApp vApp = new VApp(Site.so8, vm);
        ProvisionRequest request = new ProvisionRequest();
        request.getvApps().add(vApp);
        assertThat(ordersRestService.convertXmlToString(ordersRestService.censore(request)), not(containsString(drithemmelig)));
    }

    @Test
    public void statusEnricherFunction_missingOrchestratorOrderId() {
        assertStatusEnricherFunctionFailures(null, now(), OrderStatus.FAILURE, "Ordre mangler ordrenummer fra orchestrator");
    }

    @Test
    public void statusEnricherFunction_timedOut() {
        assertStatusEnricherFunctionFailures(null, now().minus(standardHours(13)), OrderStatus.FAILURE, "Ordre mangler ordrenummer fra orchestrator");
        assertStatusEnricherFunctionFailures("1337", now().minus(standardHours(11)), OrderStatus.SUCCESS, null);
        assertStatusEnricherFunctionFailures("1337", now().minus(standardHours(13)), OrderStatus.SUCCESS, null);
        assertStatusEnricherFunctionFailures("1057", now().minus(standardHours(11)), OrderStatus.PROCESSING, null);
        assertStatusEnricherFunctionFailures("1057", now().minus(standardHours(13)), OrderStatus.FAILURE, "Tidsavbrutt");
    }

    private void assertStatusEnricherFunctionFailures(String orderId, DateTime created, OrderStatus expectedStatus, String expectedMessage) {
        Order order = new Order(NodeType.APPLICATION_SERVER);
        order.setOrchestratorOrderId(orderId);
        when(orchestratorService.getOrderStatus("1337")).thenReturn(Tuple.of(OrderStatus.SUCCESS, (String) null));
        when(orchestratorService.getOrderStatus("1057")).thenReturn(Tuple.of(OrderStatus.PROCESSING, (String) null));
        order.setCreated(created);
        order = ordersRestService.enrichStatus(order);
        assertThat(order.getStatus(), equalTo(expectedStatus));
        assertThat(order.getErrorMessage(), equalTo(expectedMessage));
    }

    private void receiveVm(NodeType a, MiddleWareType b) {
        Order order = createMinimalOrderAndSettings(a);
        OrchestratorNodeDO vm = new OrchestratorNodeDO();
        vm.setMiddlewareType(b);
        ordersRestService.putVmInformation(order.getId(), vm, mock(HttpServletRequest.class));
        assertVmProcessed(order);
    }

    private void assertVmProcessed(Order order) {
        Set<Node> nodes = nodeRepository.findByOrder(order);
        assertThat(nodes.size(), equalTo(1));
        MiddleWareType middleWareType = settingsRepository.findByOrderId(order.getId()).getMiddleWareType();
        assertThat("Failed for " + middleWareType, nodes.iterator().next().getFasitUrl(), notNullValue());
    }

    private Order createMinimalOrderAndSettings(NodeType nodeType) {
        Order order = orderRepository.save(new Order(nodeType));
        OrderDetailsDO orderDetails = new OrderDetailsDO(defaultSettings);
        orderDetails.setNodeType(nodeType);
        orderDetails.setEnvironmentClass(EnvironmentClass.t);
        orderDetails.setZone(Zone.fss);
        settingsRepository.save(new Settings(order, orderDetails));
        return order;
    }

}
