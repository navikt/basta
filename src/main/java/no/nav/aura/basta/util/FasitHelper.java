package no.nav.aura.basta.util;

import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;

import static java.lang.System.getProperty;

public class FasitHelper {


    public static String getFasitLookupURL(String id) {
        try {
            return UriBuilder.fromUri(getProperty("fasit_rest_api_url"))
                    .replacePath("search")
                    .path(id)
                    .build()
                    .toURL()
                    .toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Illegal URL?", e);
        }
    }
}
