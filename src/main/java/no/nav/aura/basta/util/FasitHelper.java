package no.nav.aura.basta.util;

import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;

import static java.lang.System.getProperty;
import static java.util.Optional.*;

public class FasitHelper {


    public static String getFasitLookupURL(String id) {
        try {
            return UriBuilder.fromUri(getProperty("fasit_search_v1_url"))
                   // .replacePath("search")
                    .path(ofNullable(id).orElse(""))
                    .build()
                    .toURL()
                    .toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Illegal URL?", e);
        }
    }
}
