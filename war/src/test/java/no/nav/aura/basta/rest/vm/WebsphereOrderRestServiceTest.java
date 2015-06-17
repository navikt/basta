package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.core.Response;

import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.v2.ProvisionRequest2;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.rest.RestServiceTestUtils;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class WebsphereOrderRestServiceTest extends AbstractOrchestratorTest {

    private WebsphereOrderRestService ordersRestService;

    @Before
    public void setup(){
        ordersRestService = new WebsphereOrderRestService(orderRepository, orchestratorService, fasitRestClient);
        mockLogin("user", "user");
    }

    @Test
    public void orderNewShouldGiveNiceXml() {

        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.sbs);
        input.setServerCount(1);
        input.setMemory(1024);
        input.setCpuCount(2);
        input.setClassification(Classification.standard);
        input.setApplicationMappingName("myapp");
        input.setEnvironmentName("u1");

        mockOrchestratorProvision();
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.DeploymentManager), eq("wasDmgr"))).thenReturn(Lists.newArrayList(getDmgr()));

        Response response = ordersRestService.createWasNode(input.copy(), createUriInfo());

        Long orderId = RestServiceTestUtils.getOrderIdFromMetadata(response);
        Order order = orderRepository.findOne(orderId);
        assertThat(order, notNullValue());

        ProvisionRequest2 request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/was_node_order.xml");
	}



    private ResourceElement getDmgr() {
        return createResource(ResourceTypeDO.DeploymentManager, "wasDmgr", new PropertyElement("hostname", "dmgr.domain.no"));
    }


}
