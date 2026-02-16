package no.nav.aura.basta.spring;

import com.bettercloud.vault.Vault;

import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.VaultUpdateService;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;

import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@ComponentScan(basePackages = "no.nav.aura.basta", 
    excludeFilters = {
        @Filter(Configuration.class),
        @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            RestClient.class,
            OrchestratorClient.class,
            MqService.class,
            CertificateService.class,
            OracleClient.class,
            VaultUpdateService.class
            // FasitUpdateService should NOT be excluded - it's a real bean that uses mocked RestClient
        })
    })
@Import({SpringDbConfig.class, SpringSecurityTestConfig.class})
public class SpringUnitTestConfig {

    @Bean
    static BeanFactoryPostProcessor init() {
        System.setProperty("ws_orchestrator_url", "https://someserver/vmware-vmo-webcontrol/webservice");
//        System.setProperty("ORCHESTRATOR_CALLBACK_HOST_URL", "https://basta/rest");
        System.setProperty("user_orchestrator_username", "orcname");
        System.setProperty("user_orchestrator_password", "secret");

        System.setProperty("ws_menandmice_url", "https://someserver/menandmice/webservice");
        System.setProperty("ws_menandmice_username", "mmName");
        System.setProperty("ws_menandmice_password", "mmSecret");

		System.setProperty("fasit_base_url", "https://thefasitresourceapi.com"); // because not easy to load from application.properties
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    DataSource getDataSource() {
        System.setProperty("BASTADB_TYPE", "h2");
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    RestClient getRestClient() {
        return mock(RestClient.class);
    }

    @Bean
    OrchestratorClient getOrchestratorClient() {
        return mock(OrchestratorClient.class);
    }

    @Bean
    MqService getMqService() {
        return mock(MqService.class);
    }

    @Bean
    CertificateService getCertificateService() {
        return mock(CertificateService.class);
    }


    @Bean
    OracleClient getOracleClient() {
        return mock(OracleClient.class);
    }

    @Bean
    VaultUpdateService getVaultUpdateService() {
        return mock(VaultUpdateService.class);
    }

    @Bean
    Vault getVaultClient() {
        return mock(Vault.class);
    }

    @Bean
    ActiveDirectory getActiveDirectory() {
        ActiveDirectory activeDirectory = mock(ActiveDirectory.class);
        Answer<?> echoAnswer = (Answer<ServiceUserAccount>) invocation -> (ServiceUserAccount) invocation.getArguments()[0];
        when(activeDirectory.createOrUpdate(any(ServiceUserAccount.class))).then(echoAnswer);
        when(activeDirectory.userExists(any(ServiceUserAccount.class))).thenReturn(false);
        return activeDirectory;
    }

}
