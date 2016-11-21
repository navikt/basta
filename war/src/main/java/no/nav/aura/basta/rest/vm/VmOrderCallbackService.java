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
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import no.nav.aura.envconfig.client.NodeDO;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static no.nav.aura.basta.backend.FasitUpdateService.createNodeDO;
import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;

@Component
@Transactional
public class VmOrderCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(VmOrderCallbackService.class);

    @Inject
    private OrderRepository orderRepository;

//    @Inject
//    private OrchestratorService orchestratorService;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @Inject
    private OpenAMOrderRestService openAMOrderRestService;

    public void updateStatuslog(Long orderId, OrderStatusLogDO orderStatusLogDO) {
        logger.info("Order id " + orderId + " got result " + orderStatusLogDO);
        Order order = orderRepository.findOne(orderId);
        order.setStatusIfMoreImportant(OrderStatus.fromStatusLogLevel(orderStatusLogDO.getOption()));
        orderRepository.save(order.addStatuslog("Orchestrator: " + orderStatusLogDO.getText() + " : " + orderStatusLogDO.getType(), orderStatusLogDO.getOption()));
    }

    public void createVmCallBack(Long orderId, List<OrchestratorNodeDO> vms) {
        logger.info("Received list of with {} vms as orderid {}", vms.size(), orderId);
        for (OrchestratorNodeDO vm : vms) {
            logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
            Order order = orderRepository.findOne(orderId);
            VMOrderResult result = order.getResultAs(VMOrderResult.class);
            result.addHostnameWithStatusAndNodeType(vm.getHostName(), ResultStatus.ACTIVE, order.getInputAs(VMOrderInput.class).getNodeType());
            VMOrderInput input = order.getInputAs(VMOrderInput.class);

            NodeType nodeType = order.getInputAs(VMOrderInput.class).getNodeType();

            NodeDO node;

            switch (nodeType) {
                case JBOSS:
                case LIBERTY:
                case WAS_NODES:
                case BPM_NODES:
                    node = createNodeDO(vm, input);
                    fasitUpdateService.registerNode(node, order);
                    break;
                case WAS_DEPLOYMENT_MANAGER:
                    fasitUpdateService.createWASDeploymentManagerResource(vm, input, "wasDmgr", order);
                    break;
                case BPM_DEPLOYMENT_MANAGER:
                    fasitUpdateService.createWASDeploymentManagerResource(vm, input, "bpmDmgr", order);
                    break;
                case OPENAM_PROXY:
                    node = createNodeDO(vm, input);
                    node.setAccessAdGroup(OpenAMOrderRestService.OPENAM_ACCESS_GROUP);
                    fasitUpdateService.registerNode(node, order);
                    break;
                case OPENAM_SERVER:
                    node = createNodeDO(vm, input);
                    node.setAccessAdGroup(OpenAMOrderRestService.OPENAM_ACCESS_GROUP);
                    fasitUpdateService.registerNode(createNodeDO(vm, input), order);
                    openAMOrderRestService.registrerOpenAmApplication(order, result, input);
                    break;
                case PLAIN_LINUX:
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

    protected OrderDO enrichOrderDOStatus(OrderDO orderDO) {
        if (!orderDO.getStatus().isEndstate()) {
            String orchestratorOrderId = orderDO.getExternalId();
            if (orchestratorOrderId == null) {
                orderDO.setStatus(OrderStatus.FAILURE);
                orderDO.setErrorMessage("Ordre mangler ordrenummer fra orchestrator");
            } else {
//                Tuple<OrderStatus, String> tuple = orchestratorService.getOrderStatus(orchestratorOrderId);
//                orderDO.setStatus(tuple.fst);
//                orderDO.setErrorMessage(tuple.snd);
            }
            if (!orderDO.getStatus().isEndstate() && new DateTime(orderDO.getCreated()).isBefore(now().minus(standardHours(12)))) {
                orderDO.setStatus(OrderStatus.FAILURE);
                orderDO.setErrorMessage("Tidsavbrutt");
            }
        }
        return orderDO;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

}
