package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static no.nav.aura.basta.rest.RestServiceTestUtils.getOrderIdFromMetadata;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Fact;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.VApp;
import no.nav.aura.basta.backend.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm.OSType;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.ServerSize;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.order.OrchestratorRequestFactoryTest;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.aura.envconfig.client.ApplicationDO;
import no.nav.aura.envconfig.client.ApplicationGroupDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.BeforeClass;
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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@TransactionConfiguration
@Transactional
public class VmOrderRestServiceTest {

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private VmOrderRestService ordersRestService;

    @Inject
    private FasitRestClient fasitRestClient;

    @Inject
    private OrchestratorService orchestratorService;


    @BeforeClass
    public static void setFasitBaseUrl() {
        System.setProperty("fasit.rest.api.url", "http://e34apsl00136.devillo.no:8080/conf");
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
                VMOrderInput input = OrchestratorRequestFactoryTest.createRequestJbossSettings().getInputAs(VMOrderInput.class);
                input.setEnvironmentClass(environmentClass);
                String orchestratorOrderId = UUID.randomUUID().toString();
                if (expectChanges) {
                    WorkflowToken workflowToken = new WorkflowToken();
                    workflowToken.setId(orchestratorOrderId);
                    when(orchestratorService.send(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);
                }
                ordersRestService.provisionNew(input.copy(), createUriInfo(), null);
                if (expectChanges) {
                    verify(orchestratorService).send(Mockito.<ProvisionRequest> anyObject());
                    assertThat(orderRepository.findByExternalId(orchestratorOrderId), notNullValue());
                }
            }
        });
    }

    @Test(expected = UnauthorizedException.class)
    public void orderingPlainLinuxAsNormalUser_shouldFail() throws Exception {
        orderPlainLinux("user_operations", "admin");
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
        ordering_using_putXMLOrder("prod", 0);
    }

    private void ordering_using_putXMLOrder(final String orchestratorEnvironmentClass, final int expectedStatus) {
        SpringRunAs.runAs(authenticationManager, "superuser_without_prod", "superuser2", new Effect() {
            public void perform() {
                VMOrderInput input = OrchestratorRequestFactoryTest.createRequestJbossSettings().getInputAs(VMOrderInput.class);
                input.setEnvironmentClass(EnvironmentClass.t);

                WorkflowToken workflowToken = new WorkflowToken();
                workflowToken.setId(UUID.randomUUID().toString());
                when(orchestratorService.send(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);
                Response postOrderResponse = ordersRestService.provisionNew(input.copy(), createUriInfo(), true);
                Order order = orderRepository.findOne(getOrderIdFromMetadata(postOrderResponse));

                String requestXML;
                try {
                    ProvisionRequest provisionRequest = XmlUtils.parseAndValidateXmlString(ProvisionRequest.class, order.getExternalRequest());
                    provisionRequest.setEnvironmentClass(orchestratorEnvironmentClass);
                    requestXML = XmlUtils.prettyFormat(XmlUtils.generateXml(provisionRequest), 2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Response response = ordersRestService.putXMLOrder(requestXML, order.getId(), createUriInfo());
                assertThat(response.getStatus(), is(expectedStatus));
            }
        });
    }

    @Test
    public void orderingNodeForApplicationGroup() {
        SpringRunAs.runAs(authenticationManager, "user_operations", "admin", new Effect() {
            public void perform() {
                WorkflowToken workflowToken = new WorkflowToken();
                String orchestratorOrderId = UUID.randomUUID().toString();
                workflowToken.setId(orchestratorOrderId);

                when(orchestratorService.send(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);
                when(fasitRestClient.getApplicationGroup(anyString())).thenReturn(new ApplicationGroupDO("myAppGrp", createApplications()));
                Order order = orderRepository.save(Order.newProvisionOrder(createApplicationGroupInput()));

                ordersRestService.provisionNew(order.getInputAs(MapOperations.class).copy(), createUriInfo(), null);
                verify(orchestratorService).send(Mockito.<ProvisionRequest> anyObject());
                assertThat(orderRepository.findByExternalId(orchestratorOrderId), notNullValue());
            }
        });
    }

    @Test
    public void createFasitResourceForNodeMappedToApplicationGroup() {
        whenRegisterNodeCalledAddRef();

        Order order = orderRepository.save(Order.newProvisionOrder(createApplicationGroupInput()));
        OrchestratorNodeDO vm = new OrchestratorNodeDO();
        vm.setMiddlewareType(MiddleWareType.jb);
        vm.setHostName("foo.devillo.no");

        when(fasitRestClient.getApplicationGroup(anyString())).thenReturn(new ApplicationGroupDO("myAppGrp", createApplications()));
        ordersRestService.createVmCallBack(order.getId(), Arrays.asList(vm));
        verify(fasitRestClient).registerNode(Mockito.<NodeDO> anyObject(), anyString());
    }

    private Set<ApplicationDO> createApplications() {
        return Sets.newHashSet(new ApplicationDO("myApp2", null, null), new ApplicationDO("myApp1", null, null));
    }

    private MapOperations createApplicationGroupInput() {
        VMOrderInput input = new VMOrderInput(Maps.newTreeMap());
        input.setNodeType(NodeType.JBOSS);
        input.setApplicationMappingName("myAppGrp");
        input.setMiddleWareType(MiddleWareType.jb);
        input.setEnvironmentClass(EnvironmentClass.t);
        input.setEnvironmentName("test");
        input.setServerCount(1);
        input.setServerSize(ServerSize.s);
        input.setZone(Zone.fss);
        return input;
    }

    @SuppressWarnings("serial")
    private void orderPlainLinux(final String username, final String password) {
        SpringRunAs.runAs(authenticationManager, username, password, new Effect() {
            public void perform() {
                WorkflowToken workflowToken = new WorkflowToken();
                String orchestratorOrderId = UUID.randomUUID().toString();
                workflowToken.setId(orchestratorOrderId);

                when(orchestratorService.send(Mockito.<OrchestatorRequest> anyObject())).thenReturn(workflowToken);
                Order order = Order.newProvisionOrder(createPlainLinuxInput());
                ordersRestService.provisionNew(order.getInputAs(MapOperations.class).copy(), createUriInfo(), null);
                verify(orchestratorService).send(Mockito.<ProvisionRequest> anyObject());
                assertThat(orderRepository.findByExternalId(orchestratorOrderId), notNullValue());
            }
        });
    }

    private static MapOperations createPlainLinuxInput() {

        VMOrderInput input = new VMOrderInput(Maps.newTreeMap());
        input.setNodeType(NodeType.PLAIN_LINUX);
        input.setMiddleWareType(MiddleWareType.ap);
        input.setEnvironmentName("env");
        input.setServerCount(1);
        input.setServerSize(ServerSize.s);
        input.setZone(Zone.fss);
        input.setApplicationMappingName("jenkins");
        input.setEnvironmentClass(EnvironmentClass.t);
        return input;
    }

    @Test
    public void statusLogReceive() {
        Order order = createMinimalOrderAndSettings(NodeType.JBOSS, MiddleWareType.jb);
        ordersRestService.updateStatuslog(order.getId(), new OrderStatusLogDO(new OrderStatusLog("o", "text1", "type1")), mock(HttpServletRequest.class));
        ordersRestService.updateStatuslog(order.getId(), new OrderStatusLogDO(new OrderStatusLog("o", "text2", "type2")), mock(HttpServletRequest.class));
//        Response statusLog = ordersRestService.getStatusLog(order.getId(), createUriInfo());
//        System.out.println(statusLog);
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


    @Test
    public void convertXmlToCensoredString() {
        Vm vm = new Vm(OSType.rhel60, MiddleWareType.wa, 1, 2048);
        String drithemmelig = "drithemmelig";
        vm.setCustomFacts(Lists.newArrayList(new Fact(FactType.cloud_app_bpm_cellpwd, drithemmelig)));
        VApp vApp = new VApp(Site.so8, vm);
        ProvisionRequest request = new ProvisionRequest();
        request.getvApps().add(vApp);
        assertThat(ordersRestService.convertXmlToString(request.censore()), not(containsString(drithemmelig)));
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
        Order order = Order.newProvisionOrder(new MapOperations(Maps.newHashMap()));
        order.setExternalId(orderId);
        order.setId(1L);
        when(orchestratorService.getOrderStatus("1337")).thenReturn(Tuple.of(OrderStatus.SUCCESS, (String) null));
        when(orchestratorService.getOrderStatus("1057")).thenReturn(Tuple.of(OrderStatus.PROCESSING, (String) null));
        order.setCreated(created);
        OrderDO orderDO = ordersRestService.createRichOrderDO(createUriInfo(), order);
        orderDO = ordersRestService.enrichOrderDOStatus(orderDO);
        assertThat(orderDO.getStatus(), equalTo(expectedStatus));
        assertThat(orderDO.getErrorMessage(), equalTo(expectedMessage));
    }

    private Order createMinimalOrderAndSettings(NodeType nodeType, MiddleWareType middleWareType) {

        VMOrderInput input = new VMOrderInput(Maps.newTreeMap());
        input.setNodeType(nodeType);
        input.setMiddleWareType(middleWareType);
        input.setEnvironmentClass(EnvironmentClass.t);
        input.setZone(Zone.fss);
        Order order = Order.newProvisionOrder(input);
        orderRepository.save(order);
        return order;
    }
}

