package no.nav.aura.basta.spring;

import no.nav.aura.basta.RootPackage;
import org.flywaydb.core.Flyway;
import org.hibernate.cfg.Environment;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@EnableJpaRepositories(basePackageClasses = RootPackage.class)
@EnableTransactionManagement
public class SpringDbConfig {

    @Bean(name = "entityManagerFactory")
    public EntityManagerFactory getEntityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        Properties jpaProperties = new Properties();
        jpaProperties.put(Environment.USE_SECOND_LEVEL_CACHE, true);
        jpaProperties.put(Environment.USE_QUERY_CACHE, true);
        jpaProperties.put(Environment.CACHE_REGION_FACTORY, "org.hibernate.cache.ehcache.internal" +
                ".EhcacheRegionFactory");
        factoryBean.setJpaProperties(jpaProperties);
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        Database databaseType = Database.valueOf(System.getProperty("BASTADB_TYPE", Database.ORACLE.name())
                .toUpperCase());
        jpaVendorAdapter.setGenerateDdl(databaseType == Database.H2);
        jpaVendorAdapter.setDatabase(databaseType);
        jpaVendorAdapter.setShowSql(false);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);

        factoryBean.setPackagesToScan(RootPackage.class.getPackage().getName());
        factoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
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

    @Bean(initMethod = "migrate")
    @DependsOn("getDataSource")
    @ConditionalOnProperty(name="spring.flyway.enabled", havingValue="true")
    Flyway flyway(@Qualifier("getDataSource") DataSource datasource) {
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        flyway.setLocations("classpath:db/migration/bastaDB");
        flyway.setDataSource(datasource);

        return flyway;
    }
}
