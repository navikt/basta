package no.nav.aura.basta.spring;

import java.sql.SQLException;

import javax.sql.DataSource;

import no.nav.aura.basta.rootpackage;
import no.nav.aura.envconfig.client.FasitRestClient;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ComponentScan(basePackageClasses = rootpackage.class, excludeFilters = @Filter(Configuration.class))
@Import(SpringDbConfig.class)
@ImportResource({ "classpath:spring-security.xml", "classpath:spring-security-web.xml" })
public class SpringConfig {

    // TODO jndi-based
    @Bean
    public DataSource getDataSource() throws SQLException {
        // return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:h2:mem:");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setMaxWait(20000);
        return ds;
    }

    // @Bean
    // public DataSource dataSource() {
    // JndiObjectFactoryBean jndiObjectFactoryBean;
    // try {
    // jndiObjectFactoryBean = new JndiObjectFactoryBean();
    // jndiObjectFactoryBean.setJndiName("java:/jdbc/envconfDB");
    // jndiObjectFactoryBean.setExpectedType(DataSource.class);
    // jndiObjectFactoryBean.afterPropertiesSet();
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // return (DataSource) jndiObjectFactoryBean.getObject();
    // }

    @Bean
    public FasitRestClient getFasitRestClient(
            @Value("${fasit.rest.api.url}") String fasitBaseUrl,
            @Value("${fasit.rest.api.username}") String fasitUsername,
            @Value("${fasit.rest.api.password}") String fasitPassword) {
        return new FasitRestClient(fasitBaseUrl, fasitUsername, fasitPassword);
    }

    @Bean
    public BeanFactoryPostProcessor init() {
        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }

}
