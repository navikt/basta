package no.nav.aura.basta.rest;

import static org.hamcrest.Matchers.notNullValue;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import io.restassured.RestAssured;
import no.nav.aura.basta.StandaloneBastaJettyRunner;
import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {StandaloneBastaJettyRunner.class})
@ExtendWith(MockitoExtension.class)
public abstract class AbstractRestServiceTest {

//    @Autowired
//    protected TestRestTemplate restTemplate;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @Autowired
    protected OrderRepository orderRepository;
   
    @MockitoBean
	protected RestTemplate restTemplate;
    
    @Value("${local.server.port}")
    private int localServerPort;

    protected FasitRestClient fasitRestClient;
    protected FasitUpdateService fasitUpdateService;
    
    @BeforeAll
    public void setUpRestTest() {
        RestAssured.port = localServerPort;
        
        // Get the FasitRestClient spy from the context
        fasitRestClient = applicationContext.getBean(FasitRestClient.class);
        fasitUpdateService = applicationContext.getBean(FasitUpdateService.class);
        
        // Inject the mocked RestTemplate into ALL RestClient subclass beans in the context
        // This covers FasitRestClient AND FasitUpdateService (which also extends RestClient)
        applicationContext.getBeansOfType(RestClient.class).values().forEach(client -> {
            ReflectionTestUtils.setField(client, "restTemplate", restTemplate);
        });
    }

    protected Order getCreatedOrderFromResponseLocation(long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        MatcherAssert.assertThat(order, notNullValue());
        return order;
	}

}
