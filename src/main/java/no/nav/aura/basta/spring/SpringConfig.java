package no.nav.aura.basta.spring;

import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet;
import jakarta.servlet.Servlet;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.deprecated.FasitRestClient;
import no.nav.aura.basta.backend.mq.MqAdminUser;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.SecurityConfiguration;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.rest.FasitLookupService;
import no.nav.aura.basta.util.CacheAugmentationFilter;
import no.nav.aura.basta.util.MdcEnrichmentFilter;
import oracle.net.ns.SQLnetDef;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

import javax.sql.DataSource;

import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "no.nav.aura.basta")
@Import({SpringDbConfig.class, SpringSecurityConfig.class, VaultConfig.class})
public class SpringConfig {

    @Bean
    public ServletRegistrationBean<Servlet> metricsServlet() {
    	PrometheusMetricsServlet metricServlet = new PrometheusMetricsServlet();
    	ServletRegistrationBean<Servlet> srBean = new ServletRegistrationBean<Servlet>();
    	srBean.setServlet((Servlet) metricServlet);
    	srBean.addUrlMappings("/metrics");
        return srBean;
    }

    @Bean
    public FilterRegistrationBean<CacheAugmentationFilter> filterRegistrationBean() {
        CacheAugmentationFilter cacheAugmentationFilter = new CacheAugmentationFilter();
        FilterRegistrationBean<CacheAugmentationFilter> filterRegistrationBean = new FilterRegistrationBean<CacheAugmentationFilter>();
        filterRegistrationBean.setFilter(cacheAugmentationFilter);
        filterRegistrationBean.addUrlPatterns("*.js", "*.html");
        return filterRegistrationBean;
    }

    @Bean
    public jakarta.servlet.Filter openEMinViewFilter() {
        return new OpenEntityManagerInViewFilter();
    }

    @Bean
    public jakarta.servlet.Filter mdcEnrichmentFilter() {
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
            @Value("${fasit_lifecycle_v1_url}") String fasitLifecycleUrl,
            @Value("${fasit_environments_v2_url}") String fasitEnvironmentsUrl,
            @Value("${fasit_nodes_v2_url}") String fasitNodesUrl,
            @Value("${fasit_applications_v2_url}") String fasitApplicationsUrl,
            @Value("${fasit_scopedresource_v2_url}") String fasitScopedUrl,
            @Value("${srvbasta_username}") String fasitUsername,
            @Value("${srvbasta_password}") String fasitPassword,
            @Value("${fasit_applicationinstances_v2_url}") String  fasitApplicatinInstancesUrl) {
        System.setProperty("fasit_rest_api_url", fasitBaseUrl);
        System.setProperty("fasit_resources_v2_url", fasitResourcesUrl);
        System.setProperty("fasit_lifecycle_v1_url", fasitLifecycleUrl);
        System.setProperty("fasit_environments_v2_url", fasitEnvironmentsUrl);
        System.setProperty("fasit_nodes_v2_url", fasitNodesUrl);
        System.setProperty("fasit_applications_v2_url", fasitApplicationsUrl);
        System.setProperty("fasit_scopedresource_v2_url", fasitScopedUrl);
        System.setProperty("fasit_applicationinstances_v2_url", fasitApplicatinInstancesUrl);
        FasitRestClient fasitRestClient = new FasitRestClient(fasitBaseUrl, fasitUsername, fasitPassword);
        fasitRestClient.useCache(false);
        return fasitRestClient;
    }



    @Bean
    public RestClient getRestClient(
            @Value("${fasit_resources_v2_url}") String fasitResourcesUrl,
            @Value("${fasit_scopedresource_v2_url}") String fasitScopedUrl,
            @Value("${fasit_applicationinstances_v2_url}") String fasitApplicationInstancesUrl,
            @Value("${fasit_environments_v2_url}") String fasitEnvironmentsUrl,
            @Value("${fasit_nodes_v2_url}") String fasitNodesUrl,
            @Value("${srvbasta_username}") String fasitUsername,
            @Value("${srvbasta_password}") String fasitPassword) {
        return new RestClient(
                fasitResourcesUrl,
                fasitScopedUrl,
                fasitApplicationInstancesUrl,
                fasitEnvironmentsUrl,
                fasitNodesUrl,
                fasitUsername,
                fasitPassword
                );
    }

    @Bean
    public OracleClient getOracleClient(
            @Value("${oem_url}") String oemUrl,
            @Value("${oem_username}") String oemUsername,
            @Value("${oem_password}") String oemPassword) throws URISyntaxException {
        return new OracleClient(oemUrl, oemUsername, oemPassword);
    }

    @Bean
    @DependsOn("securityConfiguration")
    public ActiveDirectory getActiveDirectory(@Value("${BASTA_OPERATIONS_GROUPS}") String operationGroups,
                                              @Value("${BASTA_PRODOPERATIONS_GROUPS}") String prodOperationGroups,
                                              @Value("${BASTA_SUPERUSER_GROUPS}") String superUserGroups) {
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
            @Value("${user_orchestrator_username}") String username,
            @Value("${user_orchestrator_password}") String password) {
        return new OrchestratorClient(provisionUrl, decomissionUrl, startstopUrl, username, password);
    }

    @Bean
    public FasitLookupService getFasitProxy(FasitRestClient fasit, RestClient restClient) {
        return new FasitLookupService(fasit, restClient);
    }

    @Bean
    public static BeanFactoryPostProcessor init() {
//        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
//        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        PropertySourcesPlaceholderConfigurer propertyConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertyConfigurer.setPropertySources(new MutablePropertySources());
        return propertyConfigurer;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }



    @Bean
    public DataSource getDataSource(
        @Value("${BASTADB_URL}") String dbUrl,
        @Value("${BASTADB_ONSHOSTS}") String onsHosts,
        @Value("${BASTADB_USERNAME}") String dbUsername,
        @Value("${BASTADB_PASSWORD}") String dbPassword) throws
        SQLException {
        PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        poolDataSource.setURL(dbUrl);
        poolDataSource.setUser(dbUsername);
        poolDataSource.setPassword(dbPassword);
        poolDataSource.setConnectionFactoryClassName(getConnectionFactoryClassName());
        if (dbUrl.toLowerCase().contains("failover")) {
            if (onsHosts != null) {
                poolDataSource.setONSConfiguration("nodes=" + onsHosts);
            }
            poolDataSource.setFastConnectionFailoverEnabled(true);
        }
        Properties connProperties = new Properties();
        connProperties.setProperty(SQLnetDef.TCP_CONNTIMEOUT_STR, "3000");
        connProperties.setProperty("oracle.jdbc.thinForceDNSLoadBalancing", "true");
        // Optimizing UCP behaviour https://docs.oracle.com/database/121/JJUCP/optimize.htm#JJUCP8143
        poolDataSource.setInitialPoolSize(1);
        poolDataSource.setMinPoolSize(1);
        poolDataSource.setMaxPoolSize(50);
        poolDataSource.setMaxConnectionReuseTime(300); // 5min
        poolDataSource.setMaxConnectionReuseCount(100);
        poolDataSource.setConnectionProperties(connProperties);
        return poolDataSource;
    }

    private String getConnectionFactoryClassName() {
        return "oracle.jdbc.pool.OracleDataSource";
    }
}
