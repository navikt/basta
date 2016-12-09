package no.nav.aura.basta.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(StandaloneRunnerTestConfig.class)
public class JunitJettyTestConfig {

    private static final Logger logger = LoggerFactory.getLogger(JunitJettyTestConfig.class);

}
