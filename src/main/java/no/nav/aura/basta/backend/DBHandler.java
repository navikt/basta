package no.nav.aura.basta.backend;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.result.database.DBOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import oracle.jdbc.pool.OracleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;
import static no.nav.aura.basta.domain.input.vm.OrderStatus.SUCCESS;
import static no.nav.aura.basta.domain.result.database.DBOrderResult.*;

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
                orderRepository.save(order.addStatuslogInfo("Received READY-status from OEM"));
                final String connectionUrl = getFullConnectionString(orderStatus);

                final DBOrderInput inputs = order.getInputAs(DBOrderInput.class);
                updateDefaultSettings(connectionUrl, order);
                final ResourceElement fasitDbResource = createFasitResourceElement(connectionUrl, results, inputs);
                final ResourceElement createdResource = fasitUpdateService.createResource(fasitDbResource, order).orElse(fasitDbResource);
                results.put(FASIT_ID, String.valueOf(createdResource.getId()));
                removePasswordFrom(order);
                orderRepository.save(order);
                order.setStatus(SUCCESS);
                log.info("Order with id {} completed successfully", order.getId());
                orderRepository.save(order.addStatuslogSuccess("Provision complete. Order completed"));
            } else if (state.equalsIgnoreCase("EXCEPTION")) {
                order.setStatus(FAILURE);
                final String reason = Optional.ofNullable((String) orderStatus.get("status")).orElse("OEM status: " + orderStatus.toString());
                orderRepository.save(order.addStatuslogError("Provision failed: " + reason));
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
    private void updateDefaultSettings(String connectionUrl, Order order) {
        DBOrderResult result = order.getResultAs(DBOrderResult.class);
        String username = result.get(USERNAME);

        try (Connection connection = createDatasource(connectionUrl, result)) {
            updateUserSettings(order, username, connection);
        } catch (SQLException se) {
            orderRepository.save(order.addStatuslogError("Unable to connect to database. Default tablespace and profile must be changed manually"));
            log.error("Unable to connect to provisioned database to change user and profile", se);
        }
    }

    private void updateUserSettings(Order order, String username, Connection connection)  {
        Map<String,String> alterCommands = new HashMap<>();
        alterCommands.put("default tablespace", String.format("ALTER USER \"%s\"  DEFAULT TABLESPACE \"%s\" QUOTA UNLIMITED ON \"%s\"", username.toUpperCase(), username.toUpperCase(), username.toUpperCase()));
        alterCommands.put("password life time", "ALTER PROFILE \"DEFAULT\" LIMIT PASSWORD_LIFE_TIME UNLIMITED");
        alterCommands.put("profile", String.format("ALTER USER \"%s\" PROFILE \"C##_NAV_APP_PROFILE\"", username.toUpperCase()));
        alterCommands.put("grant dba_pending_transactions", String.format("GRANT SELECT ON sys.dba_pending_transactions TO \"%s\"", username.toUpperCase()));
        alterCommands.put("grant pending_trans$", String.format("GRANT SELECT ON sys.pending_trans$ TO \"%s\"", username.toUpperCase()));
        alterCommands.put("grant dba_2pc_pending", String.format("GRANT SELECT ON sys.dba_2pc_pending TO \"%s\"", username.toUpperCase()));
        alterCommands.put("grant dbms_xa", String.format("GRANT EXECUTE ON sys.dbms_xa TO \"%s\"", username.toUpperCase()));
        alterCommands.put("grant force transaction", String.format("GRANT FORCE ANY TRANSACTION TO \"%s\"", username.toUpperCase()));

        try {
            for (Map.Entry commandEntry : alterCommands.entrySet()) {
                connection.prepareStatement(commandEntry.getValue().toString()).execute();
                orderRepository.save(order.addStatuslogInfo("Updated " + commandEntry.getKey().toString()));
            }
        } catch (SQLException se) {
            orderRepository.save(order.addStatuslogError(String.format("Could not change user settings, this must be done manually")));
            log.error("Unable to update user settings for {}", username, se);
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
                orderRepository.save(order.addStatuslogInfo("OEM done with removing DB."));
                final Long fasitId = Long.valueOf(results.get(FASIT_ID));
                fasitUpdateService.deleteResource(fasitId, "Deleted by order " + order.getId() + " in Basta", order);
                order.setStatus(SUCCESS);
                log.info("Order with id {} completed successfully", order.getId());
                orderRepository.save(order.addStatuslogInfo("Order completed. Deletion complete"));
            } else {
                log.debug("Got state {} for waiting deletion order with id {}", resourceState.get("state"), order.getId());
            }
        } catch (Exception e) {
            log.error("Error occurred during handling of waiting DB deletion order", e);
        }
    }

   /* private void addStatusLog(Order order, String message) {
        order.addStatuslogInfo(message);
        orderRepository.save(order);
    }

    private void addStatusLogError(Order order, String message) {
        order.addStatuslogError(message);
        orderRepository.save(order);
    }

    private void addStatusLogSuccess(Order order, String message) {
        order.addStatuslogSuccess(message);
        orderRepository.save(order);
    }*/

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