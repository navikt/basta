package no.nav.aura.basta;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import jakarta.inject.Inject;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.repository.OrderRepository;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = StandaloneBastaJettyRunner.class)
public class ApplicationTest {

    @Inject
    protected OrderRepository orderRepository;

    @Autowired
    protected RestClient restClient;
    
    @BeforeAll
    public static void setup() {
        RestAssured.port = 1337;
    }
    
    @BeforeEach
    public void initMocks() {
        Mockito.reset(restClient);
    }

}
