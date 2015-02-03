package no.nav.aura.basta;

import no.nav.aura.basta.rest.OrdersRestService;
import no.nav.aura.basta.rest.api.OrdersVMRestApiService;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

public abstract class UriFactory {

    private UriFactory() {
    }

    public static URI createOrderApiUri(UriInfo uriInfo, String methodName, Long entityId) {
        return createUri(uriInfo, OrdersVMRestApiService.class, methodName, entityId);
    }

    public static URI createOrderUri(UriInfo uriInfo, String methodName, Long entityId) {
        return createUri(uriInfo, OrdersRestService.class, methodName, entityId);
    }

    private static URI createUri(UriInfo uriInfo, Class<?> resourceClass, String methodName, Long entityId) {
        return uriInfo.getBaseUriBuilder().clone().path(resourceClass).path(resourceClass, methodName).build(entityId);
    }



}
