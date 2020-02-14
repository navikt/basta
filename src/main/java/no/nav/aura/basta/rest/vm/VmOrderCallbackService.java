package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.ResultStatus;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.envconfig.client.NodeDO;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;

import static no.nav.aura.basta.backend.FasitUpdateService.createNodeDO;

@Component
@Transactional
public class VmOrderCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(VmOrderCallbackService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitUpdateService fasitUpdateService;

    public void updateStatuslog(Long orderId, OrderStatusLogDO orderStatusLogDO) {
        logger.info("Order id " + orderId + " got result " + orderStatusLogDO);
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Entity not found " + orderId));
        order.setStatusIfMoreImportant(OrderStatus.fromStatusLogLevel(orderStatusLogDO.getOption()));
        orderRepository.save(order.addStatuslog("Orchestrator: " + orderStatusLogDO.getText() + " : " + orderStatusLogDO.getType(), orderStatusLogDO.getOption()));
    }

    public void createVmCallBack(Long orderId, List<OrchestratorNodeDO> vms) {
        logger.info("Received list of with {} vms as orderid {}", vms.size(), orderId);
        for (OrchestratorNodeDO vm : vms) {
            logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Entity not found " + orderId));
            VMOrderResult result = order.getResultAs(VMOrderResult.class);
            result.addHostnameWithStatusAndNodeType(vm.getHostName(), ResultStatus.ACTIVE, order.getInputAs(VMOrderInput.class).getNodeType());
            VMOrderInput input = order.getInputAs(VMOrderInput.class);

            NodeType nodeType = input.getNodeType();

            NodeDO node;

            switch (nodeType) {
                case JBOSS:
                case WILDFLY:
                case LIBERTY:
                case WAS_NODES:
                case BPM_NODES:
                case WAS9_NODES:
                case BPM86_NODES:
                    node = createNodeDO(vm, input);
                    fasitUpdateService.registerNode(node, order);
                    break;
                case WAS_DEPLOYMENT_MANAGER:
                    fasitUpdateService.createWASDeploymentManagerResource(vm, input, "wasDmgr", order);
                    break;
                case WAS9_DEPLOYMENT_MANAGER:
                    fasitUpdateService.createWASDeploymentManagerResource(vm, input, "was9Dmgr", order);
                    break;
                case BPM_DEPLOYMENT_MANAGER:
                    fasitUpdateService.createWASDeploymentManagerResource(vm, input, "bpmDmgr", order);
                    break;
                case BPM86_DEPLOYMENT_MANAGER:
                    fasitUpdateService.createWASDeploymentManagerResource(vm, input, "bpm86Dmgr", order);
                    break;
                case PLAIN_LINUX:
                case LIGHTWEIGHT_LINUX:
                case FLATCAR_LINUX:
                case DEV_TOOLS:
                case WINDOWS_APPLICATIONSERVER:
                case WINDOWS_INTERNET_SERVER:
                    order.addStatuslogInfo("No operation in Fasit for " + nodeType);
                    break;
                default:
                    throw new RuntimeException("Unable to handle callback with node type " + nodeType + " for order " + order.getId());
            }
            orderRepository.save(order);
        }
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

}
