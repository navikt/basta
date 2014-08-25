package no.nav.aura.basta.spring;

import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.rest.OrchestratorNodeDO;
import no.nav.aura.basta.rest.OrderStatus;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.generated.vmware.ws.WorkflowToken;
import org.jboss.resteasy.client.ClientResponse;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Import(SpringConfig.class)
public class StandaloneRunnerTestConfig {

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

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
    public FasitRestClient getFasitRestClient(){
        FasitRestClient fasitRestClient = mock(FasitRestClient.class);
        return fasitRestClient;
    }

    @Bean
    public OrchestratorService getOrchestratorService() {
        OrchestratorService service = mock(OrchestratorService.class);
        Answer<WorkflowToken> decommissionAnswer = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                DecomissionRequest decomissionRequest = (DecomissionRequest) invocation.getArguments()[0];
                putRemoveVM(decomissionRequest);
                WorkflowToken token = new WorkflowToken();
                token.setId(UUID.randomUUID().toString());

                return token;
            }
        };
        when(service.decommission(Mockito.<DecomissionRequest>anyObject())).thenAnswer(decommissionAnswer);
        Answer<?> provisionAnswer = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                ProvisionRequest provisionRequest = (ProvisionRequest) invocation.getArguments()[0];
                putProvisionVM(provisionRequest);
                WorkflowToken token = new WorkflowToken();
                token.setId(UUID.randomUUID().toString());
                return token;
            }
        };
        when(service.send(Mockito.anyObject())).thenAnswer(provisionAnswer);
        when(service.getOrderStatus(Mockito.anyString())).thenReturn(Tuple.of(OrderStatus.SUCCESS, ""));
        return service;
    }

    class PutTask implements Runnable {
        private final URI uri;
        private final OrchestratorNodeDO node;

        PutTask(URI uri, OrchestratorNodeDO node) {
            this.uri = uri;
            this.node = node;
        }

        public void run() {
            RestEasyDetails bee = new RestEasyDetails("", "");
            try {
                Thread.sleep(3000);
                   ClientResponse response = bee.createClientRequest(uri).body(MediaType.APPLICATION_XML_TYPE, node).put();
                response.releaseConnection();
            } catch (Exception e) {
                e.printStackTrace();
        }


        }
    }
    private void putProvisionVM(ProvisionRequest provisionRequest) {

        String[] split = provisionRequest.getStatusCallbackUrl().getPath().split("/");
        OrchestratorNodeDO node = new OrchestratorNodeDO();
        node.setHostName("e" + Long.valueOf(split[split.length - 2]) + "1.devillo.no");
        executorService.execute(new PutTask(provisionRequest.getResultCallbackUrl(), node));
        OrchestratorNodeDO node2 = new OrchestratorNodeDO();
        node2.setHostName("e" + Long.valueOf(split[split.length - 2]) + "2.devillo.no");
        executorService.execute(new PutTask(provisionRequest.getResultCallbackUrl(), node2));
    }


    private void putRemoveVM(DecomissionRequest decomissionRequest) {
        for (String hostname : decomissionRequest.getVmsToRemove()) {
                OrchestratorNodeDO node = new OrchestratorNodeDO();
                node.setHostName(hostname + ".devillo.no");
                executorService.execute(new PutTask(decomissionRequest.getDecommissionCallbackUrl(), node));
        }

    }

}
