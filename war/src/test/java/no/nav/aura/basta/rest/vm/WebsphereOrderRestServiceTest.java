package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.core.Response;

import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.ProvisionRequest2;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class WebsphereOrderRestServiceTest extends AbstractOrchestratorTest {

    private WebsphereOrderRestService service;

    @Before
    public void setup(){
        service = new WebsphereOrderRestService(orderRepository, orchestratorService, fasitRestClient);
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
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), eq("wasDmgr"))).thenReturn(Lists.newArrayList(getDmgr()));
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wsadminUser"))).thenReturn(Lists.newArrayList(getUser()));

        Response response = service.createWasNode(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertThat(order.getExternalId(), is(notNullValue()));
        assertThat(order.getExternalRequest(), not(containsString("password")));
        assertThat(order.getExternalRequest(), containsString("srvUser"));

        ProvisionRequest2 request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was_node_order.xml");
    }

    @Test
    public void orderNewWebsphereDgmrShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setMemory(4);
        input.setCpuCount(2);
        input.setEnvironmentName("u1");

        mockOrchestratorProvision();
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), eq("wasDmgr"))).thenReturn(new ArrayList<ResourceElement>());
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wsadminUser"))).thenReturn(Lists.newArrayList(getUser()));
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wasLdapUser"))).thenReturn(Lists.newArrayList(getUser()));

        Response response = service.createWasDmgr(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertThat(order.getExternalId(), is(notNullValue()));
        assertThat(order.getExternalRequest(), not(containsString("password")));
        assertThat(order.getExternalRequest(), containsString("srvUser"));

        ProvisionRequest2 request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was_dmgr_order.xml");

    }

    private ResourceElement getUser() {
        return createResource(ResourceTypeDO.Credential, "user", new PropertyElement("username", "srvUser"), new PropertyElement("password", "password"));
    }

    private ResourceElement getDmgr() {
        return createResource(ResourceTypeDO.DeploymentManager, "wasDmgr", new PropertyElement("hostname", "dmgr.domain.no"));
    }


}
