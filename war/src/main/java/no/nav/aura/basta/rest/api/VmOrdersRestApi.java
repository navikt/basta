package no.nav.aura.basta.rest.api;

import java.net.URI;
import java.util.Arrays;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.rest.OrdersRestService;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.vm.NodesRestService;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/api/orders/vm")
@Transactional
public class VmOrdersRestApi {

    private static final Logger logger = LoggerFactory.getLogger(VmOrdersRestApi.class);

    @Inject
    private OrdersRestService ordersRestService;

    @Inject
    private NodesRestService nodesRestService;

    @POST
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response createStopVmOrder(@Context UriInfo uriInfo, String... hostnames) {
        logger.info("Creating stoporder for hostnames {}", Arrays.asList(hostnames));
        return nodesRestService.stop(uriInfo, hostnames);
    }

    @POST
    @Path("/remove")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response createDecommisionVmOrder(@Context UriInfo uriInfo, String... hostnames) {
        logger.info("Creating decommisionorder for hostnames {}", Arrays.asList(hostnames));
        return nodesRestService.decommission(uriInfo, hostnames);
    }

    @PUT
    @Path("{orderId}/decommission")
    @Consumes(MediaType.APPLICATION_XML)
    public void removeCallback(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        ordersRestService.removeVmInformation(orderId, vm, request);
    }

    @PUT
    @Path("{orderId}/stop")
    @Consumes(MediaType.APPLICATION_XML)
    public void stopCallback(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        ordersRestService.stopVmInformation(orderId, vm, request);
    }

    @PUT
    @Path("{orderId}/start")
    @Consumes(MediaType.APPLICATION_XML)
    public void startCallback(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        ordersRestService.startVmInformation(orderId, vm, request);
    }

    @PUT
    @Path("{orderId}/vm")
    @Consumes(MediaType.APPLICATION_XML)
    public void createCallback(@PathParam("orderId") Long orderId, OrchestratorNodeDOList vmList, @Context HttpServletRequest request) {
        ordersRestService.putVmInformationAsList(orderId, vmList.getVms(), request);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{orderId}/statuslog")
    public void logCallback(@PathParam("orderId") Long orderId, OrderStatusLogDO orderStatusLogDO, @Context HttpServletRequest request) {
        ordersRestService.updateStatuslog(orderId, orderStatusLogDO, request);
    }

    public static URI apiCreateCallbackUri(UriInfo uriInfo, Long entityId) {
        return uriInfo.getBaseUriBuilder().clone().path(VmOrdersRestApi.class).path(VmOrdersRestApi.class, "createCallback").build(entityId);
    }

    public static URI apiStopCallbackUri(UriInfo uriInfo, Long entityId) {
        return uriInfo.getBaseUriBuilder().clone().path(VmOrdersRestApi.class).path(VmOrdersRestApi.class, "stopCallback").build(entityId);
    }

    public static URI apiStartCallbackUri(UriInfo uriInfo, Long entityId) {
        return uriInfo.getBaseUriBuilder().clone().path(VmOrdersRestApi.class).path(VmOrdersRestApi.class, "startCallback").build(entityId);
    }

    public static URI apiDecommissionCallbackUri(UriInfo uriInfo, Long entityId) {
        return uriInfo.getBaseUriBuilder().clone().path(VmOrdersRestApi.class).path(VmOrdersRestApi.class, "removeCallback").build(entityId);
    }

    public static URI apiLogCallbackUri(UriInfo uriInfo, Long entityId) {
        return uriInfo.getBaseUriBuilder().clone().path(VmOrdersRestApi.class).path(VmOrdersRestApi.class, "logCallback").build(entityId);
    }
}
