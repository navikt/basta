package no.nav.aura.basta.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@Rollback
@Transactional
public class OrdersRestServiceTest {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitUpdateService fasitUpdateService;
    
    @Inject
    private OrchestratorClient orchestratorClient;

    @Inject
    private VmOrdersRestApi ordersVMRestApiService;

    @AfterEach
    public void resetMockito() {
        Mockito.reset(fasitUpdateService, orchestratorClient);
    }

    @Test
    public void vmReceiveApplicationServer_createsFasitNode() {
        receiveVm(NodeType.JBOSS, MiddlewareType.jb);
        verify(fasitUpdateService).registerNode(any(), any());
    }

    @Test
    public void vmReceiveWASDeploymentManager_createsFasitResourceFor() {
        receiveVm(NodeType.WAS9_DEPLOYMENT_MANAGER, MiddlewareType.wa);
        verify(fasitUpdateService).createWASDeploymentManagerResource(any(), any(), eq("was9Dmgr"), any(Order.class));
    }

    @Test
    public void vmReceiveBPMDeploymentManager_createsFasitResourceFor() {
        receiveVm(NodeType.BPM86_DEPLOYMENT_MANAGER, MiddlewareType.wa);
        verify(fasitUpdateService).createWASDeploymentManagerResource(any(), any(), eq("bpm86Dmgr"), any(Order.class));
    }

    @Test
    public void vmReceiveBPMNodes_createsFasitNode() {
        receiveVm(NodeType.BPM_NODES, MiddlewareType.wa);
        verify(fasitUpdateService).registerNode(any(), any());
    }

    private void receiveVm(NodeType nodeType, MiddlewareType middlewareType) {
        Order order = createMinimalOrderAndSettings(nodeType, middlewareType);
        OrchestratorNodeDO vm = new OrchestratorNodeDO();
        vm.setMiddlewareType(middlewareType);
        vm.setHostName("foo.devillo.no");
        vm.setDeployUser("testuser");
        vm.setDeployerPassword("testpwd");
        OrchestratorNodeDOList orchestratorNodeDOList = new OrchestratorNodeDOList();
        orchestratorNodeDOList.addVM(vm);
        System.out.println(XmlUtils.generateXml(orchestratorNodeDOList));
        ordersVMRestApiService.provisionCallback(order.getId(), orchestratorNodeDOList);
        Order storedOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new IllegalArgumentException("Entity " +
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
