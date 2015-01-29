/*
package no.nav.aura.basta.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.generated.vmware.ws.WorkflowToken;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.UUID;

import static no.nav.aura.basta.rest.RestServiceTestUtils.createUriInfo;
import static no.nav.aura.basta.rest.RestServiceTestUtils.getOrderIdFromMetadata;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@TransactionConfiguration
@Transactional
public class NodeRestServiceTest {

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private OrdersRestService ordersRestService;

    @Inject
    private NodeRepository nodeRepository;

    @Inject
    private OrchestratorService orchestratorService;

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private NodesRestService nodesRestService;

    @After
    public void resetMockito() {
        Mockito.reset(orchestratorService);
    }

    @Test
    public void order_decommissionSuccess() {
        final String uHostName = "eSomething.some.thing";
        SpringRunAs.runAs(authenticationManager, "user", "user", new Effect() {
            public void perform() {
                Node createdNode = createNode(EnvironmentClass.u, uHostName);


                when(orchestratorService.decommission(Mockito.<DecomissionRequest>anyObject())).thenAnswer(randomWorkFlowTokenAnswer());
                when(orchestratorService.getOrderStatus(anyString())).thenReturn(new Tuple<>(OrderStatus.PROCESSING, ""));

                Response response = nodesRestService.decommission(createUriInfo(), new String[]{uHostName});
                OrderDO orderDO = (OrderDO) ordersRestService.getOrder(getOrderIdFromMetadata(response), createUriInfo()).getEntity();
                OrchestratorNodeDO vm = new OrchestratorNodeDO();
                vm.setHostName(uHostName);
                ordersRestService.removeVmInformation(orderDO.getId(), vm, mock(HttpServletRequest.class));

                ArrayList<Node> nodes = Lists.newArrayList(nodeRepository.findByHostname(uHostName));
                assertThat(nodes, hasSize(1));
                assertThat(nodes.get(0).getOrder().getId(), is(equalTo(createdNode.getOrder().getId())));
                assertThat(nodes.get(0).getDecommissionOrder(), notNullValue());
            }
        });
    }



    @Test(expected = UnauthorizedException.class)
    public void unathorized_user_cannot_decommission_anything() {
        nodesRestService.decommission(createUriInfo(), new String[]{"eDevelopment.environment"});
    }

    @Test(expected = UnauthorizedException.class)
    public void regular_user_cannot_decommission_t() {
        performDecommissionAs("user", "user", "eDevelopment.environment", "dTest.environment");
    }

    @Test()
    public void user_with_operations_role_can_decommission_t() {
        performDecommissionAs("user_operations", "admin", "eDevelopment.environment", "dTest.environment");
    }

    @Test
    public void user_with_operations_role_can_decommission_q() {
        performDecommissionAs("superuser_without_prod", "superuser2", "eDevelopment.environment", "dTest.environment", "bQA.environment");
    }

    @Test(expected = UnauthorizedException.class)
    public void super_user_without_prod_operations_role_cannot_decommission_p() {
        performDecommissionAs("superuser_without_prod", "superuser2", "eDevelopment.environment", "dTest.environment", "bQA.environment", "aProd.environment");
    }

    @Test(expected = UnauthorizedException.class)
    public void must_have_without_prod_operations_role_cannot_decommission_unknown_environment() {
        performDecommissionAs("superuser_without_prod", "superuser2", "unknown.environment");
    }

    @Test
    public void super_user_with_prod_operations_role_can_decommission_p() {
        performDecommissionAs("superuser", "superuser", "eDevelopment.environment", "dTest.environment", "bQA.environment", "aProd.environment");
    }



    private void performDecommissionAs(String username, String password, final String... hostnames){
        when(orchestratorService.decommission(Mockito.<DecomissionRequest>anyObject())).thenReturn(new WorkflowToken());
        SpringRunAs.runAs(authenticationManager, username, password, new Effect() {
            public void perform() {
                nodesRestService.decommission(createUriInfo(), hostnames);
            }
        });
    }

    private Node createNode(EnvironmentClass environmentClass, String hostname) {
        VMOrderInput input = new VMOrderInput(Maps.newHashMap());
        input.setEnvironmentClass(environmentClass);
        input.setNodeType(NodeType.APPLICATION_SERVER);
        Order order = orderRepository.save(Order.newProvisionOrder(input));
        Node node = new Node(order, NodeType.APPLICATION_SERVER, hostname, null, 1, 1024, null, null, null);
        nodeRepository.save(node);
        order.addNode(node);
        orderRepository.save(order);
        return node;
    }

    private Answer<WorkflowToken> randomWorkFlowTokenAnswer() {
        return new Answer<WorkflowToken>() {
            @Override
            public WorkflowToken answer(InvocationOnMock invocation) throws Throwable {
                WorkflowToken workflowToken = new WorkflowToken();
                workflowToken.setId(UUID.randomUUID().toString());
                return workflowToken;
            }
        };
    }

}
*/
