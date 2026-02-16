package no.nav.aura.basta.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import no.nav.aura.basta.backend.RestClient;
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
    private RestClient restClient;
    
    @Inject
    private OrchestratorClient orchestratorClient;

    @Inject
    private VmOrdersRestApi ordersVMRestApiService;

    @BeforeEach
    public void setupMocks() {
        // Configure RestClient mock - now properly injected since component scan excludes it
        when(restClient.createFasitResource(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Optional.of("12345"));
    }

    @AfterEach
    public void resetMockito() {
        // Only reset the mocked beans, not the real FasitUpdateService
        Mockito.reset(restClient, orchestratorClient);
    }


    @Test
    public void vmReceiveApplicationServer_createsFasitNode() {
        receiveVm(NodeType.JBOSS, MiddlewareType.jb);
        // Verify that RestClient.createFasitResource was called (FasitUpdateService.registerNode calls it)
        verify(restClient).createFasitResource(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void vmReceiveWASDeploymentManager_createsFasitResourceFor() {
        receiveVm(NodeType.WAS_DEPLOYMENT_MANAGER, MiddlewareType.wa);
        // Verify that RestClient.createFasitResource was called (FasitUpdateService.createResource calls it)
        verify(restClient).createFasitResource(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void vmReceiveBPMDeploymentManager_createsFasitResourceFor() {
        receiveVm(NodeType.BPM_DEPLOYMENT_MANAGER, MiddlewareType.wa);
        // Verify that RestClient.createFasitResource was called (FasitUpdateService.createResource calls it)
        verify(restClient).createFasitResource(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void vmReceiveBPMNodes_createsFasitNode() {
        receiveVm(NodeType.BPM_NODES, MiddlewareType.wa);
        // Verify that RestClient.createFasitResource was called (FasitUpdateService.registerNode calls it)
        verify(restClient).createFasitResource(anyString(), anyString(), anyString(), anyString());
    }

    private void receiveVm(NodeType a, MiddlewareType b) {
        Order order = createMinimalOrderAndSettings(a, b);
        OrchestratorNodeDO vm = new OrchestratorNodeDO();
        vm.setMiddlewareType(b);
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
