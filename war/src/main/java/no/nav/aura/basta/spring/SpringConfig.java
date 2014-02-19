package no.nav.aura.basta.spring;

import javax.sql.DataSource;

import no.nav.aura.basta.RootPackage;
import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.backend.OrchestratorServiceImpl;
import no.nav.aura.basta.vmware.TrustStoreHelper;
import no.nav.aura.basta.vmware.orchestrator.WorkflowExecutor;
import no.nav.aura.envconfig.client.FasitRestClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jndi.JndiObjectFactoryBean;

@Configuration
@ComponentScan(basePackageClasses = RootPackage.class, excludeFilters = @Filter(Configuration.class))
@Import(SpringDbConfig.class)
@ImportResource({ "classpath:spring-security.xml", "classpath:spring-security-web.xml" })
public class SpringConfig {

    {
        // TODO We don't trust the certificates of orchestrator (we will in prod)
        TrustStoreHelper.configureTrustStoreWithProvidedTruststore();
    }

    @Bean
    public DataSource dataSource() {
        try {
            JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
            jndiObjectFactoryBean.setJndiName("java:/jdbc/bastaDB");
            jndiObjectFactoryBean.setExpectedType(DataSource.class);
            jndiObjectFactoryBean.afterPropertiesSet();
            return (DataSource) jndiObjectFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public FasitRestClient getFasitRestClient(
            @Value("${fasit.rest.api.url}") String fasitBaseUrl,
            @Value("${fasit.rest.api.username}") String fasitUsername,
            @Value("${fasit.rest.api.password}") String fasitPassword) {
        return new FasitRestClient(fasitBaseUrl, fasitUsername, fasitPassword);
    }

    @Bean
    public OrchestratorService getOrchestratorService(WorkflowExecutor workflowExecutor) {
        return new OrchestratorServiceImpl(workflowExecutor);
    }

    @Bean
    public BeanFactoryPostProcessor init() {
        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }

}
