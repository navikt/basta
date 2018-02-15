package no.nav.aura.basta.rest;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import no.nav.aura.basta.domain.SystemNotification;
import no.nav.aura.basta.repository.SystemNotificationRepository;
import no.nav.aura.basta.rest.dataobjects.SystemNotificationDO;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.springframework.stereotype.Component;

@Component
@Path("/system")
public class SystemRestService {

    @Inject
    SystemNotificationRepository systemNotificationRepository;


    @GET
    @Path("notifications/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getNotification(@PathParam("id") Long id ) {
        return Response.ok(SystemNotificationDO.from(systemNotificationRepository.findOne(id))).build();
    }

    @PUT
    @Path("notifications/{id}/inactive")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response setInactive(@PathParam("id") Long id ) {
        SystemNotification notification = systemNotificationRepository.findOne(id);
        notification.setInactive();
        systemNotificationRepository.save(notification);
        return Response.noContent().build();
    }


    @GET
    @Path("notifications")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public List<SystemNotificationDO> getSystemNotifications() {
        return StreamSupport.stream(systemNotificationRepository.findAll().spliterator(), false)
                .map(notification -> SystemNotificationDO.from(notification))
                .collect(Collectors.toList());

    }

    @GET
    @Path("notifications/active")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public List<SystemNotificationDO> getActiveSystemNotifications() {
        return StreamSupport.stream(systemNotificationRepository.findByActiveTrue().spliterator(), false)
                .map(notification -> SystemNotificationDO.from(notification))
                .collect(Collectors.toList());
    }


    @POST
    @Path("notifications/create/info")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response addSystemNotification(String message) {
        SystemNotification save = systemNotificationRepository.save(SystemNotification.newSystemNotification(message));
        return Response.created(URI.create("system/notifications/" + save.getId())).build();
    }

    @POST
    @Path("notifications/create/blocking")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response addBlockingSystemNotification(String message) {
        SystemNotification save = systemNotificationRepository.save(SystemNotification.newBlockingSystemNotification(message));
        return Response.created(URI.create("system/notifications/" + save.getId())).build();
    }














}
