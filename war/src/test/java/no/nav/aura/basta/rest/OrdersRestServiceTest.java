package no.nav.aura.basta.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Inject;

import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.order.OrderV1FactoryTest;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.rest.SettingsDO.EnvironmentClassDO;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.vmware.orchestrator.requestv1.ProvisionRequest;
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

    @Test(expected = UnauthorizedException.class)
    public void notAuthorisedOrderPosted() throws Exception {
        orderWithEnvironmentClass(EnvironmentClassDO.prod, false);
    }

    @Test
    public void authorisedOrderPosted() throws Exception {
        orderWithEnvironmentClass(EnvironmentClassDO.utv, true);
    }

    @SuppressWarnings("serial")
    private void orderWithEnvironmentClass(final EnvironmentClassDO environmentClass, final boolean expectChanges) {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                SettingsDO settings = OrderV1FactoryTest.createRequest1Settings();
                settings.setEnvironmentClass(environmentClass);
                OrchestratorService orchestratorServiceMock = mock(OrchestratorService.class);
                WorkflowToken workflowToken = new WorkflowToken();
                String orchestratorOrderId = UUID.randomUUID().toString();
                workflowToken.setId(orchestratorOrderId);
                when(orchestratorServiceMock.send(Mockito.<ProvisionRequest> anyObject())).thenReturn(workflowToken);
                new OrdersRestService(orderRepository, null, orchestratorServiceMock).postOrder(settings);
                if (expectChanges) {
                    verify(orchestratorServiceMock);
                    assertThat(orderRepository.findByOrchestratorOrderId(orchestratorOrderId), notNullValue());
                }
            }
        });
    }

    @Test
    public void resultReceieve() {
        String orchestratorOrderId = UUID.randomUUID().toString();
        Order order = orderRepository.save(new Order(orchestratorOrderId));
        ordersRestService.putVmInformation(order.getId(), new ResultNodeDO());
        assertThat(nodeRepository.findByOrderId(order.getId()).size(), equalTo(1));
    }

}
