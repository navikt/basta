package no.nav.aura.basta.rest;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Set;

import javax.inject.Inject;

import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@TransactionConfiguration
@Transactional
public class OrdersRestServiceTest {



    @Inject
    private OrderRepository orderRepository;

    @Inject
    private OrdersListRestService ordersRestService;

    @Inject
    private FasitRestClient fasitRestClient;

    @Inject
    private OrchestratorService orchestratorService;

    @Inject
    private VmOrdersRestApi ordersVMRestApiService;


    @BeforeClass
    public static void setFasitBaseUrl() {
        System.setProperty("fasit.rest.api.url", "http://e34apsl00136.devillo.no:8080/conf");
    }

    @After
    public void resetMockito() {
        Mockito.reset(fasitRestClient, orchestratorService);
    }




    @Test
    public void vmReceiveApplicationServer_createsFasitNode() {
        whenRegisterNodeCalledAddRef();
        receiveVm(NodeType.JBOSS, MiddleWareType.jb, "foo.devillo.no");
        verify(fasitRestClient).registerNode(Mockito.<NodeDO> any(), Mockito.anyString());
    }

    @Test
    public void vmReceiveWASDeploymentManager_createsFasitResourceFor() {
        whenRegisterResourceCalledAddRef();
        receiveVm(NodeType.WAS_DEPLOYMENT_MANAGER, MiddleWareType.wa, "foo.devillo.no");
        verify(fasitRestClient).registerResource(Mockito.<ResourceElement> any(), Mockito.anyString());
    }

    @Test
    public void vmReceiveBPMDeploymentManager_createsFasitResourceFor() {
        whenRegisterResourceCalledAddRef();
        receiveVm(NodeType.BPM_DEPLOYMENT_MANAGER, MiddleWareType.wa, "foo.devillo.no");
        verify(fasitRestClient).registerResource(Mockito.<ResourceElement> any(), Mockito.anyString());
    }

    @Test
    public void vmReceiveBPMNodes_createsFasitNode() {
        whenRegisterNodeCalledAddRef();
        receiveVm(NodeType.BPM_NODES, MiddleWareType.wa, "foo.devillo.no");
        verify(fasitRestClient).registerNode(Mockito.<NodeDO> any(), Mockito.anyString());
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
        Order order = new Order(OrderType.VM, OrderOperation.CREATE, new VMOrderInput());
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

    private void receiveVm(NodeType a, MiddleWareType b, String hostname) {
        Order order = createMinimalOrderAndSettings(a, b);
        OrchestratorNodeDO vm = new OrchestratorNodeDO();
        vm.setMiddlewareType(b);
        vm.setHostName(hostname);
        OrchestratorNodeDOList orchestratorNodeDOList = new OrchestratorNodeDOList();
        orchestratorNodeDOList.addVM(vm);
        System.out.println(XmlUtils.generateXml(orchestratorNodeDOList));
        ordersVMRestApiService.createCallback(order.getId(), orchestratorNodeDOList);
        Order storedOrder = orderRepository.findOne(order.getId());
        Set<ResultDO> nodes = storedOrder.getResultAs(VMOrderResult.class).asResultDO();
        assertThat(nodes.size(), equalTo(1));
        MiddleWareType middleWareType = storedOrder.getInputAs(VMOrderInput.class).getMiddleWareType();
        assertThat("Failed for " + middleWareType, nodes.iterator().next().getDetail(VMOrderResult.RESULT_URL_PROPERTY_KEY), notNullValue());
    }

    private Order createMinimalOrderAndSettings(NodeType nodeType, MiddleWareType middleWareType) {

        VMOrderInput input = new VMOrderInput();
        input.setNodeType(nodeType);
        input.setMiddleWareType(middleWareType);
        input.setEnvironmentClass(EnvironmentClass.t);
        input.setZone(Zone.fss);
        Order order = new Order(OrderType.VM, OrderOperation.CREATE, input);
        orderRepository.save(order);
        return order;
    }

}
