package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

public class WebsphereOrderRestServiceTest extends AbstractOrchestratorTest {

    private WebsphereOrderRestService service;

    @Before
    public void setup() {
        service = new WebsphereOrderRestService(orderRepository, orchestratorClient, fasit);
        login("user", "user");
    }

    @Test
    public void orderNewWebsphereNodeShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.sbs);
        input.setServerCount(1);
        input.setMemory(2);
        input.setCpuCount(2);
        input.setClassification(Classification.standard);
        input.setApplicationMappingName("myapp");
        input.setEnvironmentName("u1");

        mockOrchestratorProvision();
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), eq("wasDmgr"))).thenReturn(Lists.newArrayList(getDmgr("wasDmgr")));
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wsadminUser"))).thenReturn(Lists.newArrayList(getUser()));

        Response response = service.createWasNode(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertThat(order.getExternalId(), is(notNullValue()));

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was_node_order.xml");
    }

    @Test
    public void orderNewWebsphere9NodeShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.sbs);
        input.setServerCount(1);
        input.setMemory(2);
        input.setCpuCount(2);
        input.setClassification(Classification.standard);
        input.setApplicationMappingName("myapp");
        input.setEnvironmentName("u1");
        input.setNodeType(NodeType.WAS9_NODES);

        mockOrchestratorProvision();
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), eq("was9Dmgr"))).thenReturn(Lists.newArrayList(getDmgr("was9Dmgr")));
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wsadminUser"))).thenReturn(Lists.newArrayList(getUser()));

        Response response = service.createWasNode(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertNotNull(order.getExternalId());

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was9_node_order.xml");
    }

    @Test
    public void orderNewWebsphereDgmrShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setMemory(4);
        input.setCpuCount(2);
        input.setEnvironmentName("u1");
        input.setNodeType(NodeType.WAS_DEPLOYMENT_MANAGER);

        mockOrchestratorProvision();
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), eq("wasDmgr"))).thenReturn(new ArrayList<ResourceElement>());
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wsadminUser"))).thenReturn(Lists.newArrayList(getUser()));
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wasLdapUser"))).thenReturn(Lists.newArrayList(getUser()));

        Response response = service.createWasDmgr(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertThat(order.getExternalId(), is(notNullValue()));

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was_dmgr_order.xml");

    }

    @Test
    public void orderNewWebsphere9DgmrShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setMemory(4);
        input.setCpuCount(2);
        input.setEnvironmentName("u1");
        input.setNodeType(NodeType.WAS9_DEPLOYMENT_MANAGER);

        mockOrchestratorProvision();
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), eq("was9Dmgr"))).thenReturn(new ArrayList<ResourceElement>());
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wsadminUser"))).thenReturn(Lists.newArrayList(getUser()));
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wasLdapUser"))).thenReturn(Lists.newArrayList(getUser()));

        Response response = service.createWasDmgr(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertNotNull(order.getExternalId());

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was9_dmgr_order.xml");

    }

    private ResourceElement getUser() {
        return createResource(ResourceTypeDO.Credential, "user", new PropertyElement("username", "srvUser"), new PropertyElement("password", "password"));
    }

    private ResourceElement getDmgr(String alias) {
        return createResource(ResourceTypeDO.DeploymentManager, alias, new PropertyElement("hostname", "dmgr.domain.no"));
    }
}