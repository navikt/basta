package no.nav.aura.basta.spring;

import no.nav.aura.basta.RootPackage;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.envconfig.client.FasitRestClient;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ComponentScan(basePackageClasses = RootPackage.class, excludeFilters = @Filter(Configuration.class))
@Import(SpringDbConfig.class)
@ImportResource({ "classpath:spring-security-unit-test.xml" })
public class SpringUnitTestConfig {

    @Bean
    public static BeanFactoryPostProcessor init() {
        System.setProperty("ws.orchestrator.url", "https://someserver/vmware-vmo-webcontrol/webservice");
        System.setProperty("user.orchestrator.username", "orcname");
        System.setProperty("user.orchestrator.password", "secret");

        System.setProperty("ws.menandmice.url", "https://someserver/menandmice/webservice");
        System.setProperty("ws.menandmice.username", "mmName");
        System.setProperty("ws.menandmice.password", "mmSecret");

        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }

    @Bean
    public DataSource getDataSource() {
        System.setProperty("basta.db.type", "h2");
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    public OrchestratorClient getOrchestratorClient() {
        return mock(OrchestratorClient.class);
    }
    
    @Bean
    public MqService getMqService(){
        return mock(MqService.class);
    }

    @Bean
    public CertificateService getCertificateService() {
        return mock(CertificateService.class);
    }

    @Bean
    public FasitRestClient getFasitRestClient() {
        return mock(FasitRestClient.class);
    }

    @Bean
    public OracleClient getOracleClient() {
        return mock(OracleClient.class);
    }

    @Bean
    public ActiveDirectory getActiveDirectory() {
        ActiveDirectory activeDirectory = mock(ActiveDirectory.class);
        Answer<?> echoAnswer = new Answer<ServiceUserAccount>() {
            @Override
            public ServiceUserAccount answer(InvocationOnMock invocation) throws Throwable {
                ServiceUserAccount echo = (ServiceUserAccount) invocation.getArguments()[0];
                return echo;
            }
        };
        when(activeDirectory.createOrUpdate(any(ServiceUserAccount.class))).then(echoAnswer);
        when(activeDirectory.userExists(any(ServiceUserAccount.class))).thenReturn(false);
        return activeDirectory;
    }

}
