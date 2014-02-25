package no.nav.aura.basta.rest;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

public abstract class UriFactory {

    private UriFactory() {
    }

    public static URI createOrderUri(UriInfo uriInfo, String methodName, Long entityId) {
        return createUri(uriInfo, OrdersRestService.class, methodName, entityId);
    }

    private static URI createUri(UriInfo uriInfo, Class<OrdersRestService> resourceClass, String methodName, Long entityId) {
        return uriInfo.getBaseUriBuilder().clone().path(resourceClass).path(resourceClass, methodName).build(entityId);
    }

}
