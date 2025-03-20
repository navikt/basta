package no.nav.aura.basta.spring;

import com.bettercloud.vault.Vault;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.VaultUpdateService;
import no.nav.aura.basta.backend.fasit.deprecated.FasitRestClient;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;

import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ComponentScan(basePackages = "no.nav.aura.basta", excludeFilters = @Filter(Configuration.class))
@Import({SpringDbConfig.class, SpringSecurityTestConfig.class})
public class SpringUnitTestConfig {

    @Bean
    public static BeanFactoryPostProcessor init() {
        System.setProperty("ws_orchestrator_url", "https://someserver/vmware-vmo-webcontrol/webservice");
        System.setProperty("user_orchestrator_username", "orcname");
        System.setProperty("user_orchestrator_password", "secret");

        System.setProperty("ws_menandmice_url", "https://someserver/menandmice/webservice");
        System.setProperty("ws_menandmice_username", "mmName");
        System.setProperty("ws_menandmice_password", "mmSecret");
        System.setProperty("fasit_nodes_v2_url", "https://thefasitnodeapi.com");
        System.setProperty("fasit_resources_v2_url", "https://thefasitresourceapi.com");
        System.setProperty("fasit_scopedresource_v2_url", "https://thefasitresourceapi.com");
        System.setProperty("fasit_lifecycle_v1_url", "https://thefasitresourceapi.com");
        System.setProperty("fasit_applicationinstances_v2_url", "https://thefasitappinstanceapi.com");

        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public DataSource getDataSource() {
        System.setProperty("BASTADB_TYPE", "h2");
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    public RestClient getRestClient() {
        return mock(RestClient.class);
    }

    @Bean
    public OrchestratorClient getOrchestratorClient() {
        return mock(OrchestratorClient.class);
    }

    @Bean
    public MqService getMqService() {
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
    public VaultUpdateService getVaultUpdateService() {
        return mock(VaultUpdateService.class);
    }

    @Bean
    public Vault getVaultClient() {
        return mock(Vault.class);
    }

    @Bean
    public ActiveDirectory getActiveDirectory() {
        ActiveDirectory activeDirectory = mock(ActiveDirectory.class);
        Answer<?> echoAnswer = (Answer<ServiceUserAccount>) invocation -> (ServiceUserAccount) invocation.getArguments()[0];
        when(activeDirectory.createOrUpdate(any(ServiceUserAccount.class))).then(echoAnswer);
        when(activeDirectory.userExists(any(ServiceUserAccount.class))).thenReturn(false);
        return activeDirectory;
    }

}
