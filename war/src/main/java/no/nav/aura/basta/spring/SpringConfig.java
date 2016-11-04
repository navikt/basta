package no.nav.aura.basta.spring;

import no.nav.aura.basta.RootPackage;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.WorkflowExecutor;
import no.nav.aura.basta.rest.FasitLookupService;
import no.nav.aura.basta.security.TrustStoreHelper;
import no.nav.aura.envconfig.client.FasitRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackageClasses = RootPackage.class, excludeFilters = @Filter(Configuration.class))
@Import(SpringDbConfig.class)
@ImportResource({ "classpath:spring-security.xml" })
public class SpringConfig {

    {
        // TODO We don't trust the certificates of orchestrator in test (but in prod)
        TrustStoreHelper.configureTrustStoreWithProvidedTruststore();
    }

    @Bean
    public DataSource getDataSource() {
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
            @Value("${srvbasta.username}") String fasitUsername,
            @Value("${srvbasta.password}") String fasitPassword) {
        FasitRestClient fasitRestClient = new FasitRestClient(fasitBaseUrl, fasitUsername, fasitPassword);
        fasitRestClient.useCache(false);
        return fasitRestClient;
    }

    @Bean
    public OracleClient getOracleClient(
            @Value("${oem.url}") String oemUrl,
            @Value("${oem.username}") String oemUsername,
            @Value("${oem.password}") String oemPassword) {
        return new OracleClient(oemUrl, oemUsername, oemPassword);
    }

    @Bean
    public CertificateService getCertificateService() {
        return new CertificateService();
    }

    @Bean
    public ActiveDirectory getActiveDirectory() {
        return new ActiveDirectory();
    }
    
    @Bean
    public MqService getMqService(){
        return new MqService();
    }

//    @Bean
//    public OrchestratorService getOrchestratorService(WorkflowExecutor workflowExecutor) {
//        return new OrchestratorService(workflowExecutor);
//    }
    
    @Bean
    public FasitLookupService getFasitProxy(FasitRestClient fasit){
        return new FasitLookupService(fasit);
    }

    @Bean
    public static BeanFactoryPostProcessor init() {
        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }

}
