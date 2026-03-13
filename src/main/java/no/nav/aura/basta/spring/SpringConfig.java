package no.nav.aura.basta.spring;

import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import no.nav.aura.basta.backend.mq.MqAdminUser;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.SecurityConfiguration;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.security.TrustStoreHelper;
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

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "no.nav.aura.basta")
@Import({SpringDbConfig.class, SpringSecurityConfig.class, VaultConfig.class})
public class SpringConfig {

    static {
        // TODO We don't trust the certificates of orchestrator in test (but in prod)
        TrustStoreHelper.configureTrustStore();
    }

    @Bean
    ServletRegistrationBean<Servlet> metricsServlet() {
    	PrometheusMetricsServlet metricServlet = new PrometheusMetricsServlet();
    	ServletRegistrationBean<Servlet> srBean = new ServletRegistrationBean<Servlet>();
    	srBean.setServlet((Servlet) metricServlet);
    	srBean.addUrlMappings("/metrics");
        return srBean;
    }

    @Bean
    FilterRegistrationBean<CacheAugmentationFilter> filterRegistrationBean() {
        CacheAugmentationFilter cacheAugmentationFilter = new CacheAugmentationFilter();
        FilterRegistrationBean<CacheAugmentationFilter> filterRegistrationBean = new FilterRegistrationBean<CacheAugmentationFilter>();
        filterRegistrationBean.setFilter(cacheAugmentationFilter);
        filterRegistrationBean.addUrlPatterns("*.js", "*.html");
        return filterRegistrationBean;
    }

    @Bean
    Filter openEMinViewFilter() {
        return new OpenEntityManagerInViewFilter();
    }

    @Bean
    Filter mdcEnrichmentFilter() {
        return new MdcEnrichmentFilter();
    }

    @Bean(name = "securityConfiguration")
    SecurityConfiguration securityConfiguration(@Value("${security_CA_adeo_url}") String adeoUrl,
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
    @DependsOn("securityConfiguration")
    ActiveDirectory getActiveDirectory(@Value("${BASTA_OPERATIONS_GROUPS}") String operationGroups,
                                    @Value("${BASTA_PRODOPERATIONS_GROUPS}") String prodOperationGroups,
                                    @Value("${BASTA_SUPERUSER_GROUPS}") String superUserGroups) {
        return new ActiveDirectory(operationGroups, prodOperationGroups, superUserGroups);
    }

    @Bean
    @DependsOn("securityConfiguration")
    CertificateService getCertificateService() {
        return new CertificateService();
    }

    @Bean
    MqService getMqService(@Value("${BASTA_MQ_U_USERNAME}") String uUsername,
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
    OrchestratorClient getOrchestratorClient(
            @Value("${rest_orchestrator_provision_url}") URL provisionUrl,
            @Value("${rest_orchestrator_decomission_url}") URL decomissionUrl,
            @Value("${rest_orchestrator_startstop_url}") URL startstopUrl,
            @Value("${user_orchestrator_username}") String username,
            @Value("${user_orchestrator_password}") String password) {
        return new OrchestratorClient(provisionUrl, decomissionUrl, startstopUrl, username, password);
    }

    @Bean
    static BeanFactoryPostProcessor init() {
//        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
//        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        PropertySourcesPlaceholderConfigurer propertyConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertyConfigurer.setPropertySources(new MutablePropertySources());
        return propertyConfigurer;
    }

    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Bean
    DataSource getDataSource(
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
