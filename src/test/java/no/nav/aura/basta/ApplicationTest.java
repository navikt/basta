package no.nav.aura.basta;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import io.restassured.RestAssured;
import jakarta.inject.Inject;
import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.repository.OrderRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = StandaloneBastaJettyRunner.class)
public class ApplicationTest {

    @Inject
    protected OrderRepository orderRepository;

    @Inject
    protected FasitRestClient fasitRestClient;

    protected RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);

    @MockitoBean
    protected FasitUpdateService fasitUpdateService;

    @LocalServerPort
    private int localServerPort;

    @BeforeEach
    public void initMocks() {
        RestAssured.port = localServerPort;
        Mockito.reset(fasitRestClient);
        Mockito.reset(mockRestTemplate);
        fasitRestClient.setRestTemplate(mockRestTemplate);
    }

}