package no.nav.aura.basta.spring;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.UUID;

import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import(SpringConfig.class)
@ImportResource({ "classpath:spring-security-unit-test.xml" })
public class JunitJettyTestConfig {

    private static final Logger logger = LoggerFactory.getLogger(JunitJettyTestConfig.class);

    @Bean
    public FasitRestClient getFasitRestClient() {
        logger.info("mocking FasitRestClient");
        FasitRestClient fasitRestClient = mock(FasitRestClient.class);

        Answer<?> echoAnswer = new Answer<NodeDO>() {
            @Override
            public NodeDO answer(InvocationOnMock invocation) throws Throwable {
                NodeDO nodeDO = (NodeDO) invocation.getArguments()[0];
                nodeDO.setRef(new URI("http://foo.fasit.foo"));
                return nodeDO;
            }
        };
        when(fasitRestClient.registerNode(any(NodeDO.class), anyString())).thenAnswer(echoAnswer);
        return fasitRestClient;
    }

    @Bean
    public CertificateService getCertificateService() {
        return mock(CertificateService.class);
    }

    @Bean
    public OrchestratorService getOrchestratorService() {
        OrchestratorService service = mock(OrchestratorService.class);
        when(service.stop(any(StopRequest.class))).thenReturn(generateRandomToken());
        when(service.decommission(any(DecomissionRequest.class))).thenReturn(generateRandomToken());
        when(service.start(any(StartRequest.class))).thenReturn(generateRandomToken());
        return service;
    }

    private WorkflowToken generateRandomToken() {
        WorkflowToken token = new WorkflowToken();
        token.setId(UUID.randomUUID().toString());
        return token;
    }
}
