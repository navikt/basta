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
import java.net.URI;
import java.net.URISyntaxException;
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
            @Value("${bastaDB_url}") String dbUrl,
            @Value("${bastaDB_username}") String dbUsername,
            @Value("${bastaDB_password}") String dbPassword) {
        try {

            new Resource("java:/jdbc/bastaDB", createDataSource(dbUrl.contains("oracle") ? "oracle" : "DB2", dbUrl, dbUsername, dbPassword));
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
            @Value("${security_CA_adeo_url}") String security_CA_adeo_url,
            @Value("${security_CA_adeo_username}") String security_CA_adeo_username,
            @Value("${security_CA_adeo_password}") String security_CA_adeo_password,
            @Value("${security_CA_preprod_url}") String security_CA_preprod_url,
            @Value("${security_CA_preprod_username}") String security_CA_preprod_username,
            @Value("${security_CA_preprod_password}") String security_CA_preprod_password,
            @Value("${security_CA_test_url}") String security_CA_test_url,
            @Value("${security_CA_test_username}") String security_CA_test_username,
            @Value("${security_CA_test_password}") String security_CA_test_password) {
            System.setProperty("security_CA_adeo_url", security_CA_adeo_url);
        System.setProperty("security_CA_adeo_username", security_CA_adeo_username);
        System.setProperty("security_CA_adeo_password", security_CA_adeo_password);
        System.setProperty("security_CA_preprod_url", security_CA_preprod_url);
        System.setProperty("security_CA_preprod_username", security_CA_preprod_username);
        System.setProperty("security_CA_preprod_password", security_CA_preprod_password);
        System.setProperty("security_CA_adsecurity_CA_test_urleo_url", security_CA_test_url);
        System.setProperty("security_CA_test_username", security_CA_test_username);
        System.setProperty("security_CA_test_password", security_CA_test_password);
        return new CertificateService();
    }

    @Bean
    public ActiveDirectory getActiveDirectory() {
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
