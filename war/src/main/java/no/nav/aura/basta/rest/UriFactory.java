package no.nav.aura.basta.rest;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

public abstract class UriFactory {

    private UriFactory() {
    }

    public static URI createOrderUri(UriInfo uriInfo, String method, Long entity) {
        return uriInfo.getRequestUriBuilder().clone().path(OrdersRestService.class, method).build(entity);
    }

}
