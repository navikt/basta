package no.nav.aura.basta.spring;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import no.nav.aura.basta.RootPackage;

import org.hibernate.ejb.HibernatePersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public EntityManagerFactory getEntityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.cache.use_second_level_cache", "true");
        jpaProperties.setProperty("hibernate.cache.use_query_cache", "true");
        jpaProperties.setProperty("hibernate.cache.region.factory_class",
                org.hibernate.cache.ehcache.EhCacheRegionFactory.class.getName());
        factoryBean.setJpaProperties(jpaProperties);
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        Database databaseType = Database.valueOf(System.getProperty("basta_db_type", Database.ORACLE.name()).toUpperCase());
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

}
