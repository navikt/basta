package no.nav.aura.basta.rest;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

public class RestServiceTestUtils {



	public static UriInfo createUriInfo() {
        try {
            UriInfo uriInfo = mock(UriInfo.class);
            when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(new URI("http://unittest:666/")));
            return uriInfo;
        } catch (IllegalArgumentException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Long getOrderIdFromMetadata(Response response) {
        URI uri = (URI)response.getMetadata().get("Location").get(0);
        String[] split = uri.getPath().split("/");
        return Long.valueOf(split[split.length - 1]);
    }


}
