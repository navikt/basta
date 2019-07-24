package no.nav.aura.basta.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.result.database.DBOrderResult;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.junit.Test;

public class DBHandlerTest {
    @Test
    public void passwordIsRemoved() {
        final Order order = createOrder();
        assertEquals("password should be in results map", "p455w0rd", order.getResults().get("password"));
        final Order orderAfterPasswordRemoval = DBHandler.removePasswordFrom(order);
        assertNull("password should be removed from results map", orderAfterPasswordRemoval.getResults().get("password"));
    }

    @Test
    public void createsCorrectFasitResource() {
        final Order order = createOrder();
        final ResourcePayload fasitResource = DBHandler.createFasitResourcePayload("connectionurl", order.getResultAs(DBOrderResult.class), order.getInputAs(DBOrderInput.class));
        assertEquals("alias is correct", "appDB", fasitResource.alias);
        assertEquals("scoped to application", "app", fasitResource.scope.application);
        assertEquals("scoped to environment", "env", fasitResource.scope.environment);
        assertEquals("secret is set correctly", "p455w0rd", fasitResource.secrets.get("password").value);
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