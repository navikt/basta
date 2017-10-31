package no.nav.aura.basta.rest;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.envconfig.client.FasitRestClient;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public abstract class AbstractRestServiceTest {

    @Inject
    protected AuthenticationManager authenticationManager;

    @Inject
    protected OrderRepository orderRepository;

    protected FasitRestClient fasit;

    @Before
    public void initMocks() {
        fasit = Mockito.mock(FasitRestClient.class);
    }

    protected void login(String userName, String password) {
        Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        SecurityContextHolder.getContext().setAuthentication(token);
    }
    
    protected Order getCreatedOrderFromResponseLocation(Response response) {
        Long orderId = RestServiceTestUtils.getOrderIdFromMetadata(response);
        Order order = orderRepository.findOne(orderId);
        assertThat(order, notNullValue());
        return order;
    }

}
