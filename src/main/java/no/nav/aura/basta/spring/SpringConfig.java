package no.nav.aura.basta.spring;

import io.prometheus.client.exporter.MetricsServlet;
import no.nav.aura.basta.RootPackage;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.mq.MqAdminUser;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.SecurityConfiguration;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.rest.FasitLookupService;
import no.nav.aura.basta.security.TrustStoreHelper;
import no.nav.aura.basta.util.CacheAugmentationFilter;
import no.nav.aura.basta.util.MdcEnrichmentFilter;
import no.nav.aura.envconfig.client.FasitRestClient;
import oracle.net.ns.SQLnetDef;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.jetty.plus.jndi.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@ComponentScan(basePackageClasses = RootPackage.class, excludeFilters = {@Filter(Configuration.class), @Filter
        (SpringBootApplication.class)})
@Import({SpringDbConfig.class, MetricsConfig.class})
@ImportResource({"classpath:spring-security.xml"})
public class SpringConfig {

    {
        // TODO We don't trust the certificates of orchestrator in test (but in prod)
        TrustStoreHelper.configureTrustStoreWithProvidedTruststore();
    }

    @Bean
    public ServletRegistrationBean metricsServlet() {
        return new ServletRegistrationBean(new MetricsServlet(), "/metrics");
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        CacheAugmentationFilter cacheAugmentationFilter = new CacheAugmentationFilter();
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(cacheAugmentationFilter);
        filterRegistrationBean.addUrlPatterns("*.js", "*.html");
        return filterRegistrationBean;
    }

    @Bean
    public javax.servlet.Filter openEMinViewFilter() {
        return new OpenEntityManagerInViewFilter();
    }

    @Bean
    public javax.servlet.Filter mdcEnrichmentFilter() {
        return new MdcEnrichmentFilter();
    }

    @Bean(name = "securityConfiguration")
    public SecurityConfiguration securityConfiguration(@Value("${security_CA_adeo_url}") String adeoUrl,
                                                           @Value("${security_CA_adeo_username}") String adeoUsername,
                                                           @Value("${security_CA_adeo_password}") String adeoPassword,
                                                           @Value("${security_CA_preprod_url}") String preprodUrl,
                                                           @Value("${security_CA_preprod_username}") String
                                                                   preprodUsername,
                                                           @Value("${security_CA_preprod_password}") String
                                                                   preprodPassword,
                                                           @Value("${security_CA_test_url}") String testUrl,
                                                           @Value("${security_CA_test_username}") String testUsername,
                                                           @Value("${security_CA_test_password}") String testPassword) {

        System.setProperty("security_CA_adeo_url", adeoUrl);
        System.setProperty("security_CA_adeo_username", adeoUsername);
        System.setProperty("security_CA_adeo_password", adeoPassword);
        System.setProperty("security_CA_preprod_url", preprodUrl);
        System.setProperty("security_CA_preprod_username", preprodUsername);
        System.setProperty("security_CA_preprod_password", preprodPassword);
        System.setProperty("security_CA_test_url", testUrl);
        System.setProperty("security_CA_test_username", testUsername);
        System.setProperty("security_CA_test_password", testPassword);

        return new SecurityConfiguration();
    }



