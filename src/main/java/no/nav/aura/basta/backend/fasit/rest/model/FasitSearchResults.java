package no.nav.aura.basta.backend.fasit.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FasitSearchResults {
    private List<SearchResultPayload> searchResults;

    @JsonCreator
    public FasitSearchResults(@JsonProperty("searchResults") List<SearchResultPayload> searchResults) {
        this.searchResults = searchResults == null ? new ArrayList<>() : searchResults;
    }

	public List<SearchResultPayload> getSearchResults() {
		return searchResults;
	}

	public FasitSearchResults filter(Predicate<SearchResultPayload> predicate) {
        List<SearchResultPayload> filteredSearchResults = searchResults.stream().filter(predicate).collect(Collectors.toList());
        return new FasitSearchResults(filteredSearchResults);
    }

    public  static FasitSearchResults emptySearchResult() {
        return new FasitSearchResults(new ArrayList<>());
    }
}
