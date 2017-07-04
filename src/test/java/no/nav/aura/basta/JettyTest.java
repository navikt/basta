package no.nav.aura.basta;

import no.nav.aura.basta.repository.OrderRepository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.security.authentication.AuthenticationManager;

abstract public class JettyTest {

    protected static JunitBastaJettyRunner jetty;
    protected static OrderRepository repository;
    protected static AuthenticationManager authenticationManager;

    // protected static InsideJobService testBean;

    @BeforeClass
    public static void setUpJetty() throws Exception {
        jetty = new JunitBastaJettyRunner();
        jetty.start();

        authenticationManager = jetty.getSpringContext().getBean("authenticationManager", AuthenticationManager.class);
        repository = jetty.getSpringContext().getBean(OrderRepository.class);
        // testBean = jetty.getSpringContext().getBean(InsideJobService.class);
    }

    @AfterClass
    public static void close() {
        jetty.stop();
    }

}
