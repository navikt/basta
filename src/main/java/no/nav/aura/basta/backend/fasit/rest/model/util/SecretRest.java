package no.nav.aura.basta.backend.fasit.rest.model.util;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

public class SecretRest {
    public static URI secretUri(URI baseUri, long id) {
        return UriComponentsBuilder.fromUri(baseUri)
                .path("/api/v2/secrets/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
