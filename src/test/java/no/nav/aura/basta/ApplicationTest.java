package no.nav.aura.basta;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.inject.Inject;
import no.nav.aura.basta.repository.OrderRepository;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = StandaloneBastaJettyRunner.class)
public class ApplicationTest {

    @Inject
    public TestRestTemplate testRestTemplate;

    @Inject
    public OrderRepository orderRepository;

    @BeforeAll
    public static void setup() {
        // Default value has changed in Spring5, need to allow overriding of beans in tests
//        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
        RestAssured.port = 1337;
    }

    @Test
    public void verifyRestTemplate() {
        assertNotNull(testRestTemplate);
    }
}
