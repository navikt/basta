package no.nav.aura.basta.spring;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.security.KeyStore;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.serviceuser.cservice.GeneratedCertificate;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddleWareType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.ProvisionRequest2;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;
import no.nav.aura.basta.util.HTTPOperation;
import no.nav.aura.basta.util.HTTPTask;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
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

import com.google.common.collect.Lists;
import com.googlecode.flyway.core.util.Resource;

@Configuration
@Import(SpringConfig.class)
@ImportResource({ "classpath:spring-security-unit-test.xml" })
public class StandaloneRunnerTestConfig {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneRunnerTestConfig.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

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

        // Was order form
        ResourceElement wasDmgr = createResource(ResourceTypeDO.DeploymentManager, "wasDmgr", new PropertyElement("hostname", "dmgr.host.no"));
        mockFindResource(fasitRestClient, wasDmgr);
        ResourceElement wsAdminUser = createResource(ResourceTypeDO.Credential, "wsadminUser", new PropertyElement("username", "srvWas"), new PropertyElement("password", "verySecret"));
        mockFindResource(fasitRestClient, wsAdminUser);

        // Lage sertifikat
        ResourceElement certificatResource = createResource(ResourceTypeDO.Certificate, "alias");
        when(fasitRestClient.executeMultipart(anyString(), anyString(), any(MultipartFormDataOutput.class), anyString(), eq(ResourceElement.class))).thenReturn(certificatResource);

        // Lage credential
        ResourceElement credentialResource = createResource(ResourceTypeDO.Credential, "alias");
        when(fasitRestClient.registerResource(any(ResourceElement.class), anyString())).thenReturn(credentialResource);
        when(fasitRestClient.updateResource(anyInt(), any(ResourceElement.class), anyString())).thenReturn(credentialResource);
        return fasitRestClient;
    }

    private void mockFindResource(FasitRestClient fasitRestClient, ResourceElement resource) {
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(resource.getType()), eq(resource.getAlias()))).thenReturn(Lists.newArrayList(resource));
    }

    private ResourceElement createResource(ResourceTypeDO type, String alias, PropertyElement... properties) {
        ResourceElement resouce = new ResourceElement();
        resouce.setAlias(alias);
        resouce.setType(type);
        resouce.setId(100l);
        resouce.setRef(URI.create("http://mocketdup.no/resource"));
        for (PropertyElement property : properties) {
            resouce.addProperty(property);
        }

        return resouce;
    }

    @Bean
    public CertificateService getCertificateService() throws Exception {
        logger.info("mocking CertificateService");
        CertificateService certificateService = mock(CertificateService.class);
        GeneratedCertificate cert = new GeneratedCertificate();
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null, "passwd".toCharArray());
        cert.setKeyStore(keystore);

        cert.setKeyStoreAlias("alias");
        cert.setKeyStorePassword("secret");
        when(certificateService.createServiceUserCertificate(any(ServiceUserAccount.class))).thenReturn(cert);
        return certificateService;
    }

    @Bean
    public OrchestratorService getOrchestratorService() {
        logger.info("mocking OrchestratorService");
        OrchestratorService service = mock(OrchestratorService.class);

        Answer<?> provisionAnswer = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                ProvisionRequest provisionRequest = (ProvisionRequest) invocation.getArguments()[0];
                putProvisionVM(provisionRequest);
                return returnRandomToken();
            }
        };

        Answer<?> provisionAnswer2 = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                ProvisionRequest2 provisionRequest = (ProvisionRequest2) invocation.getArguments()[0];
                putProvisionVM(provisionRequest);
                return returnRandomToken();
            }
        };

        Answer<WorkflowToken> decommissionAnswer = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                DecomissionRequest decomissionRequest = (DecomissionRequest) invocation.getArguments()[0];
                removeVM(decomissionRequest);
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

        when(service.decommission(Mockito.<DecomissionRequest> anyObject())).thenAnswer(decommissionAnswer);
        when(service.stop(Mockito.<StopRequest> anyObject())).thenAnswer(stopAnswer);
        when(service.start(Mockito.<StartRequest> anyObject())).thenAnswer(startAnswer);
        when(service.send(Mockito.<ProvisionRequest> anyObject())).thenAnswer(provisionAnswer);
        when(service.provision(Mockito.<ProvisionRequest2> anyObject())).thenAnswer(provisionAnswer2);
        when(service.getOrderStatus(Mockito.anyString())).thenReturn(Tuple.of(OrderStatus.PROCESSING, ""));
        return service;
    }

    private WorkflowToken returnRandomToken() {
        WorkflowToken token = new WorkflowToken();
        token.setId(UUID.randomUUID().toString());
        return token;
    }

    private void putProvisionVM(ProvisionRequest2 provisionRequest) {

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
        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "provision", StatusLogLevel.success));
        executorService.execute(new HTTPTask(provisionRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));
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
        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "provision", StatusLogLevel.success));
        executorService.execute(new HTTPTask(provisionRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));
    }

    private void quackLikeA(OrchestratorNodeDO node) {
        node.setMiddlewareType(MiddleWareType.jb);
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

    private void sleepALittle() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopProvisionVM(StopRequest stopRequest) {
        for (String hostname : stopRequest.getPowerdown()) {
            OrchestratorNodeDO node = new OrchestratorNodeDO();
            node.setHostName(hostname + ".devillo.no");
            executorService.execute(new HTTPTask(stopRequest.getStopCallbackUrl(), node, HTTPOperation.PUT));
            sleepALittle();
        }

        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "stop", StatusLogLevel.success));
        executorService.execute(new HTTPTask(stopRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));
    }

    private void startProvisionVM(StartRequest startRequest) {
        for (String hostname : startRequest.getPoweron()) {
            OrchestratorNodeDO node = new OrchestratorNodeDO();
            node.setHostName(hostname + ".devillo.no");
            executorService.execute(new HTTPTask(startRequest.getStartCallbackUrl(), node, HTTPOperation.PUT));
            sleepALittle();
        }

        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "start", StatusLogLevel.success));
        executorService.execute(new HTTPTask(startRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));

    }

    private void removeVM(DecomissionRequest decomissionRequest) {
        for (String hostname : decomissionRequest.getVmsToRemove()) {
            OrchestratorNodeDO node = new OrchestratorNodeDO();
            node.setHostName(hostname + ".devillo.no");
            executorService.execute(new HTTPTask(decomissionRequest.getDecommissionCallbackUrl(), node, HTTPOperation.PUT));
            sleepALittle();

        }

        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "decommission", StatusLogLevel.success));
        executorService.execute(new HTTPTask(decomissionRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));

    }

}
