package no.nav.aura.basta.rest.vm;

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

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.vmware.OrchestratorService;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.api.VmOrdersRestApi;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.generated.vmware.ws.WorkflowToken;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;

@Component
@Path("/vm/operations")
@Transactional
public class VmOperationsRestService {

    private static final Logger logger = LoggerFactory.getLogger(VmOperationsRestService.class);

    @Inject
    private OrderRepository orderRepository;
    @Inject
    private OrchestratorService orchestratorService;

	public static String convertXmlToString(OrchestatorRequest request) {
		return XmlUtils.prettyFormat(XmlUtils.generateXml(request), 2);
	}

    @POST
    @Path("/decommission")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response decommission(@Context UriInfo uriInfo, String... hostnames) {
        checkDecommissionAccess(hostnames);
        Order order = Order.newDecommissionOrder(hostnames);
        orderRepository.save(order);
        logger.info("created new decommission order: " + order.getId());
        URI statuslogUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        URI decommissionUri = VmOrdersRestApi.apiDecommissionCallbackUri(uriInfo, order.getId());
        DecomissionRequest request = new DecomissionRequest(hostnames, decommissionUri, statuslogUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "decommissioning"));

        WorkflowToken workflowToken = orchestratorService.decommission(request);
        order.setExternalId(workflowToken.getId());
		order.setExternalRequest(convertXmlToString(request));
        orderRepository.save(order);

        HashMap<String, Long> result = Maps.newHashMap();
        result.put("orderId", order.getId());
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity(result).build();
    }

    private void checkDecommissionAccess(String... hostnames) {
        for (String hostname : hostnames) {
            EnvironmentClass environmentClass = findEnvionmentFromHostame(hostname);
            if (!User.getCurrentUser().hasAccess(environmentClass)) {
                throw new UnauthorizedException("User " + User.getCurrentUser().getName() + " does not have access to decommission node: " + hostname);
            }
        }
    }

    private EnvironmentClass findEnvionmentFromHostame(String hostname) {
        if (hostname.startsWith("a") || hostname.startsWith("c")) {
            return EnvironmentClass.p;
        }
        if (hostname.startsWith("b")) {
            return EnvironmentClass.q;
        }
        if (hostname.startsWith("d")) {
            return EnvironmentClass.t;
        }
        if (hostname.startsWith("e")) {
            return EnvironmentClass.u;
        }
        logger.info("Unknown hostnamepattern {} Expecting environmentClass p", hostname);
        return EnvironmentClass.p;
    }

    @POST
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stop(@Context UriInfo uriInfo, String... hostnames) {
        checkDecommissionAccess(hostnames);
        Order order = Order.newStopOrder(hostnames);
        orderRepository.save(order);
        // TODO
        URI statuslogUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        URI stopUri = VmOrdersRestApi.apiStopCallbackUri(uriInfo, order.getId());

        StopRequest request = new StopRequest(hostnames, stopUri, statuslogUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "stopping"));
        WorkflowToken workflowToken = orchestratorService.stop(request);
        order.setExternalId(workflowToken.getId());
		order.setExternalRequest(convertXmlToString(request));
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
        URI resultUri = VmOrdersRestApi.apiLogCallbackUri(uriInfo, order.getId());
        URI startUri = VmOrdersRestApi.apiStartCallbackUri(uriInfo, order.getId());

        StartRequest request = new StartRequest(hostnames, startUri, resultUri);
        order.addStatusLog(new OrderStatusLog("Basta", "Calling Orchestrator", "starting"));

        WorkflowToken workflowToken = orchestratorService.start(request);
        order.setExternalId(workflowToken.getId());
		order.setExternalRequest(convertXmlToString(request));
        orderRepository.save(order);

        HashMap<String, Long> result = Maps.newHashMap();
        result.put("orderId", order.getId());
        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId())).entity(result).build();
    }
}
