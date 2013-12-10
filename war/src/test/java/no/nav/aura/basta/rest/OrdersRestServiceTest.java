package no.nav.aura.basta.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV2FactoryTest;
import no.nav.aura.basta.persistence.ApplicationServerType;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.persistence.SettingsRepository;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.vmware.orchestrator.requestv1.ProvisionRequest;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public class OrdersRestServiceTest {

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private OrdersRestService ordersRestService;

    @Inject
    private NodeRepository nodeRepository;

    @Inject
    private SettingsRepository settingsRepository;

    @Inject
    private FasitRestClient fasitRestClient;

    @Inject
    private OrchestratorService orchestratorService;

    @Test(expected = UnauthorizedException.class)
    public void notAuthorisedOrderPosted() throws Exception {
        orderWithEnvironmentClass(EnvironmentClass.p, false);
    }

    @Test
    public void authorisedOrderPosted() throws Exception {
        orderWithEnvironmentClass(EnvironmentClass.u, true);
    }

    @SuppressWarnings("serial")
    private void orderWithEnvironmentClass(final EnvironmentClass environmentClass, final boolean expectChanges) {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                Settings settings = OrderV2FactoryTest.createRequestJbossSettings();
                settings.setEnvironmentClass(environmentClass);
                WorkflowToken workflowToken = new WorkflowToken();
                String orchestratorOrderId = UUID.randomUUID().toString();
                workflowToken.setId(orchestratorOrderId);
                if (expectChanges) {
                    when(orchestratorService.send(Mockito.<ProvisionRequest> anyObject())).thenReturn(workflowToken);
                }
                ordersRestService.postOrder(new SettingsDO(settings), createUriInfo());
                if (expectChanges) {
                    verify(orchestratorService).send(Mockito.<ProvisionRequest> anyObject());
                    assertThat(orderRepository.findByOrchestratorOrderId(orchestratorOrderId), notNullValue());
                }
            }
        });
    }

    private UriInfo createUriInfo() {
        try {
            UriInfo uriInfo = mock(UriInfo.class);
            when(uriInfo.getRequestUriBuilder()).thenReturn(UriBuilder.fromUri(new URI("http://unittest:666/")));
            return uriInfo;
        } catch (IllegalArgumentException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void vmReceiveApplicationServer_createsFasitNode() {
        Order order = createMinimalOrderAndSettings(NodeType.APPLICATION_SERVER);
        OrchestratorNodeDO vm = new OrchestratorNodeDO();
        vm.setMiddlewareType(ApplicationServerType.jb);
        ordersRestService.putVmInformation(order.getId(), vm);
        verify(fasitRestClient).registerNode(Mockito.<NodeDO> any());
        assertThat(nodeRepository.findByOrderId(order.getId()).size(), equalTo(1));
    }

    @Test
    public void vmReceiveWASDeploymentManager_createsFasitResource() {
        Order order = createMinimalOrderAndSettings(NodeType.WAS_DEPLOYMENT_MANAGER);
        OrchestratorNodeDO vm = new OrchestratorNodeDO();
        vm.setMiddlewareType(ApplicationServerType.wa);
        ordersRestService.putVmInformation(order.getId(), vm);
        verify(fasitRestClient).registerResource(Mockito.<ResourceElement> any());
        assertThat(nodeRepository.findByOrderId(order.getId()).size(), equalTo(1));
    }

    private Order createMinimalOrderAndSettings(NodeType nodeType) {
        Order order = orderRepository.save(new Order());
        SettingsDO settingsDO = new SettingsDO();
        settingsDO.setNodeType(nodeType);
        settingsDO.setEnvironmentClass(EnvironmentClass.t);
        settingsDO.setZone(Zone.fss);
        settingsRepository.save(new Settings(order, settingsDO));
        return order;
    }

}
