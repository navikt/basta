package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.DomainDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.ResourceTypeDO;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ScopePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.util.MapBuilder;

public class BpmOrderRestServiceTest extends AbstractOrchestratorTest {
    private BpmOrderRestService service;

    @BeforeEach
    public void setup(){
        service = new BpmOrderRestService(orderRepository, orchestratorClient, fasit);
        login();
    }

    @Test
    public void orderNewBpm86NodeShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setServerCount(1);
        input.setMemory(16);
        input.setCpuCount(4);
        input.setEnvironmentName("u1");
        input.setNodeType(NodeType.BPM86_NODES);

        mockOrchestratorProvision();
        when(fasit.findScopedFasitResource( eq(ResourceType.deploymentmanager), eq("bpm86Dmgr"), any(ScopePayload.class))).thenReturn(getDmgr
                ());
        mockStandard();

        Response response = service.createBpmNode(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        MatcherAssert.assertThat(order.getExternalId(), is(notNullValue()));

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/bpm86_node_order.xml");
    }

    @Test
    public void orderNewBpm86DgmrShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setMemory(4);
        input.setCpuCount(2);
        input.setEnvironmentName("u1");
        input.setNodeType(NodeType.BPM86_DEPLOYMENT_MANAGER);

        mockOrchestratorProvision();
        when(deprecatedFasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq
                (ResourceTypeDO.DeploymentManager), eq("bpm86Dmgr"))).thenReturn(new ArrayList<>());
        mockStandard();

        Response response = service.createBpmDmgr(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        MatcherAssert.assertThat(order.getExternalId(), is(notNullValue()));

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/bpm86_dmgr_order.xml");
    }

    private void mockStandard() {
        when(fasit.findScopedFasitResource(eq(ResourceType.credential), eq("wsadminUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(fasit.findScopedFasitResource(eq(ResourceType.credential), eq("wasLdapUser"), any(ScopePayload.class))).thenReturn(getUser());
        when(fasit.findScopedFasitResource(eq(ResourceType.credential), eq("srvBpm"), any(ScopePayload.class))).thenReturn(getUser());
        when(fasit.findScopedFasitResource(eq(ResourceType.datasource), anyString(), any(ScopePayload.class))).thenReturn(createDatabase());
        when(fasit.getFasitSecret(anyString())).thenReturn("password");
    }

    private Optional<ResourcePayload> createDatabase() {
        return Optional.of(createResourceWithSecret(ResourceType.datasource, "mockedDataSource", MapBuilder.stringMapBuilder().put("username", "dbuser").put("url",
                "mocked_dburl").build()));
    }

    private Optional<ResourcePayload> getUser() {
        return Optional.of(createResourceWithSecret(ResourceType.credential, "mockedUser", MapBuilder.stringMapBuilder().put("username", "srvUser").build()));
    }

    private Optional<ResourcePayload> getDmgr() {
        return Optional.of(createResource(ResourceType.deploymentmanager, "bpm86Dmgr", MapBuilder.stringMapBuilder().put("hostname", "dmgr.domain.no").build()));
    }
}
