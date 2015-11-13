package no.nav.aura.basta.backend;

import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.SUCCESS;
import static no.nav.aura.basta.domain.result.database.DBOrderResult.*;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.result.database.DBOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class DBHandler {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @Inject
    private OracleClient oracleClient;

    private static final Logger log = LoggerFactory.getLogger(DBHandler.class);

    @Inject
    public DBHandler(OrderRepository orderRepository, FasitUpdateService fasitUpdateService, OracleClient oracleClient) {
        this.orderRepository = orderRepository;
        this.fasitUpdateService = fasitUpdateService;
        this.oracleClient = oracleClient;
    }

    public DBHandler() {
    }

    public void handleWaiting(Long id) {
        try {
            final Order order = orderRepository.findOne(id);
            final Map orderStatus = oracleClient.getOrderStatus(order.getResults().get("statusUri"));
            final Map resourceState = (Map) orderStatus.get("resource_state");
            final String state = (String) resourceState.get("state");

            log.debug("Got state {} for waiting order with id {}", state, order.getId());

            if (state.equalsIgnoreCase("CREATING")) {
                log.debug("Waiting for OEM to finish order with id {}", order.getId());
            } else if (state.equalsIgnoreCase("READY")) {
                addStatusLog(order, "Received READY-status from OEM", "provision:complete");
                final String connectionUrl = (String) orderStatus.get("connect_string");
                final DBOrderResult results = order.getResultAs(DBOrderResult.class);
                final DBOrderInput inputs = order.getInputAs(DBOrderInput.class);
                final ResourceElement fasitDbResource = createFasitResource(connectionUrl, results, inputs);
                final ResourceElement createdResource = fasitUpdateService.createResource(fasitDbResource, order).orElse(fasitDbResource);
                results.put(FASIT_ID, String.valueOf(createdResource.getId()));
                removePasswordFrom(order);
                orderRepository.save(order);
                order.setStatus(SUCCESS);
                log.info("Order with id {} completed successfully", order.getId());
                addStatusLog(order, "Order completed", "provision:complete");
            } else if (state.equalsIgnoreCase("EXCEPTION")) {
                order.setStatus(FAILURE);
                final String reason = Optional.ofNullable((String) orderStatus.get("status")).orElse("OEM status: " + orderStatus.toString());
                addStatusLog(order, reason, "provision:failed");
                log.info("Order with id {} failed to complete with reason {}", order.getId(), reason);
            } else {
                log.warn("Unknown state from OracleEM {}, don't know how to handle this", state);
            }
        } catch (Exception e) {
            log.error("Error occurred during handling of waiting DB order", e);
        }
    }

    private void addStatusLog(Order order, String message, String phase) {
        order.addStatusLog(new OrderStatusLog("Basta", message, phase));
        orderRepository.save(order);
    }

    protected static Order removePasswordFrom(Order order) {
        order.getResults().remove("password");
        return order;
    }

    protected static ResourceElement createFasitResource(String connectionUrl, DBOrderResult results, DBOrderInput inputs) {
        ResourceElement dbResource = new ResourceElement(ResourceTypeDO.DataSource, results.get(FASIT_ALIAS));
        dbResource.addProperty(new PropertyElement("url", connectionUrl));
        dbResource.addProperty(new PropertyElement("username", results.get(USERNAME)));
        dbResource.addProperty(new PropertyElement("password", results.get(PASSWORD)));
        dbResource.setEnvironmentName(inputs.get(ENVIRONMENT_NAME));
        dbResource.setEnvironmentClass(inputs.get(ENVIRONMENT_CLASS));
        dbResource.setApplication(inputs.get(APPLICATION_NAME));

        return dbResource;
    }

}
