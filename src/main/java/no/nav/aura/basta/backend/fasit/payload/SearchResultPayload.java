package no.nav.aura.basta.backend.fasit.payload;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class SearchResultPayload {
    public  String link;
    public String type;

    public SearchResultPayload() {
    }
}
