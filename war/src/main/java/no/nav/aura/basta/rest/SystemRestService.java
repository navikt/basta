package no.nav.aura.basta.rest;

import com.google.common.collect.FluentIterable;
import no.nav.aura.basta.domain.SystemNotification;
import no.nav.aura.basta.repository.SystemNotificationRepository;
import no.nav.aura.basta.util.SerializableFunction;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

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
        return Response.ok(SystemNotificationDO.fromDomain(systemNotificationRepository.findOne(id))).build();
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
        Iterable<SystemNotification> all = systemNotificationRepository.findAll();
        return FluentIterable.from(all).transform(new SerializableFunction<SystemNotification, SystemNotificationDO>() {
            @Override
            public SystemNotificationDO process(SystemNotification input) {
                return SystemNotificationDO.fromDomain(input);
            }
        }).toList();
    }

    @GET
    @Path("notifications/active")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public List<SystemNotificationDO> getActiveSystemNotifications() {
        Iterable<SystemNotification> all = systemNotificationRepository.findByActiveTrue();
        return FluentIterable.from(all).transform(new SerializableFunction<SystemNotification, SystemNotificationDO>() {
            @Override
            public SystemNotificationDO process(SystemNotification input) {
                return SystemNotificationDO.fromDomain(input);
            }
        }).toList();
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
