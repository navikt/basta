    package no.nav.aura.basta.spring;

    import com.bettercloud.vault.Vault;
    import com.bettercloud.vault.VaultException;
    import com.bettercloud.vault.api.Auth;
    import com.bettercloud.vault.api.Logical;
    import com.bettercloud.vault.response.LookupResponse;
    import com.google.common.collect.ImmutableMap;
    import com.google.common.collect.Lists;
    import com.google.common.collect.Sets;
    import com.google.gson.Gson;
    import io.prometheus.client.exporter.MetricsServlet;
    import no.nav.aura.basta.backend.BigIPClient;
    import no.nav.aura.basta.backend.OracleClient;
    import no.nav.aura.basta.backend.RestClient;
    import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
    import no.nav.aura.basta.backend.mq.MqAdminUser;
    import no.nav.aura.basta.backend.mq.MqQueue;
    import no.nav.aura.basta.backend.mq.MqQueueManager;
    import no.nav.aura.basta.backend.mq.MqService;
    import no.nav.aura.basta.backend.serviceuser.ActiveDirectory;
    import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
    import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
    import no.nav.aura.basta.backend.serviceuser.cservice.GeneratedCertificate;
    import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
    import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
    import no.nav.aura.basta.backend.vmware.orchestrator.WorkflowExecutionStatus;
    import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
    import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
    import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
    import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
    import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponse;
    import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponseVm;
    import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponseVm.ResultType;
    import no.nav.aura.basta.domain.OrderStatusLog;
    import no.nav.aura.basta.domain.input.EnvironmentClass;
    import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
    import no.nav.aura.basta.rest.FasitLookupService;
    import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
    import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
    import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
    import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;
    import no.nav.aura.basta.util.HTTPOperation;
    import no.nav.aura.basta.util.HTTPTask;
    import no.nav.aura.envconfig.client.*;
    import no.nav.aura.envconfig.client.DomainDO.EnvClass;
    import no.nav.aura.envconfig.client.rest.PropertyElement;
    import no.nav.aura.envconfig.client.rest.ResourceElement;
    import org.jboss.resteasy.core.ServerResponse;
    import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
    import org.mockito.ArgumentMatchers;
    import org.mockito.Mockito;
    import org.mockito.stubbing.Answer;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
    import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
    import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
    import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
    import org.springframework.boot.web.servlet.ServletRegistrationBean;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.context.annotation.Import;
    import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
    import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

    import javax.sql.DataSource;
    import javax.ws.rs.core.Response;
    import javax.ws.rs.core.UriBuilder;
    import java.net.URI;
    import java.security.KeyStore;
    import java.util.*;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;

    import static java.util.Arrays.asList;
    import static org.mockito.ArgumentMatchers.*;
    import static org.mockito.Mockito.mock;
    import static org.mockito.Mockito.when;

