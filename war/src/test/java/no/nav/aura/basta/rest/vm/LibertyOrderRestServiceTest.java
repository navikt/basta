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

import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
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

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class LibertyOrderRestServiceTest extends AbstractOrchestratorTest {

    private LibertyOrderRestService ordersRestService;

    @Before
    public void setup() {
        ordersRestService = new LibertyOrderRestService(orderRepository, orchestratorService, fasitRestClient);
        login("user", "user");
    }

    @Test
    public void orderNewShouldGiveNiceXml() {
        VMOrderInput input = new VMOrderInput();
        input.setEnvironmentClass(EnvironmentClass.u);
        input.setZone(Zone.sbs);
        input.setServerCount(2);
        input.setMemory(1);
        input.setCpuCount(4);
        input.setClassification(Classification.standard);
        input.setApplicationMappingName("myapp");
        input.setEnvironmentName("u1");

        mockOrchestratorProvision();
        when(fasitRestClient.findResources(any(EnvClass.class), anyString(), any(DomainDO.class), anyString(), eq(ResourceTypeDO.Credential), eq("wasLdapUser"))).thenReturn(Lists.newArrayList(getUser()));

        Response response = ordersRestService.createLibertyNode(input.copy(), createUriInfo());

        Order order = getCreatedOrderFromResponseLocation(response);
        assertThat(order.getExternalId(), is(notNullValue()));
        assertThat(order.getExternalRequest(), not(containsString("password")));
        assertThat(order.getExternalRequest(), containsString("srvUser"));

        ProvisionRequest request = getAndValidateOrchestratorRequest(order.getId());
        // mock out urls for xml matching
        request.setResultCallbackUrl(URI.create("http://callback/result"));
        request.setStatusCallbackUrl(URI.create("http://callback/status"));
        assertRequestXML(request, "/orchestrator/request/liberty_order.xml");

    }

    private ResourceElement getUser() {
        return createResource(ResourceTypeDO.Credential, "user", new PropertyElement("username", "srvUser"), new PropertyElement("password", "password"));
    }

}
