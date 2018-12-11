package no.nav.aura.basta;

import com.jayway.restassured.RestAssured;
import no.nav.aura.basta.repository.OrderRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = StandaloneBastaJettyRunner.class)
public class ApplicationTest {

    @Inject
    public TestRestTemplate testRestTemplate;

    @Inject
    public OrderRepository orderRepository;

    @BeforeClass
    public static void setup() {
        RestAssured.port = 1337;
    }

    @Test
    public void verifyRestTemplate() {
        assertNotNull(testRestTemplate);
    }
}
