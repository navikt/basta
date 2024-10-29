package no.nav.aura.basta.rest;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@Rollback
@Transactional
public class OrdersRestServiceTest {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasitRestClient;

    @Inject
    private OrchestratorClient orchestratorClient;

    @Inject
    private VmOrdersRestApi ordersVMRestApiService;


    @BeforeAll
    public static void setFasitBaseUrl() {
          System.setProperty("fasit_rest_api_url", "https://this.is.fasit.com");
    }

    @AfterEach
    public void resetMockito() {
        Mockito.reset(fasitRestClient, orchestratorClient);
    }


    @Test
    public void vmReceiveApplicationServer_createsFasitNode() {
        whenRegisterNodeCalledAddRef();
        receiveVm(NodeType.JBOSS, MiddlewareType.jb);
        verify(fasitRestClient).registerNode(Mockito.any(), Mockito.anyString());
    }

    @Test
    public void vmReceiveWASDeploymentManager_createsFasitResourceFor() {
        whenRegisterResourceCalledAddRef();
        receiveVm(NodeType.WAS_DEPLOYMENT_MANAGER, MiddlewareType.wa);
        verify(fasitRestClient).registerResource(Mockito.any(), Mockito.anyString());
    }

    @Test
    public void vmReceiveBPMDeploymentManager_createsFasitResourceFor() {
        whenRegisterResourceCalledAddRef();
        receiveVm(NodeType.BPM_DEPLOYMENT_MANAGER, MiddlewareType.wa);
        verify(fasitRestClient).registerResource(Mockito.any(), Mockito.anyString());
    }

    @Test
    public void vmReceiveBPMNodes_createsFasitNode() {
        whenRegisterNodeCalledAddRef();
        receiveVm(NodeType.BPM_NODES, MiddlewareType.wa);
        verify(fasitRestClient).registerNode(Mockito.any(), Mockito.anyString());
    }



    private void whenRegisterNodeCalledAddRef() {
        when(fasitRestClient.registerNode(Mockito.any(), Mockito.anyString())).then((Answer<NodeDO>) invocation -> {
            NodeDO node = (NodeDO) invocation.getArguments()[0];
            node.setRef(new URI("http://her/eller/der"));
            return node;
        });
    }

    private void whenRegisterResourceCalledAddRef() {
        when(fasitRestClient.registerResource(Mockito.any(), Mockito.anyString())).then((Answer<ResourceElement>) invocation -> {
            ResourceElement resourceElement = (ResourceElement) invocation.getArguments()[0];
            resourceElement.setRef(new URI("http://her/eller/der"));
            return resourceElement;
        });
    }

    private void receiveVm(NodeType a, MiddlewareType b) {
        Order order = createMinimalOrderAndSettings(a, b);
        OrchestratorNodeDO vm = new OrchestratorNodeDO();
        vm.setMiddlewareType(b);
        vm.setHostName("foo.devillo.no");
        OrchestratorNodeDOList orchestratorNodeDOList = new OrchestratorNodeDOList();
        orchestratorNodeDOList.addVM(vm);
        System.out.println(XmlUtils.generateXml(orchestratorNodeDOList));
        ordersVMRestApiService.provisionCallback(order.getId(), orchestratorNodeDOList);
        Order storedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId()));
        Set<ResultDO> nodes = storedOrder.getResultAs(VMOrderResult.class).asResultDO();
        MatcherAssert.assertThat(nodes.size(), equalTo(1));
        MiddlewareType middleWareType = storedOrder.getInputAs(VMOrderInput.class).getMiddlewareType();
        MatcherAssert.assertThat("Failed for " + middleWareType, nodes.iterator().next().getDetail(VMOrderResult.RESULT_URL_PROPERTY_KEY), notNullValue());
    }

    private Order createMinimalOrderAndSettings(NodeType nodeType, MiddlewareType middleWareType) {

        VMOrderInput input = new VMOrderInput();
        input.setNodeType(nodeType);
        input.setMiddlewareType(middleWareType);
        input.setEnvironmentClass(EnvironmentClass.t);
        input.setZone(Zone.fss);
        Order order = new Order(OrderType.VM, OrderOperation.CREATE, input);
        orderRepository.save(order);
        return order;
    }

}
