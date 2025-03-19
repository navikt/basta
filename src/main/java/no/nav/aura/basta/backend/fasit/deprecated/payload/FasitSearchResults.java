package no.nav.aura.basta.backend.fasit.deprecated.payload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FasitSearchResults {
    private List<SearchResultPayload> searchResults;

    public FasitSearchResults(List<SearchResultPayload> searchResults ){
        this.searchResults = searchResults;
    }

   public List<SearchResultPayload> getSearchResults() {
        return searchResults;
    }

    public FasitSearchResults filter(Predicate<SearchResultPayload> predicate) {
        List<SearchResultPayload> filteredSearchResults = searchResults.stream().filter(predicate).collect(Collectors.toList());
        return new FasitSearchResults(filteredSearchResults);
    }

    public boolean isEmpty() {
        return searchResults == null || searchResults.isEmpty();
    }

    public  static FasitSearchResults emptySearchResult() {
        return new FasitSearchResults(new ArrayList<>());
    }
}
