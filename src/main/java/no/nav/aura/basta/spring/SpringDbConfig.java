package no.nav.aura.basta.spring;

import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import no.nav.aura.basta.RootPackage;

import oracle.net.ns.SQLnetDef;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.flywaydb.core.Flyway;
import org.hibernate.cache.ehcache.EhCacheRegionFactory;
import org.hibernate.ejb.HibernatePersistence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableJpaRepositories(basePackageClasses = RootPackage.class)
@EnableTransactionManagement
public class SpringDbConfig {

    @Bean(name = "entityManagerFactory")
    public EntityManagerFactory getEntityManagerFactory(@Qualifier("getDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.cache.use_second_level_cache", "true");
        jpaProperties.setProperty("hibernate.cache.use_query_cache", "true");
        jpaProperties.setProperty("hibernate.cache.region.factory_class", EhCacheRegionFactory.class.getName());
        factoryBean.setJpaProperties(jpaProperties);
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        Database databaseType = Database.valueOf(System.getProperty("BASTADB_TYPE", Database.ORACLE.name())
                .toUpperCase());
        jpaVendorAdapter.setGenerateDdl(databaseType == Database.H2);
        jpaVendorAdapter.setDatabase(databaseType);
        jpaVendorAdapter.setShowSql(false);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);

        factoryBean.setPackagesToScan(RootPackage.class.getPackage().getName());
        factoryBean.setPersistenceProviderClass(HibernatePersistence.class);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    @Bean
    public HibernateExceptionTranslator getHibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

    @Bean(name = "transactionManager")
    public JpaTransactionManager getTransactionManager() {
        return new JpaTransactionManager();
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
        if(dbUrl.toLowerCase().contains("failover")) {
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
        poolDataSource.setMaxPoolSize(20);
        poolDataSource.setMaxConnectionReuseTime(300); // 5min
        poolDataSource.setMaxConnectionReuseCount(100);
        poolDataSource.setConnectionProperties(connProperties);
        return poolDataSource;
    }

/*    @Bean(initMethod = "migrate")
    @DependsOn("getDataSource")
    @Conditional(value=DevDataSourceCondition.class)
    Flyway flyway(@Qualifier("getDataSource") DataSource datasource) {
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        flyway.setLocations("classpath:db/migrations/bastaDB");
        flyway.setDataSource(datasource);

        return flyway;
    }*/

    private String getConnectionFactoryClassName() {
        return "oracle.jdbc.pool.OracleDataSource";
    }
}
