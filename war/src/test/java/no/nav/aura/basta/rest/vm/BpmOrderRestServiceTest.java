package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

public class BpmOrderRestServiceTest extends AbstractOrchestratorTest {

    private BpmOrderRestService service;

    @Before
    public void setup(){
        service = new BpmOrderRestService(orderRepository, orchestratorClient, fasit);
        login("user", "user");
    }

    @Test
    public void orderNewBpmNodeShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setServerCount(1);
        input.setMemory(16);
        input.setCpuCount(4);
        input.setEnvironmentName("u1");

        mockOrchestratorProvision();
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), anyString())).thenReturn(Lists.newArrayList(getDmgr()));
        mockStandard();

        Response response = service.createBpmNode(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertThat(order.getExternalId(), is(notNullValue()));
//        assertThat(order.getExternalRequest(), not(containsString("password")));
//        assertThat(order.getExternalRequest(), containsString("srvUser"));

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/bpm_node_order.xml");
    }

    @Test
    public void orderNewBpmDgmrShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.fss);
        input.setMemory(4);
        input.setCpuCount(2);
        input.setEnvironmentName("u1");

        mockOrchestratorProvision();
        // when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(),
        // eq(ResourceTypeDO.DeploymentManager), anyString())).thenReturn(new ArrayList<ResourceElement>());
        mockStandard();

        Response response = service.createBpmDmgr(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertThat(order.getExternalId(), is(notNullValue()));
//        assertThat(order.getExternalRequest(), not(containsString("password")));
//        assertThat(order.getExternalRequest(), containsString("srvUser"));

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/bpm_dmgr_order.xml");

    }

    private void mockStandard() {
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wsadminUser"))).thenReturn(Lists.newArrayList(getUser()));
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wasLdapUser"))).thenReturn(Lists.newArrayList(getUser()));
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("srvBpm"))).thenReturn(Lists.newArrayList(getUser()));
        when(fasit.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DataSource), anyString())).thenReturn(Lists.newArrayList(createDatabase()));

    }

    private ResourceElement createDatabase() {
        return createResource(ResourceTypeDO.DataSource, "mockedDataSource", new PropertyElement("username", "dbuser"), new PropertyElement("password", "password"), new PropertyElement("url",
                "mocked_dburl"));
    }

    private ResourceElement getUser() {
        return createResource(ResourceTypeDO.Credential, "mockedUser", new PropertyElement("username", "srvUser"), new PropertyElement("password", "password"));
    }

    private ResourceElement getDmgr() {
        return createResource(ResourceTypeDO.DeploymentManager, "mockedDmgr", new PropertyElement("hostname", "dmgr.domain.no"));
    }


}
