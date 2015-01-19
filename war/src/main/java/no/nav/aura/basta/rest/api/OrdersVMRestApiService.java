package no.nav.aura.basta.rest.api;

import no.nav.aura.basta.rest.OrchestratorNodeDO;
import no.nav.aura.basta.rest.OrderStatusLogDO;
import no.nav.aura.basta.rest.OrdersRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

@SuppressWarnings("serial")
@Component
@Path("/api/orders/vm")
@Transactional
public class OrdersVMRestApiService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersVMRestApiService.class);

    @Inject
    private OrdersRestService ordersRestService;


    @PUT
    @Path("{orderId}/decommission")
    @Consumes(MediaType.APPLICATION_XML)
    public void remove(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        ordersRestService.removeVmInformation(orderId, vm, request);
    }

    @PUT
    @Path("{orderId}/stop")
    @Consumes(MediaType.APPLICATION_XML)
    public void stop(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        ordersRestService.stopVmInformation(orderId, vm, request);
    }

    @PUT
    @Path("{orderId}/start")
    @Consumes(MediaType.APPLICATION_XML)
    public void start(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        ordersRestService.startVmInformation(orderId, vm, request);
    }

    @PUT
    @Path("{orderId}/vm/single")
    @Consumes(MediaType.APPLICATION_XML)
    public void add(@PathParam("orderId") Long orderId, OrchestratorNodeDO vm, @Context HttpServletRequest request) {
        ordersRestService.putVmInformation(orderId, vm, request);
    }

    @PUT
    @Path("{orderId}/vm")
    @Consumes(MediaType.APPLICATION_XML)
    public void addList(@PathParam("orderId") Long orderId, List<OrchestratorNodeDO> vm, @Context HttpServletRequest request) {
        ordersRestService.putVmInformationAsList(orderId, vm, request);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{orderId}/statuslog")
    public void log(@PathParam("orderId") Long orderId, OrderStatusLogDO orderStatusLogDO, @Context HttpServletRequest request) {
        ordersRestService.updateStatuslog(orderId, orderStatusLogDO, request);
    }
}
