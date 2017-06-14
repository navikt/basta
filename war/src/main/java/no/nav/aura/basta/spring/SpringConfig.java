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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;


@Configuration
@ComponentScan(basePackageClasses = RootPackage.class, excludeFilters = @Filter(Configuration.class))
@Import(SpringDbConfig.class)
@PropertySources({
        @PropertySource("file:${app.home}/configuration/environment.properties")
})
@ImportResource({"classpath:spring-security.xml"})
public class SpringConfig {

    {
        // TODO We don't trust the certificates of orchestrator in test (but in prod)
        TrustStoreHelper.configureTrustStoreWithProvidedTruststore();
    }

    @Bean
    public DataSource getDataSource(
            @Value("${bastaDB.url}") String dbUrl,
            @Value("${bastaDB.username}") String dbUsername,
            @Value("${bastaDB.password}") String dbPassword) {
        try {
            new Resource("java:/jdbc/bastaDB", createDataSource("oracle", dbUrl, dbUsername, dbPassword));
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
    public RestClient getRestClient(
            @Value("${srvbasta.username}") String fasitUsername,
            @Value("${srvbasta.password}") String fasitPassword) {
        return new RestClient(fasitUsername, fasitPassword);
    }

    @Bean
    public OracleClient getOracleClient(
            @Value("${oem.url}") String oemUrl,
            @Value("${oem.username}") String oemUsername,
            @Value("${oem.password}") String oemPassword) {
        return new OracleClient(oemUrl, oemUsername, oemPassword);
    }

    @Bean
    public CertificateService getCertificateService(
            @Value("${scep.adeo.no.url}") String scepAdeoUrl,
            @Value("${scep.adeo.no.username}") String scepAdeoUsername,
            @Value("${scep.adeo.no.password}") String scepAdeoPassword,
            @Value("${scep.preprod.local.url}") String scepPreprodUrl,
            @Value("${scep.preprod.local.username}") String scepPreprodUsername,
            @Value("${scep.preprod.local.password}") String scepPreprodPassword,
            @Value("${scep.test.local.url}") String scepTestUrl,
            @Value("${scep.test.local.username}") String scepTestUsername,
            @Value("${scep.test.local.password}") String scepTestPassword) {
        System.setProperty("scep.adeo.no.url", scepAdeoUrl);
        System.setProperty("scep.adeo.no.username", scepAdeoUsername);
        System.setProperty("scep.adeo.no.password", scepAdeoPassword);
        System.setProperty("scep.preprod.local.url", scepPreprodUrl);
        System.setProperty("scep.preprod.local.username", scepPreprodUsername);
        System.setProperty("scep.preprod.local.password", scepPreprodPassword);
        System.setProperty("scep.test.local.url", scepTestUrl);
        System.setProperty("scep.test.local.username", scepTestUsername);
        System.setProperty("scep.test.local.password", scepTestPassword);
        return new CertificateService();
    }

    @Bean
    public ActiveDirectory getActiveDirectory(@Value("${ldap.url}") String ldapUrl,
                                              @Value("${ldap.domain}") String ldapDomain) {

        System.setProperty("ldap.url", ldapUrl);
        System.setProperty("ldap.domain", ldapDomain);
        return new ActiveDirectory();
    }

    @Bean
    public MqService getMqService() {
        return new MqService();
    }

    @Bean
    public OrchestratorClient getOrchestratorClient (
            @Value("${rest.orchestrator.provision.url}") URL provisionUrl,
            @Value("${rest.orchestrator.decomission.url}") URL decomissionUrl,
            @Value("${rest.orchestrator.startstop.url}") URL startstopUrl,
            @Value("${rest.orchestrator.modify.url}") URL modifyUrl,
            @Value("${user.orchestrator.username}") String username,
            @Value("${user.orchestrator.password}") String password) {
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
        System.setProperty("basta.db.type", type);
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaxWait(20000);
        System.out.println("using database " + ds.getUsername() + "@" + ds.getUrl());
        return ds;
    }
}
