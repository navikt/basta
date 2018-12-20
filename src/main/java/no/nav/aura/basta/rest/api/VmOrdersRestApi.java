package no.nav.aura.basta.rest.api;

import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponse;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.vm.VmOperationsRestService;
import no.nav.aura.basta.rest.vm.VmOrderCallbackService;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDOList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Arrays;

@Component
@Path("/api/orders/vm")
@Transactional
public class VmOrdersRestApi {

    private static final Logger logger = LoggerFactory.getLogger(VmOrdersRestApi.class);


    @Inject
	private VmOrderCallbackService ordersService;

    @Inject
    private VmOperationsRestService operationsService;


    private static final String callbackHost = System.getenv("orchestrator_callback_host_url");

    @POST
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response createStopVmOrder(@Context UriInfo uriInfo, String... hostnames) {
        logger.info("Creating stop order for hostnames {}", Arrays.asList(hostnames));
        return operationsService.stop(uriInfo, hostnames);
    }

    @POST
    @Path("/remove")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response createDecommisionVmOrder(@Context UriInfo uriInfo, String... hostnames) {
        logger.info("Creating decommision order for hostnames {}", Arrays.asList(hostnames));
        return operationsService.decommission(uriInfo, hostnames);
    }

    @PUT
    @Path("{orderId}/decommission")
    @Consumes(MediaType.APPLICATION_XML)
    public void removeCallback(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm) {
        operationsService.deleteVmCallback(orderId, vm);
    }

    @PUT
    @Path("{orderId}/stop")
    @Consumes(MediaType.APPLICATION_XML)
    public void stopCallback(@PathParam("orderId") Long orderId, OperationResponse response) {
        operationsService.vmOperationCallback(orderId, response);
    }

    @PUT
    @Path("{orderId}/start")
    @Consumes(MediaType.APPLICATION_XML)
    public void startCallback(@PathParam("orderId") Long orderId, OperationResponse response) {
        operationsService.vmOperationCallback(orderId, response);
    }

    @PUT
    @Path("{orderId}/vm")
    @Consumes(MediaType.APPLICATION_XML)
    public void provisionCallback(@PathParam("orderId") Long orderId, OrchestratorNodeDOList vmList) {
		ordersService.createVmCallBack(orderId, vmList.getVms());
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{orderId}/statuslog")
    public void logCallback(@PathParam("orderId") Long orderId, OrderStatusLogDO orderStatusLogDO) {
        ordersService.updateStatuslog(orderId, orderStatusLogDO);
    }

    public static URI apiCreateCallbackUri(UriInfo uriInfo, Long entityId) {
        return generateUri(uriInfo, entityId, "provisionCallback");
    }

    public static URI apiStopCallbackUri(UriInfo uriInfo, Long entityId) {
        return generateUri(uriInfo, entityId, "stopCallback");
    }

    public static URI apiStartCallbackUri(UriInfo uriInfo, Long entityId) {
        return generateUri(uriInfo, entityId, "startCallback");
    }

    public static URI apiDecommissionCallbackUri(UriInfo uriInfo, Long entityId) {
        return generateUri(uriInfo, entityId, "removeCallback");
    }

    public static URI apiLogCallbackUri(UriInfo uriInfo, Long entityId) {
        return generateUri(uriInfo, entityId, "logCallback");
    }

    private static URI generateUri(UriInfo uriInfo, Long entityId, String methodName) {

        if(callbackHost != null && callbackHost != "" ) {
            URI uri = UriBuilder.fromPath(callbackHost).path(VmOrdersRestApi.class).path(VmOrdersRestApi.class, methodName).build(entityId);
            logger.info("Creating callback uri: " + uri.toString());
            return uri;
        }

        return  uriInfo.getBaseUriBuilder().clone().path(VmOrdersRestApi.class).path(VmOrdersRestApi.class,
                methodName).scheme("https").build(entityId);
    }
}
