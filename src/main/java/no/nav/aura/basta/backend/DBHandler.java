package no.nav.aura.basta.backend;

import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.payload.ScopePayload;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.result.database.DBOrderResult;
import no.nav.aura.basta.repository.OrderRepository;
import oracle.jdbc.pool.OracleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.sql.Connection;
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
    private VaultUpdateService vaultUpdateService;

    @Inject
    private OracleClient oracleClient;

    private static final Logger log = LoggerFactory.getLogger(DBHandler.class);

    @Inject
    public DBHandler(
            OrderRepository orderRepository,
            FasitUpdateService fasitUpdateService,
            VaultUpdateService vaultUpdateService,
            OracleClient oracleClient
    ) {
        this.orderRepository = orderRepository;
        this.fasitUpdateService = fasitUpdateService;
        this.vaultUpdateService = vaultUpdateService;
        this.oracleClient = oracleClient;
    }

    public DBHandler() {
    }

    public void handleCreationOrder(Long id) {
        try {
            final Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Entity not " +
                    "found " + id));
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



                SortedMap<String, Object> creds = new TreeMap<>();
                creds.put("username", results.get("username"));
                creds.put("password", results.get("password"));

                final String vaultBasePath = "oracle/" + (inputs.get("environmentClass").equals("p") ? "prod" : "dev") + "/";
                final String databaseName = inputs.get("databaseName").toLowerCase();
                final String vaultCredentialsPath = vaultBasePath + "creds/" + databaseName + "-user";
                final String vaultConfigPath = vaultBasePath + "config/" + databaseName;

                log.info("Writing database credentials to vault at " + vaultCredentialsPath);
                vaultUpdateService.writeSecrets(vaultCredentialsPath, creds);

                SortedMap<String, Object> configData = new TreeMap<>();
                configData.put("jdbc_url", connectionUrl);
                log.info("Writing database connection config to vault at " + vaultConfigPath);
                vaultUpdateService.writeSecrets(vaultConfigPath, configData);

                final String adjustedCredentialsPath = vaultCredentialsPath.replace("oracle/", "oracle/data/");

                final ResourcePayload fasitDbResource = createFasitResourcePayload(connectionUrl, results, inputs, adjustedCredentialsPath + "/password");
                Optional<String> createdResourceId = fasitUpdateService.createResource(fasitDbResource, order);

                createdResourceId.ifPresent(fasitId -> results.put(FASIT_ID, fasitId));
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
            Map<String,String> alterCommands = new HashMap<>();
            alterCommands.put("default tablespace", String.format("ALTER USER \"%s\"  DEFAULT TABLESPACE \"%s\" QUOTA UNLIMITED ON \"%s\"", username.toUpperCase(), username.toUpperCase(), username.toUpperCase()));
            alterCommands.put("password life time", "ALTER PROFILE \"DEFAULT\" LIMIT PASSWORD_LIFE_TIME UNLIMITED");
            alterCommands.put("profile", String.format("ALTER USER \"%s\" PROFILE \"C##_NAV_APP_PROFILE\"", username.toUpperCase()));
            alterCommands.put("revoke dba", String.format("REVOKE DBA FROM \"%s\"", username.toUpperCase()));
            for (Map.Entry commandEntry : alterCommands.entrySet()) {
                log.debug("SQL to execute: " + commandEntry.getValue().toString());
                orderRepository.save(order.addStatuslogInfo("Updating " + commandEntry.getKey().toString()));
                connection.prepareStatement(commandEntry.getValue().toString()).execute();
            }
        } catch (SQLException se) {
            orderRepository.save(order.addStatuslogError("Could not change user settings, this must be done manually"));
            log.error("Unable to update user settings for {}", username, se);
        }
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
            final Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Entity not " +
                    "found " + id));
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

    protected static ResourcePayload createFasitResourcePayload(String connectionUrl, DBOrderResult results, DBOrderInput inputs, String vaultpath) {
        final Map<String, String> properties = new HashMap<>();
        properties.put("url", connectionUrl);
        properties.put("username", results.get(USERNAME));
        properties.put("oemEndpoint", results.get(OEM_ENDPOINT));

        ScopePayload scope = new ScopePayload(
                inputs.get(ENVIRONMENT_CLASS))
                .environment(inputs.get(ENVIRONMENT_NAME))
                .application(inputs.get(APPLICATION_NAME));

        ResourcePayload payload = new ResourcePayload()
                .withType(ResourceType.datasource)
                .withAlias(results.get(FASIT_ALIAS))
                .withProperties(properties)
                .withScope(scope)
                .withVaultSecret("password", vaultpath);

        return payload;
    }
}
