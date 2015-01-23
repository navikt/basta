package no.nav.aura.basta.rest;

import static no.nav.aura.basta.rest.UriFactory.createOrderApiUri;

import java.net.URI;
import java.util.HashMap;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.domain.input.vm.EnvironmentClass;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;

@Component
@Path("/nodes/")
@Transactional
public class NodesRestService {

    private static final Logger logger = LoggerFactory.getLogger(NodesRestService.class);

    @Inject
    private OrderRepository orderRepository;
    @Inject
    private OrchestratorService orchestratorService;

    @POST
    @Path("/decommission")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response decommission(@Context UriInfo uriInfo, String... hostnames) {
        checkDecommissionAccess(hostnames);
        Order order = Order.newDecommissionOrder(hostnames);
        orderRepository.save(order);
        logger.info("created new decommission order: " + order.getId());
        URI statuslogUri = createOrderApiUri(uriInfo, "log", order.getId());
        URI decommissionUri = createOrderApiUri(uriInfo, "remove", order.getId());
        DecomissionRequest request = new DecomissionRequest(hostnames, decommissionUri, statuslogUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "decommissioning", ""));

        WorkflowToken workflowToken = orchestratorService.decommission(request);
        order.setExternalId(workflowToken.getId());
        order.setExternalRequest(OrdersRestService.convertXmlToString(request));
        orderRepository.save(order);

        HashMap<String, Long> result = Maps.newHashMap();
        result.put("orderId", order.getId());
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity(result).build();
    }

    private void checkDecommissionAccess(String... hostnames) {
        for (String hostname : hostnames) {
            EnvironmentClass environmentClass = EnvironmentClass.fromHostname(hostname);
            if (!User.getCurrentUser().hasAccess(environmentClass)) {
                throw new UnauthorizedException("User " + User.getCurrentUser().getName() + " does not have access to decommission node: " + hostname);
            }
        }
    }

    @POST
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stop(@Context UriInfo uriInfo, String... hostnames) {
        checkDecommissionAccess(hostnames);
        Order order = Order.newStopOrder(hostnames);
        orderRepository.save(order);
        URI statuslogUri = createOrderApiUri(uriInfo, "log", order.getId());
        URI stopUri = createOrderApiUri(uriInfo, "stop", order.getId());

        StopRequest request = new StopRequest(hostnames, stopUri, statuslogUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "stopping", ""));
        WorkflowToken workflowToken = orchestratorService.stop(request);
        order.setExternalId(workflowToken.getId());
        order.setExternalRequest(OrdersRestService.convertXmlToString(request));
        orderRepository.save(order);

        HashMap<String, Long> result = Maps.newHashMap();
        result.put("orderId", order.getId());
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity(result).build();
    }

    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(@Context UriInfo uriInfo, String... hostnames) {
        checkDecommissionAccess(hostnames);
        Order order = Order.newStartOrder(hostnames);
        orderRepository.save(order);
        URI resultUri = createOrderApiUri(uriInfo, "log", order.getId());
        URI startUri = createOrderApiUri(uriInfo, "start", order.getId());

        StartRequest request = new StartRequest(hostnames, startUri, resultUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "starting", ""));

        WorkflowToken workflowToken = orchestratorService.start(request);
        order.setExternalId(workflowToken.getId());
        order.setExternalRequest(OrdersRestService.convertXmlToString(request));
        orderRepository.save(order);

        HashMap<String, Long> result = Maps.newHashMap();
        result.put("orderId", order.getId());
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity(result).build();
    }

}
