package no.nav.aura.basta.spring;

import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.RootPackage;
import no.nav.aura.envconfig.client.FasitRestClient;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(basePackageClasses = RootPackage.class, excludeFilters = @Filter(Configuration.class))
@Import(SpringDbConfig.class)
@ImportResource({ "classpath:spring-security-unit-test.xml", "classpath:spring-security-web.xml" })
public class KarmaTestConfig {

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
        System.setProperty("basta.db.type", "h2");
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    public OrchestratorService getOrchestratorService() {
        return mock(OrchestratorService.class);
    }

    @Bean
    public FasitRestClient getFasitRestClient() {
        return mock(FasitRestClient.class);
    }

    public static DataSource createDataSource(String type, String url, String username, String password) {
        if ("h2".equalsIgnoreCase(type)) {
            System.setProperty("useH2", "true");
        }
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaxWait(20000);
        System.out.println("using database " + ds.getUsername() + "@" + ds.getUrl());
        return ds;
    }

}
