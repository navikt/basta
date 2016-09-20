package no.nav.aura.basta.spring;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.mockito.Matchers;
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
import com.google.gson.Gson;

import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
import no.nav.aura.basta.backend.bigip.RestClient;
import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.backend.mq.MqTopic;
import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.serviceuser.cservice.GeneratedCertificate;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponse;
import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponseVm;
import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponseVm.ResultType;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.FasitLookupService;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;
import no.nav.aura.basta.util.HTTPOperation;
import no.nav.aura.basta.util.HTTPTask;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.envconfig.client.*;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.generated.vmware.ws.WorkflowToken;

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

        System.setProperty("ws.menandmice.url", "https://someserver/menandmice/webservice");
        System.setProperty("ws.menandmice.username", "mmName");
        System.setProperty("ws.menandmice.password", "mmSecret");

        System.setProperty("fasit:resource_v2.url", "https://thefasitresourceapi.com");
        System.setProperty("fasit:environments_v2.url", "https://thefasitenvironmentsapi.com");
        System.setProperty("fasit:applications_v2.url", "https://thefasitapplicationsapi.com");
        System.setProperty("fasit.rest.api.url", "https://theoldfasitapi.com");

        logger.info("init StandaloneRunnerTestConfig");
        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }

    @Bean(name="restClient")
    public RestClient getRestClientMock(){
        RestClient restClient = mock(RestClient.class);
        when(restClient.get(anyString(), eq(Map.class))).thenReturn(Optional.of(new HashMap<>()));
        when(restClient.get(anyString(), eq(List.class))).thenReturn(Optional.of(Arrays.asList(new HashMap<>())));
        return restClient;
    }


    @Bean(name="bigIPClientSetup")
    public BigIPClientSetup getBigIPClientService() {
        logger.info("mocking BigIPService");
        final BigIPClientSetup setup = mock(BigIPClientSetup.class);
        final BigIPClient bigIPClientMock = mock(BigIPClient.class);
        when(bigIPClientMock.getVirtualServers(anyString())).thenReturn((List<Map<String, Object>>) createBigIpItemList().get("items"));
        when(bigIPClientMock.getVirtualServer(anyString())).thenReturn(Optional.of(new HashMap<>()));
        when(bigIPClientMock.getPoliciesFrom(anyMap())).thenReturn(new HashSet<>());
        when(bigIPClientMock.getRules(anyString())).thenReturn(createBigIpItemList());
        when(bigIPClientMock.deleteRuleFromPolicy(anyString(), anyString())).thenReturn(new ServerResponse(null, 404, null));
        when(setup.setupBigIPClient(any(BigIPOrderInput.class))).thenReturn(bigIPClientMock);
        return setup;
    }

    private Map createBigIpItemList() {
        String json = "{\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"name\": \"vs_name_1\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"vs_name_2\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
        Map response = new Gson().fromJson(json,Map.class);
        return response;

    }


    @Bean
    public FasitLookupService getFasitProxy() {
        logger.info("mocking fasit proxy");
        FasitLookupService proxy = mock(FasitLookupService.class);
        Gson gson = new Gson();
        when(proxy.getApplications())
                .thenReturn(gson.toJson(new ApplicationDO[] { new ApplicationDO("app1", "group", "artifact"), new ApplicationDO("app2", "group", "artifact"), new ApplicationDO("fasit", "group", "artifact") }));
        when(proxy.getApplicationGroups()).thenReturn(gson.toJson(new ApplicationGroupDO[] { new ApplicationGroupDO("appgroup1") }));
        UriBuilder uriBuilder = UriBuilder.fromUri("http://mock.standalone");
        when(proxy.getEnvironments()).thenReturn(gson
                .toJson(new EnvironmentDO[] { new EnvironmentDO("u1", "u", uriBuilder), new EnvironmentDO("u2", "u", uriBuilder), new EnvironmentDO("u3", "u", uriBuilder), new EnvironmentDO("cd-u1", "u", uriBuilder),
                        new EnvironmentDO("t1", "t", uriBuilder), new EnvironmentDO("t2", "t", uriBuilder), new EnvironmentDO("q1", "q", uriBuilder), new EnvironmentDO("p", "p", uriBuilder) }));

        mockProxyResource(proxy, ResourceTypeDO.QueueManager,
                createResource(ResourceTypeDO.QueueManager, "mockedQm", new PropertyElement("name", "MOCK_CLIENT01"), new PropertyElement("hostname", "mocking.server"), new PropertyElement("port", "9696")));

        mockProxyResource(proxy, ResourceTypeDO.Topic,
                createResource(ResourceTypeDO.Topic, "mockedTopic", new PropertyElement("topicString", "mock/me/to/hell")));
        
        mockProxyResource(proxy, ResourceTypeDO.Queue,
                createResource(ResourceTypeDO.Queue, "mockedQueue", new PropertyElement("queueName", "QA.U1_MOCK_QUEUE1")));
        
        mockProxyResource(proxy, ResourceTypeDO.Channel,
                createResource(ResourceTypeDO.Channel, "mockedChannel", new PropertyElement("name", "U1_MOCK_CHANNEL")));

        return proxy;
    }

    public void mockProxyResource(FasitLookupService proxy, ResourceTypeDO type, ResourceElement... returnValues) {
        when(proxy.getResources(anyString(), anyString(), anyString(), eq(type), anyString(), any(), any())).thenReturn(new Gson().toJson(returnValues));
    }

    @Bean
    public ActiveDirectory getActiveDirectory() {
        logger.info("mocking AD");
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
    public MqService getMqService() {
        logger.info("mocking MQ");
        MqService mqService = mock(MqService.class);
        when(mqService.queueExists(any(MqQueueManager.class), endsWith("EXISTS"))).thenReturn(true);
        when(mqService.deleteQueue(any(MqQueueManager.class), anyString())).thenReturn(true);
        Answer<?> queueAnswer = new Answer<Optional<MqQueue>>() {

            @Override
            public Optional<MqQueue> answer(InvocationOnMock invocation) throws Throwable {
                String queueName = (String) invocation.getArguments()[1];
                return Optional.of(new MqQueue(queueName, 1, 1, "mockup queue for test"));
            }
        };
        when(mqService.getQueue(any(MqQueueManager.class), anyString())).thenAnswer(queueAnswer);
        when(mqService.getClusterNames(any(MqQueueManager.class))).thenReturn(asList("NL.DEV.D1.CLUSTER", "NL.TEST.T1.CLUSTER"));
        when(mqService.findQueuesAliases(any(MqQueueManager.class), endsWith("*"))).thenReturn(asList("U1_MOCK_QUEUE1", "U1_MOCK_QUEUE2", "U1_MOCK_QUEUE3"));
        when(mqService.getTopics(any(MqQueueManager.class)))
                .thenReturn(asList(new MqTopic("heavenMock", "mock/me/to/heaven"), new MqTopic("hellMock", "mock/me/to/hell"), new MqTopic("rockMock", "rock/stairway/to/heaven")));
        
        when(mqService.findChannelNames(any(MqQueueManager.class), startsWith("U3"))).thenReturn(Arrays.asList("U3_MYAPP"));
        when(mqService.findChannelNames(any(MqQueueManager.class), eq("*"))).thenReturn(Arrays.asList("U1_MYAPP", "U1_YOURAPP", "U2_MYAPP", "U1_MOCK_CHANNEL"));
        return mqService;
    }

    @Bean
    public FasitRestClient getFasitRestClient() {
        logger.info("mocking FasitRestClient");
        FasitRestClient fasitRestClient = mock(FasitRestClient.class);

        when(fasitRestClient.buildResourceQuery(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), any(ResourceTypeDO.class), anyString(), any(), any())).thenReturn(URI.create("http://mocked.up"));
        Answer<?> nodeEchoAnswer = new Answer<NodeDO>() {
            @Override
            public NodeDO answer(InvocationOnMock invocation) throws Throwable {
                NodeDO nodeDO = (NodeDO) invocation.getArguments()[0];
                nodeDO.setRef(new URI("http://foo.fasit.foo"));
                return nodeDO;
            }
        };
        when(fasitRestClient.registerNode(any(NodeDO.class), anyString())).thenAnswer(nodeEchoAnswer);

        // Generisk create og update resource
        Answer<?> resourceEchoAnswer = new Answer<ResourceElement>() {
            @Override
            public ResourceElement answer(InvocationOnMock invocation) throws Throwable {
                ResourceElement resource = (ResourceElement) invocation.getArguments()[0];
                resource.setId(102l);
                resource.setRef(new URI("http://foo.fasit.foo"));
                return resource;
            }
        };
        when(fasitRestClient.registerResource(any(ResourceElement.class), anyString())).thenAnswer(resourceEchoAnswer);
        when(fasitRestClient.updateResource(anyInt(), any(ResourceElement.class), anyString())).thenAnswer(resourceEchoAnswer);
        when(fasitRestClient.deleteResource(anyLong(), anyString())).thenReturn(Response.noContent().build());

        // Was order form
        // Mock dmgr in all evironments ending with 1
        ResourceElement wasDmgr = createResource(ResourceTypeDO.DeploymentManager, "wasDmgr", new PropertyElement("hostname", "dmgr.host.no"));
        when(fasitRestClient.findResources(any(EnvClass.class), endsWith("1"), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), eq("wasDmgr"))).thenReturn(Lists.newArrayList(wasDmgr));

        ResourceElement wsAdminUser = createResource(ResourceTypeDO.Credential, "wsadminUser", new PropertyElement("username", "srvWas"), new PropertyElement("password", "verySecret"));
        mockFindResource(fasitRestClient, wsAdminUser);
        // was dmgr
        ResourceElement wasLdapUser = createResource(ResourceTypeDO.Credential, "wasLdapUser", new PropertyElement("username", "srvWasLdap"), new PropertyElement("password", "verySecret"));
        mockFindResource(fasitRestClient, wasLdapUser);

        // openAm
        ResourceElement mockUser = createResource(ResourceTypeDO.Credential, "mockUser", new PropertyElement("username", "mockUser"), new PropertyElement("password", "verySecret"));
        when(fasitRestClient.findResources(any(EnvClass.class), endsWith("1"), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), anyString())).thenReturn(Lists.newArrayList(mockUser));
        when(fasitRestClient.findResources(any(EnvClass.class), endsWith("2"), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), anyString())).thenReturn(Lists.newArrayList(mockUser));
        when(fasitRestClient.getApplicationInstance(endsWith("2"), eq("openAm"))).thenReturn(createOpenAmAppInstance());

        // bpm
        ResourceElement bpmDmgr = createResource(ResourceTypeDO.DeploymentManager, "bpmDmgr", new PropertyElement("hostname", "dmgr.host.no"));
        when(fasitRestClient.findResources(any(EnvClass.class), endsWith("1"), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), eq("bpmDmgr"))).thenReturn(Lists.newArrayList(bpmDmgr));
        ResourceElement srvBpm = createResource(ResourceTypeDO.Credential, "srvBpm", new PropertyElement("username", "srvBpm"), new PropertyElement("password", "verySecretAlso"));
        mockFindResource(fasitRestClient, srvBpm);
        ResourceElement database = createResource(ResourceTypeDO.DataSource, "mocked", new PropertyElement("url", "mockedUrl"), new PropertyElement("username", "dbuser"), new PropertyElement("password", "yep"));
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DataSource), Matchers.startsWith("bpm"))).thenReturn(Lists.newArrayList(database));

        // mq
        mockFindResource(fasitRestClient, createResource(ResourceTypeDO.Queue, "existingQueue", new PropertyElement("queueName", "QA.EXISTING_QUEUE")));
        mockFindResource(fasitRestClient, createResource(ResourceTypeDO.Topic, "existingTopic", new PropertyElement("topicString", "hei/aloha/mock")));
        mockFindResource(fasitRestClient, createResource(ResourceTypeDO.Channel, "existingChannel", new PropertyElement("name", "U1_MOCK_CHANNEL")));

        // Lage sertifikat
        ResourceElement certificatResource = createResource(ResourceTypeDO.Certificate, "alias");
        when(fasitRestClient.executeMultipart(anyString(), anyString(), any(MultipartFormDataOutput.class), anyString(), eq(ResourceElement.class))).thenReturn(certificatResource);

        //bigip
        ResourceElement bigipResource = createResource(ResourceTypeDO.LoadBalancer, "bigip", new PropertyElement("url", "http://some.roi"));
        when(fasitRestClient.getResource(any(), eq("bigip"), eq(ResourceTypeDO.LoadBalancer), any(DomainDO.class), anyString())).thenReturn(bigipResource);



        return fasitRestClient;
    }

    public ApplicationInstanceDO createOpenAmAppInstance() {
        ApplicationInstanceDO appinstance = new ApplicationInstanceDO();
        ClusterDO cluster = new ClusterDO();
        NodeDO node = new NodeDO();
        node.setHostname("mocked.devillo.no");
        node.setPlatformType(PlatformTypeDO.OPENAM_SERVER);
        cluster.addNode(node);

        appinstance.setCluster(cluster);
        return appinstance;
    }

    private void mockFindResource(FasitRestClient fasitRestClient, ResourceElement... resources) {
        ResourceElement resource=resources[0];
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(resource.getType()), eq(resource.getAlias()))).thenReturn(Lists.newArrayList(resources));
    }

    private ResourceElement createResource(ResourceTypeDO type, String alias, PropertyElement... properties) {
        ResourceElement resource = new ResourceElement();
        resource.setAlias(alias);
        resource.setType(type);
        resource.setId(100l);
        resource.setRevision(500l);
        resource.setRef(URI.create("http://mocketdup.no/resource"));
        for (PropertyElement property : properties) {
            resource.addProperty(property);
        }

        return resource;
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
            }
        };

        Answer<?> startAnswer = new Answer<WorkflowToken>() {
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                StartRequest startRequest = (StartRequest) invocation.getArguments()[0];
                startProvisionVM(startRequest);
                return returnRandomToken();
            }
        };

        when(service.decommission(Mockito.anyObject())).thenAnswer(decommissionAnswer);
        when(service.stop(Mockito.anyObject())).thenAnswer(stopAnswer);
        when(service.start(Mockito.anyObject())).thenAnswer(startAnswer);
        when(service.provision(Mockito.<ProvisionRequest> anyObject())).thenAnswer(provisionAnswer);
        when(service.getOrderStatus(Mockito.anyString())).thenReturn(Tuple.of(OrderStatus.PROCESSING, ""));
        return service;
    }

    private WorkflowToken returnRandomToken() {
        WorkflowToken token = new WorkflowToken();
        token.setId(UUID.randomUUID().toString());
        return token;
    }

    @Bean
    public OracleClient getOracleClient() {
        logger.info("mocking Oracle client");
        final OracleClient oracleClientMock = mock(OracleClient.class);

        when(oracleClientMock.getDeletionOrderStatus(anyString())).thenReturn(new HashMap()); // mocks response when deletion is
        // completed
        when(oracleClientMock.getOrderStatus(anyString())).thenReturn(createOEMReadyResponse());
        when(oracleClientMock.createDatabase(anyString(), anyString(), anyString(), anyString())).thenReturn("/em/cloud/dbaas/pluggabledbplatforminstance/byrequest/6969");
        when(oracleClientMock.getStatus(anyString())).thenReturn("RUNNING");
        when(oracleClientMock.deleteDatabase(anyString())).thenReturn("/em/cloud/dbaas/pluggabledbplatforminstance/byrequest/6969");

        return oracleClientMock;
    }

    @Bean
    public BigIPClient getBigIPClient() {
        logger.info("mocking bigIP client");
        final BigIPClient bigipClientMock = mock(BigIPClient.class);

        return bigipClientMock;
    }





    private static HashMap createOEMReadyResponse() {
        final HashMap orderStatus = new HashMap();
        final HashMap state = new HashMap();
        state.put("state", "READY");
        orderStatus.put("resource_state", state);
        return orderStatus;
    }

    private void putProvisionVM(ProvisionRequest provisionRequest) {

        OrchestratorNodeDOList vms = new OrchestratorNodeDOList();
        String[] split = provisionRequest.getStatusCallbackUrl().getPath().split("/");
        String orderNum = split[split.length - 2];

        for (int i = 0; i < provisionRequest.getVms().size(); i++) {
            OrchestratorNodeDO node = new OrchestratorNodeDO();

            node.setHostName("e" + orderNum + i + ".devillo.no");
            quackLikeA(node);
            vms.addVM(node);
        }

        executorService.execute(new HTTPTask(provisionRequest.getResultCallbackUrl(), vms, HTTPOperation.PUT));
        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "provision", StatusLogLevel.success));
        executorService.execute(new HTTPTask(provisionRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));
    }

    private void quackLikeA(OrchestratorNodeDO node) {
        node.setMiddlewareType(MiddlewareType.jb);
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopProvisionVM(StopRequest stopRequest) {
        sleepALittle();
        OperationResponse response = new OperationResponse();
        List<OperationResponseVm> vmList = new ArrayList<>();
        for (String hostname : stopRequest.getPoweroff()) {
            vmList.add(new OperationResponseVm(hostname + ".devillo.no", ResultType.off));
        }
        response.setVms(vmList);
        executorService.execute(new HTTPTask(stopRequest.getResultCallbackUrl(), response, HTTPOperation.PUT));
        sleepALittle();

        OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "stop", StatusLogLevel.success));
        executorService.execute(new HTTPTask(stopRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));
    }

    private void startProvisionVM(StartRequest startRequest) {
        sleepALittle();
        OperationResponse response = new OperationResponse();
        List<OperationResponseVm> vmList = new ArrayList<>();
        for (String hostname : startRequest.getPoweron()) {
            vmList.add(new OperationResponseVm(hostname + ".devillo.no", ResultType.on));
        }
        response.setVms(vmList);
        executorService.execute(new HTTPTask(startRequest.getStartCallbackUrl(), response, HTTPOperation.PUT));
        sleepALittle();
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
