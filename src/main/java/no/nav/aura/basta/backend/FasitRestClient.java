package no.nav.aura.basta.backend;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static no.nav.aura.basta.backend.fasit.rest.model.ApplicationListPayload.emptyApplicationList;
import static no.nav.aura.basta.backend.fasit.rest.model.FasitSearchResults.emptySearchResult;
import static no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload.emptyResourcesList;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import no.nav.aura.basta.backend.fasit.rest.model.ApplicationListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ApplicationPayload;
import no.nav.aura.basta.backend.fasit.rest.model.EnvironmentPayload;
import no.nav.aura.basta.backend.fasit.rest.model.FasitSearchResults;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SearchResultPayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;

@Component
public class FasitRestClient extends RestClient {
	private static final Logger log = LoggerFactory.getLogger(FasitRestClient.class);
//	private RestClient restClient;
	
    private String fasitBaseUrl;

    
	public FasitRestClient(
			@Value("${fasit_base_url}") String fasitBaseUrl,
            @Value("${srvbasta_username}") String fasitUsername,
            @Value("${srvbasta_password}") String fasitPassword) {
		super(fasitUsername, fasitPassword);
		this.fasitBaseUrl = fasitBaseUrl;
//    	this.username = fasitUsername;
//		this.restClient = new RestClient(fasitUsername, fasitPassword);
	}
	
//	public FasitRestClient(String username, String password) {
//		super(username, password);
//	}
	
    public Optional<ResourcePayload> findScopedFasitResource(ResourceType type, String alias, ScopePayload scope ) {
        String scopedResourceApiUri = String.format(
                fasitBaseUrl + "/api/v2/scopedresource?type=%s&alias=%s&environment=%s&application=%s&zone=%s",
                type, alias, scope.environment, scope.application, scope.zone ) ;
        log.info("Finding scoped fasit resource: " + scopedResourceApiUri);
        return get(scopedResourceApiUri, ResourcePayload.class);
    }

    public Optional<ResourcePayload> getFasitResourceById(long id) {
        String resourceApiUri = fasitBaseUrl + "/api/v2/resources/" + id;
        log.info("Getting fasit resource by id: " + resourceApiUri);
        return get(resourceApiUri, ResourcePayload.class);
    }

    public Integer getNodeCountFor(String environment, String application) {
        String nodesApiUrl = String.format(fasitBaseUrl + "/api/v2/nodes?environment=%s&application=%s", environment, application);
        return getCount(nodesApiUrl);
    }

    public ResourcePayload getScopedFasitResource(ResourceType type, String alias, ScopePayload scope ) {
        return findScopedFasitResource(type, alias, scope)
                .orElseThrow(() -> new IllegalArgumentException("No matching resource found in fasit with alias " + alias + " for scope"));
    }

    public <T> List<T> searchFasit(String searchQuery, String type, Class<T> returnType) {
        String fullSearchUrl = String.format("%s/api/v1/search?q=%s", fasitBaseUrl, searchQuery);
        log.info("Searching fasit with query: " + fullSearchUrl);
//        FasitSearchResults fasitSearchResults = getAs(fullSearchUrl, new ParameterizedTypeReference<List<SearchResultPayload>>() {
//        	}).map(FasitSearchResults::new).orElse(emptySearchResult());
        FasitSearchResults fasitSearchResults = get(fullSearchUrl, FasitSearchResults.class).orElse(emptySearchResult());

        return fasitSearchResults.getSearchResults()
                .stream()
                .filter(result -> result.type.equals(type))
                .map(searchResult -> get(searchResult.link.toString(), returnType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    public ResourcesListPayload findFasitResources(ResourceType type, String alias, ScopePayload searchScope) {
        StringBuilder resourceApiUri = new StringBuilder().append(fasitBaseUrl).append("/api/v2/resources").append("?type=").append(type).append("&environmentclass=").append(searchScope.environmentclass);
        
        ofNullable(alias).ifPresent(a -> resourceApiUri.append("&alias=").append(a));
        ofNullable(searchScope.environment).ifPresent(env -> resourceApiUri.append("&environment=").append(env));
        ofNullable(searchScope.application).ifPresent(app -> resourceApiUri.append("&application=").append(app));
        ofNullable(searchScope.zone).ifPresent(zone -> resourceApiUri.append("&zone=").append(zone));
        log.info("Finding fasit resources with query: {}", resourceApiUri.toString());

//        return getAs(resourceApiUri.toString(), new ParameterizedTypeReference<List<ResourcePayload>>(){})
//                .map(ResourcesListPayload::new).orElse(emptyResourcesList());
        return get(resourceApiUri.toString(), ResourcesListPayload.class).orElse(emptyResourcesList());
    }
    
    public boolean existsInFasit(ResourceType type, String alias, ScopePayload searchScope) {
        ResourcesListPayload resources = findFasitResources(type, alias, searchScope);
        return !resources.isEmpty();
    }
    
    public ApplicationPayload getApplicationByName(String applicationName) {
        String applicationApiUri = String.format(fasitBaseUrl + "/api/v2/applications/%s", applicationName);
        log.info("Getting fasit application by name: " + applicationApiUri);
        return get(applicationApiUri, ApplicationPayload.class)
                .orElseThrow(() -> new IllegalArgumentException("No matching application found in fasit with name " + applicationName));
    }
    
    public ApplicationListPayload getAllApplications() {
        String applicationApiUri = String.format(fasitBaseUrl + "/api/v2/applications");
        log.info("Getting fasit application by name: " + applicationApiUri);
//        return getAs(applicationApiUri.toString(), new ParameterizedTypeReference<List<ApplicationPayload>>(){})
//                .map(ApplicationListPayload::new).orElse(emptyApplicationList());
        return get(applicationApiUri.toString(), ApplicationListPayload.class).orElse(emptyApplicationList());

    }

    public EnvironmentPayload getEnvironmentByName(String environmentName) {
        String applicationApiUri = String.format(fasitBaseUrl + "/api/v2/environments/%s", environmentName);
        log.info("Getting fasit environment by name: " + applicationApiUri);
        return get(applicationApiUri, EnvironmentPayload.class)
                .orElseThrow(() -> new IllegalArgumentException("No matching environment found in fasit with name " + environmentName));
    }
    
    public List<EnvironmentPayload> getAllEnvironments() {
        String environmentApiUri = String.format(fasitBaseUrl + "/api/v2/environments");
        log.info("Getting fasit environments: " + environmentApiUri);
        return get(environmentApiUri, EnvironmentPayload[].class)
                .map(List::of)
                .orElse(List.of());
    }
}
