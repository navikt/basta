package no.nav.aura.basta.spring;

import no.nav.aura.basta.RootPackage;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.rest.FasitLookupService;
import no.nav.aura.basta.security.TrustStoreHelper;
import no.nav.aura.envconfig.client.FasitRestClient;
import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.jetty.plus.jndi.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.sql.DataSource;
import java.net.URL;

@Configuration
@ComponentScan(basePackageClasses = RootPackage.class, excludeFilters = @Filter(Configuration.class))
@Import(SpringDbConfig.class)
@ImportResource({"classpath:spring-security.xml"})
public class SpringConfig {

    {
        // TODO We don't trust the certificates of orchestrator in test (but in prod)
        TrustStoreHelper.configureTrustStoreWithProvidedTruststore();
    }

    @Bean
    public DataSource getDataSource(
            @Value("${basta_db_type}") String dbType,
            @Value("${basta_db_url}") String dbUrl,
            @Value("${basta_db_username}") String dbUsername,
            @Value("${basta_db_password}") String dbPassword) {
        try {
            new Resource("java:/jdbc/bastaDB", createDataSource(dbType, dbUrl, dbUsername, dbPassword));
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
            @Value("${fasit_rest_api_url}") String fasitBaseUrl,
            @Value("${srvbasta_username}") String fasitUsername,
            @Value("${srvbasta_password}") String fasitPassword) {
        FasitRestClient fasitRestClient = new FasitRestClient(fasitBaseUrl, fasitUsername, fasitPassword);
        fasitRestClient.useCache(false);
        return fasitRestClient;
    }

    @Bean
    public RestClient getRestClient(
            @Value("${srvbasta_username}") String fasitUsername,
            @Value("${srvbasta_password}") String fasitPassword) {
        return new RestClient(fasitUsername, fasitPassword);
    }

    @Bean
    public OracleClient getOracleClient(
            @Value("${oem_url}") String oemUrl,
            @Value("${oem_username}") String oemUsername,
            @Value("${oem_password}") String oemPassword) {
        return new OracleClient(oemUrl, oemUsername, oemPassword);
    }

    @Bean
    public CertificateService getCertificateService(
            @Value("${scep_adeo_no_url}") String scepAdeoUrl,
            @Value("${scep_adeo_no_username}") String scepAdeoUsername,
            @Value("${scep_adeo_no_password}") String scepAdeoPassword,
            @Value("${scep_preprod_local_url}") String scepPreprodUrl,
            @Value("${scep_preprod_local_username}") String scepPreprodUsername,
            @Value("${scep_preprod_local_password}") String scepPreprodPassword,
            @Value("${scep_test_local_url}") String scepTestUrl,
            @Value("${scep_test_local_username}") String scepTestUsername,
            @Value("${scep_test_local_password}") String scepTestPassword) {
        return new CertificateService();
    }

    @Bean
    public ActiveDirectory getActiveDirectory(@Value("${ldap_url}") String ldapUrl,
                                              @Value("${ldap_domain}") String ldapDomain) {

        System.setProperty("ldap_url", ldapUrl);
        System.setProperty("ldap_domain", ldapDomain);
        return new ActiveDirectory();
    }

    @Bean
    public MqService getMqService() {
        return new MqService();
    }

    @Bean
    public OrchestratorClient getOrchestratorClient (
            @Value("${rest_orchestrator_provision_url}") URL provisionUrl,
            @Value("${rest_orchestrator_decomission_url}") URL decomissionUrl,
            @Value("${rest_orchestrator_startstop_url}") URL startstopUrl,
            @Value("${rest_orchestrator_modify_url}") URL modifyUrl,
            @Value("${user_orchestrator_username}") String username,
            @Value("${user_orchestrator_password}") String password) {
        return new OrchestratorClient(provisionUrl, decomissionUrl, startstopUrl, modifyUrl, username, password);
    }

    @Bean
    public FasitLookupService getFasitProxy(FasitRestClient fasit) {
        return new FasitLookupService(fasit);
    }

    @Bean
    public static BeanFactoryPostProcessor init() {
        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public static DataSource createDataSource(String type, String url, String username, String password) {
        //System.setProperty("basta_db_type", type);
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaxWait(20000);
        System.out.println("using database " + ds.getUsername() + "@" + ds.getUrl());
        return ds;
    }
}