@Configuration
@EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class})
@Import({SpringDbConfig.class, SpringSecurityTestConfig.class})
public class StandaloneRunnerTestConfig {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneRunnerTestConfig.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Bean
    public DataSource getDataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    public static BeanFactoryPostProcessor init() {
        System.setProperty("rest_orchestrator_provision_url", "http://provisionurl.com");
        System.setProperty("rest_orchestrator_decomission_url", "http://provisionurl.com");
        System.setProperty("rest_orchestrator_startstop_url", "http://provisionurl.com");

        System.setProperty("ws.menandmice.url", "https://someserver/menandmice/webservice");
        System.setProperty("ws.menandmice.username", "mmName");
        System.setProperty("ws.menandmice.password", "mmSecret");

        System.setProperty("fasit_resources_v2_url", "https://thefasitresourceapi.com");
        System.setProperty("fasit_nodes_v2_url", "https://thefasitresourceapi.com");
        System.setProperty("fasit_scopedresource_v2_url", "https://thefasitscopedresourceapi.com");
        System.setProperty("fasit_lifecycle_v1_url", "https://thefasitscopedresourceapi.com");
        System.setProperty("fasit_environments_v2_url", "https://thefasitenvironmentsapi.com");
        System.setProperty("fasit_applications_v2_url", "https://thefasitapplicationsapi.com");
        System.setProperty("fasit_rest_api_url", "https://theoldfasitapi.com");
        System.setProperty("fasit_applicationinstances_v2_url", "https://thefasitappinstanceapi.com");


        System.setProperty("ws_orchestrator_url", "https://someserver/vmware-vmo-webcontrol/webservice");
        System.setProperty("user_orchestrator_username", "orcname");
        System.setProperty("user_orchestrator_password", "secret");
        System.setProperty("rest_orchestrator_provision_url", "http://provisionurl.com");
        System.setProperty("rest_orchestrator_decomission_url", "http://provisionurl.com");
        System.setProperty("rest_orchestrator_startstop_url", "http://provisionurl.com");
        System.setProperty("orchestrator_callback_host", "http://localhost:1337");

        System.setProperty("srvbasta_username", "mjau");
        System.setProperty("srvbasta_password", "pstpst");

        System.setProperty("environment_class", "p");
        System.setProperty("BASTA_OPERATIONS_GROUPS", "6283f2bd-8bb5-4d13-ae38-974e1bcc1aad,cd3983f1-fbc1-4c33-9f31-f7a40e422ccd");
        System.setProperty("BASTA_SUPERUSER_GROUPS", "0000-GA-BASTA_SUPERUSER,6283f2bd-8bb5-4d13-ae38-974e1bcc1aad");
        System.setProperty("BASTA_PRODOPERATIONS_GROUPS", "0000-ga-env_config_S,0000-GA-STDAPPS,6283f2bd-8bb5-4d13-ae38-974e1bcc1aad,cd3983f1-fbc1-4c33-9f31-f7a40e422ccd");

        System.setProperty("security_CA_test_url", "https://certenroll.test.local/certsrv/mscep/");
        System.setProperty("security_CA_test_username", "srvSCEP");
        System.setProperty("security_CA_test_password", "fjas");
        System.setProperty("security_CA_adeo_url", "adeourl");
        System.setProperty("security_CA_adeo_username", "");
        System.setProperty("security_CA_adeo_password", "");
        System.setProperty("security_CA_preprod_url", "preprodurl");
        System.setProperty("security_CA_preprod_username", "srvSCEP");
        System.setProperty("security_CA_preprod_password", "dilldall");

        System.setProperty("oem_url", "https://fjas.adeo.no");
        System.setProperty("oem_username", "eple");
        System.setProperty("oem_password", "banan");

        System.setProperty("BASTA_MQ_U_USERNAME", "srvAura");
        System.setProperty("BASTA_MQ_U_PASSWORD", "bacon");
        System.setProperty("BASTA_MQ_T_USERNAME", "srvAura");
        System.setProperty("BASTA_MQ_T_PASSWORD", "secret");
        System.setProperty("BASTA_MQ_Q_USERNAME", "srvAura");
        System.setProperty("BASTA_MQ_Q_PASSWORD", "secret");
        System.setProperty("BASTA_MQ_P_USERNAME", "srvAura");
        System.setProperty("BASTA_MQ_P_PASSWORD", "secret");

        System.setProperty("BASTADB_TYPE", "h2");
        System.setProperty("BASTADB_URL", "jdbc:h2:mem:basta");
        System.setProperty("BASTADB_ONSHOSTS", "basta:6200");
        System.setProperty("BASTADB_USERNAME", "sa");
        System.setProperty("BASTADB_PASSWORD", "");

        System.setProperty("basta_client_id", "b36e92f3-d48b-473d-8f69-e7887457bd3f");
        System.setProperty("basta_frontend_app_id", "b36e92f3-d48b-473d-8f69-e7887457bd3f");
        System.setProperty("tenant_id", "966ac572-f5b7-4bbe-aa88-c76419c0f851");
        System.setProperty("token_issuer", "https://sts.windows.net/966ac572-f5b7-4bbe-aa88-c76419c0f851/");

        logger.info("init StandaloneRunnerTestConfig");
        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }

