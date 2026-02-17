package no.nav.aura.basta.util;

import static java.util.Optional.ofNullable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FasitHelper {

    private static String fasitBaseUrl;

    @Value("${fasit_base_url}")
    public void setFasitBaseUrl(String url) {
        FasitHelper.fasitBaseUrl = url;
    }

    public static String getFasitLookupURL(String id) {
        try {
            String pathSegment = ofNullable(id).orElse("");
            
            return UriComponentsBuilder.fromUriString(fasitBaseUrl)
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
