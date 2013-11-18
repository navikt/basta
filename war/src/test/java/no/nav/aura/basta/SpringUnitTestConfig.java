package no.nav.aura.basta;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

// TODO @EnableTransactionManagement
@Configuration
@ComponentScan(basePackageClasses = ComponentScanBase.class, excludeFilters = @Filter(Configuration.class))
@ImportResource({ "classpath:spring-security-unit-test.xml", "classpath:spring-security-web.xml" })
public class SpringUnitTestConfig {

    @Bean
    public BeanFactoryPostProcessor init() {
        System.setProperty("ws.orchestrator.url", "https://someserver/vmware-vmo-webcontrol/webservice");
        System.setProperty("user.orchestrator.username", "orcname");
        System.setProperty("user.orchestrator.password", "secret");

        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }
}
