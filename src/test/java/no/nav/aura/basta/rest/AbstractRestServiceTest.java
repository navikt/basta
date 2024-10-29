package no.nav.aura.basta.rest;

import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.envconfig.client.FasitRestClient;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public abstract class AbstractRestServiceTest {

    @Inject
    protected AuthenticationManager authenticationManager;

    @Inject
    protected OrderRepository orderRepository;

    protected RestClient fasit;
    protected FasitRestClient deprecatedFasitRestClient;

    @BeforeEach
    public void initMocks() {
        fasit = Mockito.mock(RestClient.class);
        deprecatedFasitRestClient = Mockito.mock(FasitRestClient.class);
    }

    protected void login() {
        Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("user", "user"));
        SecurityContextHolder.getContext().setAuthentication(token);
    }
    
    protected Order getCreatedOrderFromResponseLocation(Response response) {
        Long orderId = RestServiceTestUtils.getOrderIdFromMetadata(response);
        Order order = orderRepository.findById(orderId).orElse(null);
        MatcherAssert.assertThat(order, notNullValue());
        return order;
    }

}
