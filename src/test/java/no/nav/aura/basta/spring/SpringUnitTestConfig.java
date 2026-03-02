package no.nav.aura.basta.spring;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import javax.sql.DataSource;

import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.web.client.RestTemplate;

import com.bettercloud.vault.Vault;

import no.nav.aura.basta.backend.DBHandler;
import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.VaultUpdateService;
import no.nav.aura.basta.backend.WaitingOrderHandler;
import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.rest.bigip.BigIPOrderRestService;

@TestConfiguration
@ComponentScan(basePackages = "no.nav.aura.basta", 
    excludeFilters = {
        @Filter(Configuration.class),
        @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            OrchestratorClient.class,
            MqService.class,
            CertificateService.class,
            OracleClient.class,
            VaultUpdateService.class,
//            BigIPClientSetup.class,
            BigIPOrderRestService.class,
            DBHandler.class,
            WaitingOrderHandler.class,
            SpringStartupHook.class,
            FasitRestClient.class,  // Exclude so mock bean is used instead
            FasitUpdateService.class  // Exclude to prevent HTTP calls during startup
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
		System.setProperty("srvbasta_username", "mockUser"); 
		System.setProperty("srvbasta_password", "mockPassword");
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    DataSource getDataSource() {
        System.setProperty("BASTADB_TYPE", "h2");
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

//    @Bean
//    RestClient getRestClient() {
//        return mock(RestClient.class);
//    }
    @Bean
    FasitRestClient getFasitRestClient() {
        FasitRestClient mock = mock(FasitRestClient.class);
        // Stub the createFasitResource method to avoid HTTP calls
        when(mock.createFasitResource(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Optional.of("12345"));
        return mock;
    }

    @Bean
    FasitUpdateService getFasitUpdateService() {
        FasitUpdateService mock = mock(FasitUpdateService.class);
        // Stub registerNode method to return a successful result
        when(mock.registerNode(any(), any())).thenReturn(Optional.of("12345"));
        return mock;
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