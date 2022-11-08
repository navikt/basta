package no.nav.aura.basta.backend;

import java.util.HashMap;
import java.util.Map;

import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.result.database.DBOrderResult;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DBHandlerTest {
    @Test
    public void passwordIsRemoved() {
        final Order order = createOrder();
        assertEquals("p455w0rd", order.getResults().get("password"), "password should be in results map");
        final Order orderAfterPasswordRemoval = DBHandler.removePasswordFrom(order);
        assertNull(orderAfterPasswordRemoval.getResults().get("password"), "password should be removed from results map");
    }

    @Test
    public void createsCorrectFasitResource() {
        final Order order = createOrder();
        final ResourcePayload fasitResource = DBHandler.createFasitResourcePayload("connectionurl", order.getResultAs(DBOrderResult.class), order.getInputAs(DBOrderInput.class), "/oracle/data/dev/creds/mydb/password");
        assertEquals("appDB", fasitResource.alias, "alias is correct");
        assertEquals("app", fasitResource.scope.application, "scoped to application");
        assertEquals("env", fasitResource.scope.environment, "scoped to environment");
        assertEquals("/oracle/data/dev/creds/mydb/password", fasitResource.secrets.get("password").vaultpath, "secret is set correctly");
    }

    private static Order createOrder() {
        final Map input = new HashMap<>();
        input.put("environmentName", "env");
        input.put("applicationName", "app");
        final Order order = new Order(OrderType.OracleDB, OrderOperation.CREATE, input);
        final Map results = new HashMap<>();
        results.put("password", "p455w0rd");
        results.put("fasitAlias", "appDB");
        order.setResults(results);
        return order;
    }
}