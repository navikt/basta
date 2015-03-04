package no.nav.aura.basta.spring;

import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.request.*;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.util.HTTPOperation;
import no.nav.aura.basta.util.HTTPTask;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.generated.vmware.ws.WorkflowToken;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Import(SpringConfig.class)
@ImportResource({ "classpath:spring-security-unit-test.xml" })
public class StandaloneRunnerTestConfig {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneRunnerTestConfig.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(1        );


    @Bean
    public static BeanFactoryPostProcessor init() {
        System.setProperty("basta.db.type", "h2");
        System.setProperty("ws.orchestrator.url", "https://someserver/vmware-vmo-webcontrol/webservice");
        System.setProperty("user.orchestrator.username", "orcname");
        System.setProperty("user.orchestrator.password", "secret");
        logger.info("init StandaloneRunnerTestConfig");
        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }

    @Bean
    public FasitRestClient getFasitRestClient(){
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
    public OrchestratorService getOrchestratorService() {
        OrchestratorService service = mock(OrchestratorService.class);

        Answer<?> provisionAnswer = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                ProvisionRequest provisionRequest = (ProvisionRequest) invocation.getArguments()[0];
                putProvisionVM(provisionRequest);
                return returnRandomToken();
            }
        };

        Answer<WorkflowToken> decommissionAnswer = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                DecomissionRequest decomissionRequest = (DecomissionRequest) invocation.getArguments()[0];
                putRemoveVM(decomissionRequest);
                return returnRandomToken();
            }
        };
        Answer<?> stopAnswer = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                StopRequest stopRequest = (StopRequest) invocation.getArguments()[0];
                stopProvisionVM(stopRequest);
                return returnRandomToken();
            };
        };

        Answer<?> startAnswer = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                StartRequest startRequest = (StartRequest) invocation.getArguments()[0];
                startProvisionVM(startRequest);
                return returnRandomToken();
            };
        };

        when(service.decommission(Mockito.<DecomissionRequest>anyObject())).thenAnswer(decommissionAnswer);
        when(service.stop(Mockito.<StopRequest>anyObject())).thenAnswer(stopAnswer);
        when(service.start(Mockito.<StartRequest>anyObject())).thenAnswer(startAnswer);
        when(service.send(Mockito.<ProvisionRequest>anyObject())).thenAnswer(provisionAnswer);
        when(service.getOrderStatus(Mockito.anyString())).thenReturn(Tuple.of(OrderStatus.PROCESSING, ""));
        return service;
    }
    private WorkflowToken returnRandomToken(){
        WorkflowToken token = new WorkflowToken();
        token.setId(UUID.randomUUID().toString());
        return token;
    }
    private void putProvisionVM(ProvisionRequest provisionRequest) {

        OrchestratorNodeDOList vms = new OrchestratorNodeDOList();

        String[] split = provisionRequest.getStatusCallbackUrl().getPath().split("/");
        OrchestratorNodeDO node = new OrchestratorNodeDO();
        node.setHostName("e" + Long.valueOf(split[split.length - 2]) + "1.devillo.no");
        quackLikeA(node);
        vms.addVM(node);

        OrchestratorNodeDO node2 = new OrchestratorNodeDO();
        node2.setHostName("e" + Long.valueOf(split[split.length - 2]) + "2.devillo.no");
        quackLikeA(node2);
        vms.addVM(node2);
        executorService.execute(new HTTPTask(provisionRequest.getResultCallbackUrl(), vms, HTTPOperation.PUT));
        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "provision", "success"));
        executorService.execute(new HTTPTask(provisionRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));
    }

    private void quackLikeA(OrchestratorNodeDO node) {
        node.setMiddlewareType(Vm.MiddleWareType.jb);
        node.setAdminUrl(null);
        node.setCpuCount(1);
        node.setDatasenter("datacenter,yeah");
        node.setDeployerPassword("it must be a duck");
        node.setDeployUser("quack");
        node.setMemoryMb(1024);
        node.setSslCert("cert");
        node.setSslpassphrase("knock knock");
        node.setSslPrivateKey("who's there?");
        node.setvApp("vappavappa");
    }


    private void putRemoveVM(DecomissionRequest decomissionRequest) {
        OrchestratorNodeDOList vms = new OrchestratorNodeDOList();
        for (String hostname : decomissionRequest.getVmsToRemove()) {
                OrchestratorNodeDO node = new OrchestratorNodeDO();
                node.setHostName(hostname + ".devillo.no");
                vms.addVM(node);

        }
        executorService.execute(new HTTPTask(decomissionRequest.getDecommissionCallbackUrl(), vms, HTTPOperation.PUT));
        sleepALittle();
        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "decommission", "success"));
        executorService.execute(new HTTPTask(decomissionRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));

    }

    private void sleepALittle() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopProvisionVM(StopRequest stopRequest) {
        OrchestratorNodeDOList vms = new OrchestratorNodeDOList();
        for (String hostname : stopRequest.getPowerdown()) {
            OrchestratorNodeDO node = new OrchestratorNodeDO();
            node.setHostName(hostname + ".devillo.no");
            vms.addVM(node);
        }

        executorService.execute(new HTTPTask(stopRequest.getStopCallbackUrl(), vms, HTTPOperation.PUT));
        sleepALittle();
        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "stop", "success"));
        executorService.execute(new HTTPTask(stopRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));
    }

    private void startProvisionVM(StartRequest startRequest) {
        OrchestratorNodeDOList vms = new OrchestratorNodeDOList();
        for (String hostname : startRequest.getPoweron()) {
            OrchestratorNodeDO node = new OrchestratorNodeDO();
            node.setHostName(hostname + ".devillo.no");
            vms.addVM(node);
        }
        executorService.execute(new HTTPTask(startRequest.getStartCallbackUrl(), vms, HTTPOperation.PUT));
        sleepALittle();
        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "start", "success"));
        executorService.execute(new HTTPTask(startRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));

    }

}
