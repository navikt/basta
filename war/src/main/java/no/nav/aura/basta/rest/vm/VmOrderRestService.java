package no.nav.aura.basta.rest.vm;

import static org.joda.time.DateTime.now;
import static org.joda.time.Duration.standardHours;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.ResultStatus;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;
import no.nav.aura.basta.util.Tuple;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/vm/orders")
@Transactional
public class VmOrderRestService {

    private static final Logger logger = LoggerFactory.getLogger(VmOrderRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private OrchestratorService orchestratorService;

    @Inject
    private FasitUpdateService fasitUpdateService;


    public void updateStatuslog(@PathParam("orderId") Long orderId, OrderStatusLogDO orderStatusLogDO) {
		logger.info("Order id " + orderId + " got result " + orderStatusLogDO);
		Order order = orderRepository.findOne(orderId);
		order.setStatusIfMoreImportant(OrderStatus.fromStatusLogLevel(orderStatusLogDO.getOption()));
		orderRepository.save(order);
		saveOrderStatusEntry(order, "Orchestrator", orderStatusLogDO.getText(), orderStatusLogDO.getType(), orderStatusLogDO.getOption());
	}




	public void createVmCallBack(Long orderId, List<OrchestratorNodeDO> vms) {
        logger.info("Received list of with {} vms as orderid {}", vms.size(), orderId);
        for (OrchestratorNodeDO vm : vms) {
            logger.info(ReflectionToStringBuilder.toStringExclude(vm, "deployerPassword"));
            Order order = orderRepository.findOne(orderId);
            VMOrderResult result = order.getResultAs(VMOrderResult.class);
            result.addHostnameWithStatusAndNodeType(vm.getHostName(), ResultStatus.ACTIVE, order.getInputAs(VMOrderInput.class).getNodeType());
            fasitUpdateService.createFasitEntity(order, vm);
            orderRepository.save(order);
        }
    }


    private void saveOrderStatusEntry(Order order, String source, String text, String type, StatusLogLevel option) {
        order.addStatusLog(new OrderStatusLog(source, text, type, option));
        orderRepository.save(order);
    }


    protected OrderDO enrichOrderDOStatus(OrderDO orderDO) {
        if (!orderDO.getStatus().isEndstate()) {
            String orchestratorOrderId = orderDO.getExternalId();
            if (orchestratorOrderId == null) {
                orderDO.setStatus(OrderStatus.FAILURE);
                orderDO.setErrorMessage("Ordre mangler ordrenummer fra orchestrator");
            } else {
                Tuple<OrderStatus, String> tuple = orchestratorService.getOrderStatus(orchestratorOrderId);
                orderDO.setStatus(tuple.fst);
                orderDO.setErrorMessage(tuple.snd);
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
