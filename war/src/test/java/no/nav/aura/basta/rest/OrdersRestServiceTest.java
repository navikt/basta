package no.nav.aura.basta.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV2FactoryTest;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.persistence.SettingsRepository;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.Fact;
import no.nav.aura.basta.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.VApp;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.OSType;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.joda.time.DateTime;
import org.junit.After;
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
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                Settings settings = OrderV2FactoryTest.createRequestJbossSettings();
                settings.setEnvironmentClass(environmentClass);
                String orchestratorOrderId = UUID.randomUUID().toString();
                if (expectChanges) {
                    WorkflowToken workflowToken = new WorkflowToken();
                    workflowToken.setId(orchestratorOrderId);
                    when(orchestratorService.send(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);
                }
                ordersRestService.postOrder(new OrderDetailsDO(settings), createUriInfo());
                if (expectChanges) {
                    verify(orchestratorService).send(Mockito.<ProvisionRequest> anyObject());
                    assertThat(orderRepository.findByOrchestratorOrderId(orchestratorOrderId), notNullValue());
                }
            }
        });
    }

    private UriInfo createUriInfo() {
        try {
            UriInfo uriInfo = mock(UriInfo.class);
            when(uriInfo.getRequestUriBuilder()).thenReturn(UriBuilder.fromUri(new URI("http://unittest:666/")));
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

    @SuppressWarnings("serial")
    @Test
    public void order_decommisionSuccess() {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                createNode(EnvironmentClass.u, "dill");
                OrderDetailsDO orderDetails = new OrderDetailsDO();
                orderDetails.setNodeType(NodeType.DECOMMISSIONING);
                orderDetails.setHostnames(new String[] { "dill", "dall" });
                WorkflowToken workflowToken = new WorkflowToken();
                workflowToken.setId(UUID.randomUUID().toString());
                when(orchestratorService.decommission(Mockito.<DecomissionRequest> anyObject())).thenReturn(workflowToken);
                ordersRestService.postOrder(orderDetails, createUriInfo());
                assertThat(nodeRepository.findByHostname("dill").iterator().next().getDecommissionOrder(), notNullValue());
            }
        });
    }

    @Test(expected = UnauthorizedException.class)
    public void order_decommisionFailure() {
        createNode(EnvironmentClass.u, "dill");
        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setNodeType(NodeType.DECOMMISSIONING);
        orderDetails.setHostnames(new String[] { "dill", "dall" });
        ordersRestService.postOrder(orderDetails, createUriInfo());
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
        order = ordersRestService.statusEnricherFunction.apply(order);
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
        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setNodeType(nodeType);
        orderDetails.setEnvironmentClass(EnvironmentClass.t);
        orderDetails.setZone(Zone.fss);
        settingsRepository.save(new Settings(order, orderDetails));
        return order;
    }

}
