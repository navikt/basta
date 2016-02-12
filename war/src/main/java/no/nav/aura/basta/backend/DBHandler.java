package no.nav.aura.basta.backend;

import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.SUCCESS;
import static no.nav.aura.basta.domain.result.database.DBOrderResult.*;

import java.sql.*;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import no.nav.aura.appconfig.resource.ConnectionPool;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.result.database.DBOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import oracle.jdbc.pool.OracleDataSource;
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

    public void handleCreationOrder(Long id) {
        try {
            final Order order = orderRepository.findOne(id);
            final DBOrderResult results = order.getResultAs(DBOrderResult.class);
            final Map orderStatus = oracleClient.getOrderStatus(results.get(OEM_ENDPOINT));
            final Map resourceState = (Map) orderStatus.get("resource_state");
            final String state = (String) resourceState.get("state");

            log.debug("Got state {} for waiting order with id {}", state, order.getId());

            if (state.equalsIgnoreCase("CREATING")) {
                log.debug("Waiting for OEM to finish creation order with id {}", order.getId());
            } else if (state.equalsIgnoreCase("READY")) {
                addStatusLog(order, "Received READY-status from OEM", "provision:complete");
                final String connectionUrl = getFullConnectionString(orderStatus);

                final DBOrderInput inputs = order.getInputAs(DBOrderInput.class);
                fixTableSpace(connectionUrl, order);
                final ResourceElement fasitDbResource = createFasitResourceElement(connectionUrl, results, inputs);
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
            log.error("Error occurred during handling of waiting DB creation order", e);
        }
    }

    private String getFullConnectionString(Map orderStatus) {
        String connectionUrl = (String) orderStatus.get("connect_string");
        return String.format("jdbc:oracle:thin:@%s", connectionUrl);
    }

    // AURA-1644 Temporary workaround for setting default tablespace until the enchancement request in OEM is available
    private void fixTableSpace(String connectionUrl, Order order) {
        DBOrderResult result = order.getResultAs(DBOrderResult.class);
        String username = result.get(USERNAME);

        try (Connection connection = createDatasource(connectionUrl, result)) {
            addStatusLog(order, String.format("Default tablespace is %s, changing to %s", getDefaultTemplateForDb(connection, username), username), "fixTableSpace");
            updateDefaultTableSpace(order, username, connection);
        } catch (SQLException se) {
            addWarningLog(order, String.format("Unable to connect to database. Default tablespace must be changed from SYSTEM to %s manually", username), "fixTableSpace");
            log.error("Unable to connect to privisioned Database to change default table space", se);
        }
    }

    private void updateDefaultTableSpace(Order order, String username, Connection connection)  {
        String changeTablespace = String.format("ALTER USER \"%s\"  DEFAULT TABLESPACE \"%s\" QUOTA UNLIMITED ON \"%s\"", username.toUpperCase(), username, username);
        try {
            connection.prepareStatement(changeTablespace).execute();
            addStatusLog(order, String.format("Default tablespace is now %s", getDefaultTemplateForDb(connection, username)), "fixTableSpace");
        } catch (SQLException se) {
            addWarningLog(order, String.format("Default tablespace not changed, this must be done maually"), "fixTableSpace");
            log.error("Unable to update default tablespace for {}", username, se);
        }
    }


    private String getDefaultTemplateForDb(Connection connection, String username) throws SQLException {
        String query = String.format("select DEFAULT_TABLESPACE from USER_USERS where username = '%s'", username.toUpperCase());
        ResultSet resultSet = connection.prepareStatement(query).executeQuery();
        resultSet.next();
        return resultSet.getString(1);
    }

    private Connection createDatasource(String connectionUrl, DBOrderResult result) throws SQLException {
        String username = result.get(USERNAME);
        String password = result.get(PASSWORD);

        OracleDataSource oracleDataSource = new OracleDataSource();
        oracleDataSource.setURL(connectionUrl);
        oracleDataSource.setUser(username);
        oracleDataSource.setPassword(password);

        return oracleDataSource.getConnection();
    }

    public void handleDeletionOrder(Long id) {
        try {
            log.debug("Handling deletion order with id {}", id);
            final Order order = orderRepository.findOne(id);
            final DBOrderResult results = order.getResultAs(DBOrderResult.class);
            final String statusUri = results.get(OEM_ENDPOINT);
            final Map orderStatus = oracleClient.getDeletionOrderStatus(statusUri);
            final Map resourceState = (Map) orderStatus.get("resource_state");

            if (resourceState == null) {
                addStatusLog(order, "OEM done with removing DB", "deletion:finishing");
                final String fasitId = results.get(FASIT_ID);
                fasitUpdateService.deleteResource(fasitId, "Deleted by order " + order.getId() + " in Basta", order);
                order.setStatus(SUCCESS);
                orderRepository.save(order);
                log.info("Order with id {} completed successfully", order.getId());
                addStatusLog(order, "Order completed", "deletion:complete");
            } else {
                log.debug("Got state {} for waiting deletion order with id {}", resourceState.get("state"), order.getId());
            }
        } catch (Exception e) {
            log.error("Error occurred during handling of waiting DB deletion order", e);
        }
    }

    private void addStatusLog(Order order, String message, String phase) {
        order.addStatusLog(new OrderStatusLog("Basta", message, phase));
        orderRepository.save(order);
    }

    private void addWarningLog(Order order, String message, String phase) {
        order.addStatusLog(new OrderStatusLog("Basta", message, phase, StatusLogLevel.warning));
        orderRepository.save(order);
    }

    protected static Order removePasswordFrom(Order order) {
        order.getResults().remove("password");
        return order;
    }

    protected static ResourceElement createFasitResourceElement(String connectionUrl, DBOrderResult results, DBOrderInput inputs) {
        ResourceElement dbResource = new ResourceElement(ResourceTypeDO.DataSource, results.get(FASIT_ALIAS));
        dbResource.addProperty(new PropertyElement("url", connectionUrl));
        dbResource.addProperty(new PropertyElement("username", results.get(USERNAME)));
        dbResource.addProperty(new PropertyElement("password", results.get(PASSWORD)));
        dbResource.addProperty(new PropertyElement("oemEndpoint", results.get(OEM_ENDPOINT)));
        dbResource.setEnvironmentName(inputs.get(ENVIRONMENT_NAME));
        dbResource.setEnvironmentClass(inputs.get(ENVIRONMENT_CLASS));
        dbResource.setApplication(inputs.get(APPLICATION_NAME));

        return dbResource;
    }
}
