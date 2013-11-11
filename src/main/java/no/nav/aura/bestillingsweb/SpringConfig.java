package no.nav.aura.bestillingsweb;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
// TODO @EnableTransactionManagement
@ComponentScan(basePackageClasses = ComponentScanBase.class)
@ImportResource({ "classpath:spring-security.xml", "classpath:spring-security-web.xml" })
public class SpringConfig {

}
