package no.nav.aura.basta.spring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.rest.OrderStatus;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SpringConfig.class)
public class StandaloneRunnerTestConfig {

    @Bean
    public static BeanFactoryPostProcessor init() {
        System.setProperty("basta.db.type", "h2");
        System.setProperty("ws.orchestrator.url", "https://someserver/vmware-vmo-webcontrol/webservice");
        System.setProperty("user.orchestrator.username", "orcname");
        System.setProperty("user.orchestrator.password", "secret");

        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }

    @Bean
    public OrchestratorService getOrchestratorService() {
        OrchestratorService service = mock(OrchestratorService.class);
        Answer<WorkflowToken> generateToken = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                WorkflowToken token = new WorkflowToken();
                token.setId(UUID.randomUUID().toString());
                return token;
            }
        };
        when(service.decommission(Mockito.<DecomissionRequest> anyObject())).then(generateToken);
        when(service.send(Mockito.anyObject())).then(generateToken);
        when(service.getOrderStatus(Mockito.anyString())).thenReturn(Tuple.of(OrderStatus.FAILURE, "This is a mock; what do you expect"));
        return service;
    }

}
