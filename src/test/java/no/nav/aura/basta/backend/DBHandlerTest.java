package no.nav.aura.basta.backend;

import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcePayload;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.result.database.DBOrderResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class DBHandlerTest {
    @Test
    public void passwordIsRemoved() {
        final Order order = createOrder();
        Assertions.assertEquals("p455w0rd", order.getResults().get("password"), "password should be in results map");
        final Order orderAfterPasswordRemoval = DBHandler.removePasswordFrom(order);
        Assertions.assertNull(orderAfterPasswordRemoval.getResults().get("password"), "password should be removed from results map");
    }

    @Test
    public void createsCorrectFasitResource() {
        final Order order = createOrder();
        final ResourcePayload fasitResource = DBHandler.createFasitResourcePayload("connectionurl", order.getResultAs(DBOrderResult.class), order.getInputAs(DBOrderInput.class), "/oracle/data/dev/creds/mydb/password");
        Assertions.assertEquals("appDB", fasitResource.alias, "alias is correct");
        Assertions.assertEquals("app", fasitResource.scope.application, "scoped to application");
        Assertions.assertEquals("env", fasitResource.scope.environment, "scoped to environment");
        Assertions.assertEquals("/oracle/data/dev/creds/mydb/password", fasitResource.secrets.get("password").vaultpath, "secret is set correctly");
    }

    private static Order createOrder() {
        final Map<String, String> input = new HashMap<>();
        input.put("environmentName", "env");
        input.put("applicationName", "app");
        final Order order = new Order(OrderType.OracleDB, OrderOperation.CREATE, input);
        final Map<String, String> results = new HashMap<>();
        results.put("password", "p455w0rd");
        results.put("fasitAlias", "appDB");
        order.setResults(results);
        return order;
    }

}