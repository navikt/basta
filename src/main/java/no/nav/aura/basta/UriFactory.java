package no.nav.aura.basta;

import no.nav.aura.basta.rest.OrdersListRestService;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

public abstract class UriFactory {

    private UriFactory() {
    }

    public static URI createOrderUri(UriInfo uriInfo, String methodName, Long entityId) {
        return createUri(uriInfo, OrdersListRestService.class, methodName, entityId);
    }

    private static URI createUri(UriInfo uriInfo, Class<?> resourceClass, String methodName, Long entityId) {
        URI uri = uriInfo.getBaseUriBuilder().clone().path(resourceClass).path(resourceClass, methodName).scheme
                ("https").build(entityId);
        return uri;
    }

	public static URI getOrderUri(UriInfo uriInfo, Long entityId) {
		return createUri(uriInfo, OrdersListRestService.class, "getOrder", entityId);
	}

}
