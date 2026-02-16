package no.nav.aura.basta.util;

import org.springframework.web.util.UriComponentsBuilder;

import static java.lang.System.getProperty;
import static java.util.Optional.*;

public class FasitHelper {


    public static String getFasitLookupURL(String id) {
        try {
            String baseUrl = getProperty("fasit_base_url");
            String pathSegment = ofNullable(id).orElse("");
            
            return UriComponentsBuilder.fromUriString(baseUrl)
                    .replacePath("/search")
                    .path("/")
                    .path(pathSegment)
                    .build()
                    .toUriString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Illegal URL?", e);
        }
    }
}