    private static HashMap createOEMReadyResponse() {
        final HashMap orderStatus = new HashMap();
        final HashMap state = new HashMap();
        state.put("state", "READY");
        orderStatus.put("resource_state", state);
        return orderStatus;
    }

    @Bean
    public ServletRegistrationBean metricsServlet() {
        return new ServletRegistrationBean(new MetricsServlet(), "/metrics");
    }

    @Bean(name="restClient")
    public RestClient getRestClientMock(){
        RestClient restClient = mock(RestClient.class);
        when(restClient.get(anyString(), eq(Map.class))).thenReturn(Optional.of(new HashMap<>()));
        when(restClient.get(anyString(), eq(List.class))).thenReturn(Optional.of(Collections.singletonList(new HashMap<>())));
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
        when(bigIPClientMock.getVersion()).thenReturn("12.1.2");
        when(bigIPClientMock.deleteRuleFromPolicy(anyString(), anyString(), anyBoolean())).thenReturn(new ServerResponse(null, 404, null));
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
        return new Gson().fromJson(json,Map.class);
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

        when(proxy.getClusters(anyString())).thenReturn(Sets.newHashSet("a", "b", "c"));

        return proxy;
    }

    @Bean
    public ActiveDirectory getActiveDirectory() {
        logger.info("mocking AD");
        ActiveDirectory activeDirectory = mock(ActiveDirectory.class);
        Answer<?> suaAnswer = (Answer<ServiceUserAccount>) invocation -> (ServiceUserAccount) invocation.getArguments()[0];
        when(activeDirectory.createOrUpdate(any(ServiceUserAccount.class))).then(suaAnswer);
        when(activeDirectory.groupExists(any(ServiceUserAccount.class), anyString())).thenReturn(false);
        when(activeDirectory.userExists(any(ServiceUserAccount.class))).thenReturn(false);
        return activeDirectory;
    }

    @Bean
    public MqService getMqService() {
        logger.info("mocking MQ");
        MqService mqService = mock(MqService.class);
        Map<EnvironmentClass, MqAdminUser> envCredMap = new HashMap<>();
        envCredMap.put(EnvironmentClass.u, new MqAdminUser("mqadmin", "secret", "SRVAURA.ADMIN"));
        when(mqService.queueExists(any(MqQueueManager.class), endsWith("EXISTS"))).thenReturn(true);
        when(mqService.deleteQueue(any(MqQueueManager.class), anyString())).thenReturn(true);
        Answer<?> queueAnswer = (Answer<Optional<MqQueue>>) invocation -> {
            String queueName = (String) invocation.getArguments()[1];
            return Optional.of(new MqQueue(queueName, 1, 1, "mockup queue for test"));
        };
        when(mqService.getQueue(any(MqQueueManager.class), anyString())).thenAnswer(queueAnswer);
        when(mqService.getClusterNames(any(MqQueueManager.class))).thenReturn(asList("NL.DEV.D1.CLUSTER", "NL.TEST.T1.CLUSTER"));
        when(mqService.findQueuesAliases(any(MqQueueManager.class), endsWith("*"))).thenReturn(asList("U1_MOCK_QUEUE1", "U1_MOCK_QUEUE2", "U1_MOCK_QUEUE3"));
        when(mqService.findChannelNames(any(MqQueueManager.class), startsWith("U3"))).thenReturn(Collections.singletonList("U3_MYAPP"));
        when(mqService.findChannelNames(any(MqQueueManager.class), eq("*"))).thenReturn(Arrays.asList("U1_MYAPP", "U1_YOURAPP", "U2_MYAPP", "U1_MOCK_CHANNEL"));
        when(mqService.getCredentialMap()).thenReturn(envCredMap);
        return mqService;
    }

