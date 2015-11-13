package no.nav.aura.basta.util;

import static java.lang.System.getProperty;

import java.net.MalformedURLException;

import javax.ws.rs.core.UriBuilder;

import com.google.common.base.Optional;

public class FasitHelper {

    public static String getFasitLookupURL(String id, String name, String entityType) {
        try {
            return UriBuilder.fromUri(getProperty("fasit.rest.api.url"))
                    .replacePath("lookup")
                    .queryParam("type", entityType)
                    .queryParam("id", Optional.fromNullable(id).or(""))
                    .queryParam("name", name)
                    .build()
                    .toURL()
                    .toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Illegal URL?", e);
        }
    }
}
