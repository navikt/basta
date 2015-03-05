package no.nav.aura.basta.rest.api;

import no.nav.aura.basta.JunitBastaJettyRunner;
import no.nav.aura.basta.repository.OrderRepository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.security.authentication.AuthenticationManager;

import com.jayway.restassured.RestAssured;

abstract public class RestTest {

    protected static JunitBastaJettyRunner jetty;
    protected static OrderRepository repository;
    protected static AuthenticationManager authenticationManager;

    // protected static InsideJobService testBean;

    @BeforeClass
    public static void setUpJetty() throws Exception {
        jetty = new JunitBastaJettyRunner();
        jetty.start();
        RestAssured.port = jetty.getPort();

        authenticationManager = jetty.getSpringContext().getBean("authenticationManager", AuthenticationManager.class);
        repository = jetty.getSpringContext().getBean(OrderRepository.class);
        // testBean = jetty.getSpringContext().getBean(InsideJobService.class);
    }

    @AfterClass
    public static void close() {
        jetty.stop();
    }

}