    @Bean
    public FasitRestClient getFasitRestClient() {
        logger.info("mocking FasitRestClient");
        FasitRestClient fasitRestClient = mock(FasitRestClient.class);

        when(fasitRestClient.buildResourceQuery(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), any(ResourceTypeDO.class), anyString(), any(), any())).thenReturn(URI.create("http://mocked.up"));
        Answer<?> nodeEchoAnswer = (Answer<NodeDO>) invocation -> {
            NodeDO nodeDO = (NodeDO) invocation.getArguments()[0];
            nodeDO.setRef(new URI("http://foo.fasit.foo"));
            return nodeDO;
        };
        when(fasitRestClient.registerNode(any(NodeDO.class), anyString())).thenAnswer(nodeEchoAnswer);

        // Generisk create og update resource
        Answer<?> resourceEchoAnswer = (Answer<ResourceElement>) invocation -> {
            ResourceElement resource = (ResourceElement) invocation.getArguments()[0];
            resource.setId(102L);
            resource.setRef(new URI("http://foo.fasit.foo"));
            return resource;
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

        // bpm
        ResourceElement bpmDmgr = createResource(ResourceTypeDO.DeploymentManager, "bpm86Dmgr", new PropertyElement
                ("hostname", "dmgr.host.no"));
        when(fasitRestClient.findResources(any(EnvClass.class), endsWith("1"), any(DomainDO.class), anyString(), eq
                (ResourceTypeDO.DeploymentManager), eq("bpm86Dmgr"))).thenReturn(Lists.newArrayList(bpmDmgr));
        ResourceElement srvBpm = createResource(ResourceTypeDO.Credential, "srvBpm", new PropertyElement("username", "srvBpm"), new PropertyElement("password", "verySecretAlso"));
        mockFindResource(fasitRestClient, srvBpm);
        ResourceElement database = createResource(ResourceTypeDO.DataSource, "mocked", new PropertyElement("url", "mockedUrl"), new PropertyElement("username", "dbuser"), new PropertyElement("password", "yep"));
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DataSource), ArgumentMatchers.startsWith("bpm"))).thenReturn(Lists.newArrayList(database));

        // mq
        mockFindResource(fasitRestClient, createResource(ResourceTypeDO.Queue, "existingQueue", new PropertyElement("queueName", "QA.EXISTING_QUEUE")));
        mockFindResource(fasitRestClient, createResource(ResourceTypeDO.Channel, "existingChannel", new PropertyElement("name", "U1_MOCK_CHANNEL")));

        // Lage sertifikat
        ResourceElement certificatResource = createResource(ResourceTypeDO.Certificate, "alias");
        when(fasitRestClient.executeMultipart(anyString(), anyString(), any(MultipartFormDataOutput.class), anyString(), eq(ResourceElement.class))).thenReturn(certificatResource);

        //bigip
        ResourceElement bigipResource = createResource(ResourceTypeDO.LoadBalancer, "bigip", new PropertyElement("url", "http://some.roi"));
        when(fasitRestClient.getResource(any(), eq("bigip"), eq(ResourceTypeDO.LoadBalancer), any(DomainDO.class), anyString())).thenReturn(bigipResource);



        return fasitRestClient;
    }


    private void mockFindResource(FasitRestClient fasitRestClient, ResourceElement... resources) {
        ResourceElement resource=resources[0];
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(resource.getType()), eq(resource.getAlias()))).thenReturn(Lists.newArrayList(resources));
    }

       private ResourceElement createResource(ResourceTypeDO type, String alias, PropertyElement... properties) {
        ResourceElement resource = new ResourceElement();
        resource.setAlias(alias);
        resource.setType(type);
        resource.setId(100L);
        resource.setRevision(500L);
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
    public OrchestratorClient getOrchestratorClient() {
        logger.info("mocking OrchestratorService");
        OrchestratorClient client = mock(OrchestratorClient.class);

        Answer<?> provisionAnswer = (Answer<Optional<String>>) invocation -> {
            ProvisionRequest provisionRequest = (ProvisionRequest) invocation.getArguments()[0];
            putProvisionVM(provisionRequest);
            return Optional.of("http://url.to.orchestrator.execution.for.this.provision.order");
        };

        Answer<?> decommissionAnswer = (Answer<Optional<String>>) invocation -> {
            DecomissionRequest decomissionRequest = (DecomissionRequest) invocation.getArguments()[0];
            removeVM(decomissionRequest);
            return Optional.of("http://url.to.orchestrator.execution.for.this.decomission.order");
        };

        Answer<?> workflowExecutionStatusAnswer = (Answer<WorkflowExecutionStatus>) invocation -> WorkflowExecutionStatus.RUNNING;

        Answer<?> workflowExecutionLogs = (Answer<List<String>>) invocation -> {
            ArrayList<String> logs = new ArrayList<>();
            logs.add("something horrible happened on orchestrator");
            return logs;
        };

        Answer<?> stopAnswer = (Answer<Optional<String>>) invocation -> {
            StopRequest stopRequest = (StopRequest) invocation.getArguments()[0];
            stopProvisionVM(stopRequest);
            return Optional.of("http://url.to.orchestrator.execution.for.this.stop.order");
        };

        Answer<?> startAnswer = (Answer<Optional<String>>) invocation -> {
            StartRequest startRequest = (StartRequest) invocation.getArguments()[0];
            startProvisionVM(startRequest);
            return Optional.of("http://url.to.orchestrator.execution.for.this.start.order");
        };

        when(client.decomission(Mockito.any())).thenAnswer(decommissionAnswer);
        when(client.stop(Mockito.any())).thenAnswer(stopAnswer);
        when(client.start(Mockito.any())).thenAnswer(startAnswer);
        when(client.provision(Mockito.<ProvisionRequest> any())).thenAnswer(provisionAnswer);
        when(client.getWorkflowExecutionState(anyString())).thenAnswer(workflowExecutionStatusAnswer);
        when(client.getWorkflowExecutionErrorLogs(anyString())).thenAnswer(workflowExecutionLogs);
        return client;
    }

    @Bean
    public OracleClient getOracleClient() {
        logger.info("mocking Oracle client");
        final OracleClient oracleClientMock = mock(OracleClient.class);

        // completed
        when(oracleClientMock.getOrderStatus(anyString())).thenReturn(createOEMReadyResponse());
        when(oracleClientMock.createDatabase(anyString(), anyString(), anyString(), anyString())).thenReturn("/em/cloud/dbaas/pluggabledbplatforminstance/byrequest/6969");
        when(oracleClientMock.getStatus(anyString())).thenReturn("RUNNING");

        when(oracleClientMock.getTemplatesForZone(anyString())).thenReturn(Lists.newArrayList(
                ImmutableMap.of("uri", "someuri", "name", "u_somename", "description", "en template"),
                ImmutableMap.of("uri", "someuri", "name", "p_somename", "description", "en template")
        ));

        return oracleClientMock;
    }

    @Bean
    public Vault getVaultClient() throws VaultException {
        logger.info("Mocking Vault client");
        Vault mock = mock(Vault.class);
        when(mock.logical()).thenReturn(mock(Logical.class));
        Auth mockAuth = mock(Auth.class);
        LookupResponse response = mock(LookupResponse.class);
        when(response.getTTL()).thenReturn(1337L);
        when(mockAuth.lookupSelf()).thenReturn(response);
        when(mock.auth()).thenReturn(mockAuth);
        return mock;
    }

    @Bean
    public BigIPClient getBigIPClient() {
        logger.info("mocking bigIP client");

        return mock(BigIPClient.class);
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
            System.out.println("Sleep interrupted");
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
