package no.nav.aura.basta.spring;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.response.LookupResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet;
import jakarta.servlet.Servlet;
import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
import no.nav.aura.basta.backend.fasit.rest.model.NodePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SecretPayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
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
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;
import no.nav.aura.basta.util.HTTPOperation;
import no.nav.aura.basta.util.HTTPTask;

@Configuration
@EnableAutoConfiguration(exclude = { FlywayAutoConfiguration.class })
@Import({ SpringDbConfig.class, SpringSecurityTestConfig.class })
public class StandaloneRunnerTestConfig {

	private static final Logger logger = LoggerFactory.getLogger(StandaloneRunnerTestConfig.class);

	private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Bean
    DataSource getDataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
	}

    @Bean
    static BeanFactoryPostProcessor init() {
		System.setProperty("rest_orchestrator_provision_url", "http://provisionurl.com");
		System.setProperty("rest_orchestrator_decomission_url", "http://provisionurl.com");
		System.setProperty("rest_orchestrator_startstop_url", "http://provisionurl.com");
        System.setProperty("ORCHESTRATOR_CALLBACK_HOST_URL", "https://basta/rest");

		System.setProperty("ws.menandmice.url", "https://someserver/menandmice/webservice");
		System.setProperty("ws.menandmice.username", "mmName");
		System.setProperty("ws.menandmice.password", "mmSecret");


		System.setProperty("ws_orchestrator_url", "https://someserver/vmware-vmo-webcontrol/webservice");
		System.setProperty("user_orchestrator_username", "orcname");
		System.setProperty("user_orchestrator_password", "secret");
		System.setProperty("rest_orchestrator_provision_url", "http://provisionurl.com");
		System.setProperty("rest_orchestrator_decomission_url", "http://provisionurl.com");
		System.setProperty("rest_orchestrator_startstop_url", "http://provisionurl.com");
		System.setProperty("orchestrator_callback_host", "http://localhost:1337");
		System.setProperty("ORCHESTRATOR_CALLBACK_HOST_URL", "http://localhost:1337");

		System.setProperty("srvbasta_username", "mjau");
		System.setProperty("srvbasta_password", "pstpst");

		System.setProperty("environment_class", "p");
		System.setProperty("BASTA_OPERATIONS_GROUPS",
				"6283f2bd-8bb5-4d13-ae38-974e1bcc1aad,cd3983f1-fbc1-4c33-9f31-f7a40e422ccd");
		System.setProperty("BASTA_SUPERUSER_GROUPS", "0000-GA-BASTA_SUPERUSER,6283f2bd-8bb5-4d13-ae38-974e1bcc1aad");
		System.setProperty("BASTA_PRODOPERATIONS_GROUPS",
				"0000-ga-env_config_S,0000-GA-STDAPPS,6283f2bd-8bb5-4d13-ae38-974e1bcc1aad,cd3983f1-fbc1-4c33-9f31-f7a40e422ccd");

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

		
		System.setProperty("fasit_base_url", "https://thefasitresourceapi.com"); // because not easy to load from application.properties
		
		logger.info("init StandaloneRunnerTestConfig");
        PropertySourcesPlaceholderConfigurer propertyConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertyConfigurer.setPropertySources(new MutablePropertySources());
		return propertyConfigurer;
	}

	private static HashMap<String, Map<String, String>> createOEMReadyResponse() {
		final HashMap<String, Map<String, String>> orderStatus = new HashMap<>();
		final HashMap<String, String> state = new HashMap<>();
		state.put("state", "READY");
		orderStatus.put("resource_state", state);
		return orderStatus;
	}

    @Bean
    ServletRegistrationBean<Servlet> metricsServlet() {
    	PrometheusMetricsServlet metricServlet = new PrometheusMetricsServlet();
    	ServletRegistrationBean<Servlet> srBean = new ServletRegistrationBean<Servlet>();
    	srBean.setServlet((Servlet) metricServlet);
    	srBean.addUrlMappings("/metrics");
        return srBean;
    }


    @Bean(name = "bigIPClientSetup")
    BigIPClientSetup getBigIPClientService() {
		logger.info("mocking BigIPService");
		final BigIPClientSetup setup = mock(BigIPClientSetup.class);
		final BigIPClient bigIPClientMock = mock(BigIPClient.class);
		when(bigIPClientMock.getVirtualServers(anyString()))
				.thenReturn((List<Map<String, Object>>) createBigIpItemList().get("items"));
		when(bigIPClientMock.getVirtualServer(anyString())).thenReturn(Optional.of(new HashMap<>()));
		when(bigIPClientMock.getPoliciesFrom(anyMap())).thenReturn(new HashSet<>());
		when(bigIPClientMock.getRules(anyString())).thenReturn(createBigIpItemList());
		when(bigIPClientMock.getVersion()).thenReturn("12.1.2");
		when(bigIPClientMock.deleteRuleFromPolicy(anyString(), anyString(), anyBoolean()))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		when(setup.setupBigIPClient(any(BigIPOrderInput.class))).thenReturn(bigIPClientMock);
		return setup;
	}

	private Map<?, ?> createBigIpItemList() {
		String json = "{\n" + "  \"items\": [\n" + "    {\n" + "      \"name\": \"vs_name_1\"\n" + "    },\n"
				+ "    {\n" + "      \"name\": \"vs_name_2\"\n" + "    }\n" + "  ]\n" + "}\n";
		try {
			return new ObjectMapper().readValue(json, Map.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse JSON", e);
		}
	}

//    @Bean
//    FasitLookupService getFasitProxy() {
//		logger.info("mocking fasit proxy");
//		FasitLookupService proxy = mock(FasitLookupService.class);
//		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.findAndRegisterModules(); // This registers JavaTimeModule for LocalDateTime support
//		
//		List<ApplicationPayload> apps = Arrays.asList(new ApplicationPayload("app1", "group", "artifact"),
//				new ApplicationPayload("app2", "group", "artifact"), new ApplicationPayload("fasit", "group", "artifact"));
//		
//		ApplicationListPayload appListPayload = new ApplicationListPayload(apps);
//	
//		try {
//			String appJson = objectMapper.writeValueAsString(appListPayload);
//			when(proxy.getApplications()).thenReturn(new ResponseEntity<>(appJson, HttpStatus.OK));
//		} catch (Exception e) {
//			throw new RuntimeException("Failed to serialize ApplicationListPayload", e);
//		}
//		
////		when(proxy.getApplicationGroups())
////				.thenReturn(objectMapper.writeValueAsString(new ApplicationGroupDO[] { new ApplicationGroupDO("appgroup1") }));
//		
//		List<EnvironmentPayload> envs = Arrays.asList(new EnvironmentPayload("u1", EnvironmentClass.u),
//				new EnvironmentPayload("u2", EnvironmentClass.u), new EnvironmentPayload("t1", EnvironmentClass.t),
//				new EnvironmentPayload("q1", EnvironmentClass.q), new EnvironmentPayload("p", EnvironmentClass.p));
//		
//		EnvironmentListPayload envListPayload = new EnvironmentListPayload(envs);
//		try {
//			String envJson = objectMapper.writeValueAsString(envListPayload);
//			when(proxy.getEnvironments()).thenReturn(new ResponseEntity<>(envJson, HttpStatus.OK));
//		} catch (Exception e) {
//			throw new RuntimeException("Failed to serialize EnvironmentListPayload", e);
//		}
//
//		when(proxy.getClusters(anyString())).thenReturn(new ResponseEntity<>(Sets.newHashSet("a", "b", "c"), HttpStatus.OK));
//
//		return proxy;
//	}

    @Bean
    ActiveDirectory getActiveDirectory() {
		logger.info("mocking AD");
		ActiveDirectory activeDirectory = mock(ActiveDirectory.class);
		Answer<?> suaAnswer = (Answer<ServiceUserAccount>) invocation -> (ServiceUserAccount) invocation
				.getArguments()[0];
		when(activeDirectory.createOrUpdate(any(ServiceUserAccount.class))).then(suaAnswer);
		when(activeDirectory.groupExists(any(ServiceUserAccount.class), anyString())).thenReturn(false);
		when(activeDirectory.userExists(any(ServiceUserAccount.class))).thenReturn(false);
		return activeDirectory;
	}

    @Bean
    MqService getMqService() {
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
		when(mqService.getClusterNames(any(MqQueueManager.class)))
				.thenReturn(asList("NL.DEV.D1.CLUSTER", "NL.TEST.T1.CLUSTER"));
		when(mqService.findQueuesAliases(any(MqQueueManager.class), endsWith("*")))
				.thenReturn(asList("U1_MOCK_QUEUE1", "U1_MOCK_QUEUE2", "U1_MOCK_QUEUE3"));
		when(mqService.findChannelNames(any(MqQueueManager.class), startsWith("U3")))
				.thenReturn(Collections.singletonList("U3_MYAPP"));
		when(mqService.findChannelNames(any(MqQueueManager.class), eq("*")))
				.thenReturn(Arrays.asList("U1_MYAPP", "U1_YOURAPP", "U2_MYAPP", "U1_MOCK_CHANNEL"));
		when(mqService.getCredentialMap()).thenReturn(envCredMap);
		return mqService;
	}

//    @Bean(name = "restClient")
//    RestClient getRestClientMock() {
//		FasitRestClient fasitRestClient = mock(FasitRestClient.class);
//		when(fasitRestClient.get(anyString(), eq(Map.class))).thenReturn(Optional.of(new HashMap<>()));
//		when(fasitRestClient.get(anyString(), eq(List.class)))
//				.thenReturn(Optional.of(Collections.singletonList(new HashMap<>())));
//		return fasitRestClient;
//	}

//	
    @Bean
    @Primary
    FasitRestClient getRestClient() {
		logger.info("Creating FasitRestClient spy for testing");
		// Create a REAL FasitRestClient instance
		FasitRestClient realClient = new FasitRestClient(
			System.getProperty("fasit_base_url"),
			System.getProperty("srvbasta_username"),
			System.getProperty("srvbasta_password")
		);
		// Wrap it in a spy so we can stub specific methods while keeping real behavior for RestTemplate
		FasitRestClient fasitRestClient = Mockito.spy(realClient);
		// The RestTemplate will be injected via @MockBean in AbstractRestServiceTest
		
		FasitUpdateService fasitUpdateService = mock(FasitUpdateService.class);

//		when(restClient.buildResourceQuery(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(),
//				any(ResourceTypeDO.class), anyString(), any(), any())).thenReturn(URI.create("http://mocked.up"));
		
		Answer<?> nodeEchoAnswer = (Answer<NodePayload>) invocation -> {
			NodePayload nodeDO = (NodePayload) invocation.getArguments()[0];
			return nodeDO;
		};
		when(fasitUpdateService.registerNode(any(NodePayload.class), any())).thenAnswer(nodeEchoAnswer);

		// Generisk create og update resource
		Answer<?> resourceEchoAnswer = (Answer<ResourcePayload>) invocation -> {
			ResourcePayload resource = (ResourcePayload) invocation.getArguments()[0];
			resource.id = 102L;
			return resource;
		};
		when(fasitUpdateService.createResource(any(ResourcePayload.class), any(Order.class))).thenAnswer(resourceEchoAnswer);
//		when(fasitUpdateService.updateResource(anyInt(), any(ResourcePayload.class), anyString()))
//				.thenAnswer(resourceEchoAnswer);
		when(fasitUpdateService.createOrUpdateResource(anyLong(), any(ResourcePayload.class), any(Order.class)))
				.thenAnswer(resourceEchoAnswer);
//		when(restClient.deleteResource(anyLong(), anyString())).thenReturn(Response.noContent().build());
		when(fasitUpdateService.deleteResource(anyLong(), anyString(), any(Order.class))).thenReturn(true);

		// Was order form
		// Mock dmgr in all evironments ending with 1
		ResourcePayload wasDmgr = createResource(ResourceType.DeploymentManager, "wasDmgr", Map.of("hostname", "dmgr.host.no"));
		
		// Use doReturn().when() syntax for spy to avoid calling real method during stubbing
		doReturn(wasDmgr).when(fasitRestClient).getScopedFasitResource(eq(ResourceType.DeploymentManager), eq("wasDmgr"), any());
		
		ResourcePayload wsAdminUser = createResource(ResourceType.Credential, "wsadminUser", Map.ofEntries(
				entry("username", "srvWas"),
				entry("password", "verySecret")));
		mockFindResource(fasitRestClient, wsAdminUser);
		
		// was dmgr
//		ResourcePayload wasLdapUser = createResource(ResourceType.Credential, "wasLdapUser",
//				new PropertyElement("username", "srvWasLdap"), new PropertyElement("password", "verySecret"));
		ResourcePayload wasLdapUser = createResource(ResourceType.Credential, "wasLdapUser", Map.ofEntries(
				entry("username", "srvWasLdap")));
		wasLdapUser.setSecrets(Map.ofEntries(entry("password", SecretPayload.withValue("verySecret"))));
		mockFindResource(fasitRestClient, wasLdapUser);

		// bpm
		ResourcePayload bpmDmgr = createResource(ResourceType.DeploymentManager, "bpm86Dmgr", Map.ofEntries(entry("hostname", "dmgr.host.no")));
		
		doReturn(wasDmgr).when(fasitRestClient).getScopedFasitResource(eq(ResourceType.DeploymentManager), eq("bpm86Dmgr"), any());
		
//		ResourcePayload srvBpm = createResource(ResourceType.Credential, "srvBpm",
//				new PropertyElement("username", "srvBpm"), new PropertyElement("password", "verySecretAlso"));
		ResourcePayload srvBpm = createResource(ResourceType.Credential, "srvBpm", Map.ofEntries(
				entry("username", "srvBpm")));
		srvBpm.setSecrets(Map.ofEntries(entry("password", SecretPayload.withValue("verySecretAlso"))));
		mockFindResource(fasitRestClient, srvBpm);
		
//		ResourcePayload database = createResource(ResourceType.DataSource, "mocked",
//				new PropertyElement("url", "mockedUrl"), new PropertyElement("username", "dbuser"),
//				new PropertyElement("password", "yep"));
		ResourcePayload database = createResource(ResourceType.DataSource, "mocked", Map.ofEntries(
				entry("url", "mockedUrl"),
				entry("username", "dbuser")));
		database.setSecrets(Map.ofEntries(entry("password", SecretPayload.withValue("yep"))));
		
		doReturn(database).when(fasitRestClient).getScopedFasitResource(eq(ResourceType.DataSource), ArgumentMatchers.startsWith("bpm"), any());
		
		// mq
		mockFindResource(fasitRestClient, createResource(ResourceType.Queue, "existingQueue", Map.ofEntries(entry("managerName", "U1_MQ_MANAGER"))));
		mockFindResource(fasitRestClient, createResource(ResourceType.Channel, "existingChannel",Map.ofEntries(entry("name", "U1_MOCK_CHANNEL"))));

		// Lage sertifikat
		ResourcePayload certificatResource = createResource(ResourceType.Certificate, "alias", null);
		
//		when(restClient.executeMultipart(anyString(), anyString(), any(MultipartFormDataOutput.class), anyString(),
//				eq(ResourcePayload.class))).thenReturn(certificatResource);
//		when(restClient.createFasitResource(any(ResourcePayload.class), any(Order.class)).thenReturn(Optional.of(certificatResource)));
		mockFindResource(fasitRestClient, certificatResource);
		
		return fasitRestClient;
	}

	private void mockFindResource(FasitRestClient fasitRestClient, ResourcePayload... resources) {
		ResourcePayload resource = resources[0];
		// Use doReturn().when() syntax for spy to avoid calling real method during stubbing
		doReturn(resource).when(fasitRestClient).getScopedFasitResource(any(), eq(resource.getAlias()), eq(resource.getScope()));
	}

//	private ResourceElement createResource(ResourceTypeDO type, String alias, PropertyElement... properties) {
//		ResourceElement resource = new ResourceElement();
//		resource.setAlias(alias);
//		resource.setType(type);
//		resource.setId(100L);
//		resource.setRevision(500L);
//		resource.setRef(URI.create("http://mocketdup.no/resource"));
//		for (PropertyElement property : properties) {
//			resource.addProperty(property);
//		}
//
//		return resource;
//	}
	private ResourcePayload createResource(ResourceType type, String alias, Map<String, String> properties) {
		ResourcePayload resource = new ResourcePayload(type, alias);
		resource.id = 100L;
		resource.revision = 500L;
		resource.setProperties(properties);
		return resource;
	}

    @Bean
    CertificateService getCertificateService() throws Exception {
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
    OrchestratorClient getOrchestratorClient() {
		logger.info("mocking OrchestratorService");
		OrchestratorClient client = mock(OrchestratorClient.class);

		Answer<?> provisionAnswer = (Answer<Optional<String>>) invocation -> {
			ProvisionRequest provisionRequest = (ProvisionRequest) invocation.getArguments()[0];
			putProvisionVM(provisionRequest);
			return Optional.of("http://url.to.orchestrator.execution.for.this.provision.order");
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

		when(client.stop(Mockito.any())).thenAnswer(stopAnswer);
		when(client.start(Mockito.any())).thenAnswer(startAnswer);
		when(client.provision(Mockito.<ProvisionRequest>any())).thenAnswer(provisionAnswer);
		when(client.getWorkflowExecutionState(anyString())).thenAnswer(workflowExecutionStatusAnswer);
		when(client.getWorkflowExecutionErrorLogs(anyString())).thenAnswer(workflowExecutionLogs);
		return client;
	}

    @Bean
    OracleClient getOracleClient() {
		logger.info("mocking Oracle client");
		final OracleClient oracleClientMock = mock(OracleClient.class);

		// completed
		when(oracleClientMock.getOrderStatus(anyString())).thenReturn(createOEMReadyResponse());
		when(oracleClientMock.createDatabase(anyString(), anyString(), anyString(), anyString()))
				.thenReturn("/em/cloud/dbaas/pluggabledbplatforminstance/byrequest/6969");
		when(oracleClientMock.getStatus(anyString())).thenReturn("RUNNING");

		when(oracleClientMock.getTemplatesForZone(anyString())).thenReturn(Lists.newArrayList(
				ImmutableMap.of("uri", "someuri", "name", "u_somename", "description", "en template"),
				ImmutableMap.of("uri", "someuri", "name", "p_somename", "description", "en template")));

		return oracleClientMock;
	}

    @Bean
    Vault getVaultClient() throws VaultException {
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
    BigIPClient getBigIPClient() {
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
		OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator",
				"StandaloneRunnerTestConfig :)", "provision", StatusLogLevel.success));
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

		OrderStatusLogDO success = new OrderStatusLogDO(
				new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "stop", StatusLogLevel.success));
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
		OrderStatusLogDO success = new OrderStatusLogDO(
				new OrderStatusLog("Orchestrator", "StandaloneRunnerTestConfig :)", "start", StatusLogLevel.success));
		executorService.execute(new HTTPTask(startRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));

	}

	private void removeVM(DecomissionRequest decomissionRequest) {
		for (String hostname : decomissionRequest.getVmsToRemove()) {
			OrchestratorNodeDO node = new OrchestratorNodeDO();
			node.setHostName(hostname + ".devillo.no");
			executorService
					.execute(new HTTPTask(decomissionRequest.getDecommissionCallbackUrl(), node, HTTPOperation.PUT));
			sleepALittle();

		}

		OrderStatusLogDO success = new OrderStatusLogDO(new OrderStatusLog("Orchestrator",
				"StandaloneRunnerTestConfig :)", "decommission", StatusLogLevel.success));
		executorService.execute(new HTTPTask(decomissionRequest.getStatusCallbackUrl(), success, HTTPOperation.POST));

	}
}
