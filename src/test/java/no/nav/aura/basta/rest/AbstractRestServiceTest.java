package no.nav.aura.basta.rest;

import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import io.restassured.RestAssured;
import no.nav.aura.basta.StandaloneBastaJettyRunner;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {StandaloneBastaJettyRunner.class})
public abstract class AbstractRestServiceTest {

    @TestConfiguration
    public static class MockRestClientConfiguration {
        @Bean
        RestClient restClient() {
            return Mockito.mock(RestClient.class);
        }
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected RestClient restClient;
    
    @BeforeAll
    public static void setUpRestTest() {
        RestAssured.port = 1337;
    }

    
    @BeforeEach
    public void initMocks() {
        Mockito.reset(restClient);
    }

    protected Order getCreatedOrderFromResponseLocation(long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        MatcherAssert.assertThat(order, notNullValue());
        return order;
	}

}
