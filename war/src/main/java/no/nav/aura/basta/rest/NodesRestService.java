package no.nav.aura.basta.rest;

import com.google.common.collect.Maps;
import no.nav.aura.basta.backend.OrchestratorService;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderRepository;
import no.nav.aura.basta.persistence.OrderStatusLog;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.vmware.orchestrator.request.StopRequest;
import no.nav.generated.vmware.ws.WorkflowToken;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;

import static no.nav.aura.basta.rest.UriFactory.createOrderUri;

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
        URI statuslogUri = createOrderUri(uriInfo, "updateStatuslog", order.getId());
        URI decommissionUri = createOrderUri(uriInfo, "removeVmInformation", order.getId());
        DecomissionRequest request = new DecomissionRequest(hostnames, decommissionUri, statuslogUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "decommissioning", ""));

        WorkflowToken workflowToken = orchestratorService.decommission(request);
        order.setOrchestratorOrderId(workflowToken.getId());
        order.setRequestXml(OrdersRestService.convertXmlToString(request));
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
        URI statuslogUri = createOrderUri(uriInfo, "updateStatuslog", order.getId());
        URI stopUri = createOrderUri(uriInfo, "stopVmInformation", order.getId());

        StopRequest request = new StopRequest(hostnames, stopUri, statuslogUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "stopping", ""));
        WorkflowToken workflowToken = orchestratorService.stop(request);
        order.setOrchestratorOrderId(workflowToken.getId());
        order.setRequestXml(OrdersRestService.convertXmlToString(request));
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
        URI resultUri = createOrderUri(uriInfo, "updateStatuslog", order.getId());
        URI startUri = createOrderUri(uriInfo, "startVmInformation", order.getId());

        StartRequest request = new StartRequest(hostnames, startUri, resultUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "starting", ""));

        WorkflowToken workflowToken = orchestratorService.start(request);
        order.setOrchestratorOrderId(workflowToken.getId());
        order.setRequestXml(OrdersRestService.convertXmlToString(request));
        orderRepository.save(order);

        HashMap<String, Long> result = Maps.newHashMap();
        result.put("orderId", order.getId());
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity(result).build();
    }

}
