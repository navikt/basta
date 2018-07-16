package no.nav.aura.basta.rest.database;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.PostgreSQLClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-security-unit-test.xml")
public class PostgreSQLOrderRestServiceTest {
    @Inject
    private AuthenticationManager authenticationManager;

    @Test
    public void orderDatabase() {
        Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("prodadmin", "prodadmin"));
        SecurityContextHolder.getContext().setAuthentication(token);

        OrderRepository orderRepository = mock(OrderRepository.class);
        FasitUpdateService fasitUpdateService = mock(FasitUpdateService.class);
        PostgreSQLClient postgreSQLClient = mock(PostgreSQLClient.class);

        PostgreSQLOrderRestService service = new PostgreSQLOrderRestService(
                orderRepository,
                fasitUpdateService,
                postgreSQLClient
        );

        Map<String, String> request = new HashMap<>();
        request.put("nodeType", "DB_POSTGRESQL");
        request.put("environmentClass", "u");
        request.put("environmentName", "u3");
        request.put("zone", "fss");
        request.put("applicationName", "myapp");
        request.put("databaseName", "MYAPP_U3");
        request.put("fasitAlias", "myappDBPSQL");

        PostgreSQLClient.CreateDBResponse provisionResponse = new PostgreSQLClient.CreateDBResponse();
        provisionResponse.server = "someserver:5432";
        provisionResponse.username = "databaseuser";
        provisionResponse.password = "databasepassword";
        provisionResponse.db_name = "MYAPP_U3";
        provisionResponse.version = "10.4";
        when(postgreSQLClient.createDatabase("MYAPP_U3", "u", "fss")).thenReturn(provisionResponse);

        Order order = new Order(OrderType.DB, OrderOperation.CREATE, request);
        order.setStatus(OrderStatus.SUCCESS);
        order.addStatuslogInfo("Database created.");
        Map<String, String> orderResults = new HashMap<>();
        orderResults.put("db_name", "MYAPP_U3");
        orderResults.put("version", "10.4");
        orderResults.put("username", "databaseuser");
        orderResults.put("password", "******");
        orderResults.put("server", "someserver:5432");
        order.setResults(orderResults);

        order.setId(123L);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Response result = service.createPostgreSQLDB(request);

        verify(orderRepository, times(1)).save(order);

        ArgumentCaptor<ResourceElement> argument = ArgumentCaptor.forClass(ResourceElement.class);

        verify(fasitUpdateService, times(1)).createResource(argument.capture(), Matchers.eq(order));

        ResourceElement fasitResource = argument.getValue();
        Assert.assertEquals(ResourceTypeDO.DataSource, fasitResource.getType());
        Assert.assertEquals("myappDBPSQL", fasitResource.getAlias());
        Assert.assertEquals("someserver:5432", fasitResource.getPropertyString("hosts"));
        Assert.assertEquals("databaseuser", fasitResource.getPropertyString("username"));
        Assert.assertEquals("databasepassword", fasitResource.getPropertyString("password"));
        Assert.assertEquals("u3", fasitResource.getEnvironmentName());
        Assert.assertEquals("u", fasitResource.getEnvironmentClass());
        Assert.assertEquals("myapp", fasitResource.getApplication());

        Assert.assertEquals(200, result.getStatus());
        Assert.assertEquals("{\"id\": 123}", result.getEntity());
    }
}
