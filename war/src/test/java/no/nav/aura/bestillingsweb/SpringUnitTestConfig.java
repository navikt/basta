package no.nav.aura.bestillingsweb;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

// TODO @EnableTransactionManagement
@Configuration
@ComponentScan(basePackageClasses = ComponentScanBase.class, excludeFilters = @Filter(Configuration.class))
@ImportResource({ "classpath:spring-security-unit-test.xml", "classpath:spring-security-web.xml" })
public class SpringUnitTestConfig {

}