    @Bean
    public FasitRestClient getFasitRestClient(
            @Value("${fasit_rest_api_url}") String fasitBaseUrl,
            @Value("${fasit_resources_v2_url}") String fasitResourcesUrl,
            @Value("${fasit_environments_v2_url}") String fasitEnvironmentsUrl,
            @Value("${fasit_nodes_v2_url}") String fasitNodesUrl,
            @Value("${fasit_applications_v2_url}") String fasitApplicationsUrl,
            @Value("${fasit_scopedresource_v2_url}") String fasitScopedUrl,
            @Value("${srvbasta_username}") String fasitUsername,
            @Value("${srvbasta_password}") String fasitPassword) {
        System.setProperty("fasit_rest_api_url", fasitBaseUrl);
        System.setProperty("fasit_resources_v2_url", fasitResourcesUrl);
        System.setProperty("fasit_environments_v2_url", fasitEnvironmentsUrl);
        System.setProperty("fasit_nodes_v2_url", fasitNodesUrl);
        System.setProperty("fasit_applications_v2_url", fasitApplicationsUrl);
        System.setProperty("fasit_scopedresource_v2_url", fasitScopedUrl);
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
    @DependsOn("securityConfiguration")
    public ActiveDirectory getActiveDirectory(@Value("${basta_operations_groups}") String operationGroups,
                                              @Value("${basta_prodoperations_groups}") String prodOperationGroups,
                                              @Value("${basta_superuser_groups}") String superUserGroups) {
        return new ActiveDirectory(operationGroups, prodOperationGroups, superUserGroups);
    }

    @Bean
    @DependsOn("securityConfiguration")
    public CertificateService getCertificateService() {
        return new CertificateService();
    }

    @Bean
    public MqService getMqService(@Value("${BASTA_MQ_U_USERNAME}") String uUsername,
                                  @Value("${BASTA_MQ_T_USERNAME}") String tUsername,
                                  @Value("${BASTA_MQ_Q_USERNAME}") String qUsername,
                                  @Value("${BASTA_MQ_P_USERNAME}") String pUsername,
                                  @Value("${BASTA_MQ_U_PASSWORD}") String uPassword,
                                  @Value("${BASTA_MQ_T_PASSWORD}") String tPassword,
                                  @Value("${BASTA_MQ_Q_PASSWORD}") String qPassword,
                                  @Value("${BASTA_MQ_P_PASSWORD}") String pPassword) {
        Map<EnvironmentClass, MqAdminUser> envCredMap = new HashMap<>();
        envCredMap.put(EnvironmentClass.u, new MqAdminUser(uUsername, uPassword, "SRVAURA.ADMIN"));
        envCredMap.put(EnvironmentClass.t, new MqAdminUser(tUsername, tPassword, "SRVAURA.ADMIN"));
        envCredMap.put(EnvironmentClass.q, new MqAdminUser(qUsername, qPassword, "SRVAURA.ADMIN"));
        envCredMap.put(EnvironmentClass.p, new MqAdminUser(pUsername, pPassword, "SRVAURA.ADMIN"));
        return new MqService(envCredMap);
    }

    @Bean
    public OrchestratorClient getOrchestratorClient(
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

    @Bean
    public DataSource getDataSource(
            @Value("${bastaDB_url}") String dbUrl,
            @Value("${bastaDB_username}") String dbUsername,
            @Value("${bastaDB_password}") String dbPassword) {
        try {
            new Resource("java:/jdbc/bastaDB", createDataSource(dbUrl, dbUsername, dbPassword));
            JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
            jndiObjectFactoryBean.setJndiName("java:/jdbc/bastaDB");
            jndiObjectFactoryBean.setExpectedType(DataSource.class);
            jndiObjectFactoryBean.afterPropertiesSet();
            return (DataSource) jndiObjectFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DataSource createDataSource(String url, String username, String password) throws SQLException {

        PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        poolDataSource.setURL(url);
        poolDataSource.setUser(username);
        poolDataSource.setPassword(password);
        poolDataSource.setConnectionFactoryClassName(getConnectionFactoryClassName());
        /*if(url.toLowerCase().contains("failover")) {
            int onsPort = 6200;
            URI uri = URI.create(url.substring(5));

            System.out.println("Setting up database FCF support");
            poolDataSource.setONSConfiguration("nodes=d26dbfl022.test.local:6200,d26dbfl024.test.local:6200");
            poolDataSource.setFastConnectionFailoverEnabled(true);
        }*/
        Properties connProperties = new Properties();
        connProperties.setProperty(SQLnetDef.TCP_CONNTIMEOUT_STR, "3000");
        connProperties.setProperty("oracle.jdbc.thinForceDNSLoadBalancing", "true");
        // Optimizing UCP behaviour https://docs.oracle.com/database/121/JJUCP/optimize.htm#JJUCP8143
        poolDataSource.setInitialPoolSize(5);
        poolDataSource.setMinPoolSize(2);
        poolDataSource.setMaxPoolSize(20);
        poolDataSource.setMaxConnectionReuseTime(300); // 5min
        poolDataSource.setMaxConnectionReuseCount(100);
        poolDataSource.setConnectionProperties(connProperties);
        return poolDataSource;
    }

    private String getConnectionFactoryClassName() {
        return "oracle.jdbc.pool.OracleDataSource";
    }
}
