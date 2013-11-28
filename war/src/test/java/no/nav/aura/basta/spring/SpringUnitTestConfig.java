package no.nav.aura.basta.spring;

import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import no.nav.aura.basta.rootpackage;
import no.nav.aura.envconfig.client.FasitRestClient;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration
@ComponentScan(basePackageClasses = rootpackage.class, excludeFilters = @Filter(Configuration.class))
@Import(SpringDbConfig.class)
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

    @Bean
    public DataSource getDataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    public FasitRestClient getFasitRestClient() {
        return mock(FasitRestClient.class);
    }

}
