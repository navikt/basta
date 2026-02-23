package no.nav.aura.basta.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.networknt.org.apache.commons.validator.routines.DomainValidator;

import no.nav.aura.basta.backend.fasit.rest.model.ApplicationListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ApplicationPayload;
import no.nav.aura.basta.backend.fasit.rest.model.EnvironmentListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.EnvironmentPayload;
import no.nav.aura.basta.backend.fasit.rest.model.FasitSearchResults;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SearchResultPayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;
import static no.nav.aura.basta.backend.fasit.rest.model.FasitSearchResults.emptySearchResult;
import static no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload.emptyResourcesList;
import static no.nav.aura.basta.backend.fasit.rest.model.ApplicationListPayload.emptyApplicationList;
import static no.nav.aura.basta.backend.fasit.rest.model.EnvironmentListPayload.emptyEnvironmentList;

@Component
public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);

    private String fasitBaseUrl;
    
    private String username;
    

    private final RestTemplate restTemplate;
    private DomainValidator validator = DomainValidator.getInstance();

    public RestClient() {
		this.restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
	}
  
    public RestClient(String username, String password) {
        this.username = username;
        this.restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
        
        // Configure basic authentication
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, authHeader);
            return execution.execute(request, body);
        });
    }

    public RestClient(
            @Value("${fasit_base_url}") String fasitBaseUrl,
            @Value("${srvfasit_username}") String fasitUsername,
            @Value("${srvfasit_password}") String fasitPassword) {
        this(fasitUsername, fasitPassword);
        this.fasitBaseUrl = fasitBaseUrl;
        log.info("Creating FasitRestClient with urls");
    }

    // Helper method to create HTTP headers
    private HttpHeaders createHeaders(String onBehalfOfUser, String comment) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (onBehalfOfUser != null) {
            headers.set("x-onbehalfof", onBehalfOfUser);
        }
        if (comment != null) {
            headers.set("x-comment", comment);
        }
        return headers;
    }

    private HttpHeaders createHeaders() {
        return createHeaders(null, null);
    }

    public ResourcePayload getScopedFasitResource(ResourceType type, String alias, ScopePayload scope ) {
        return findScopedFasitResource(type, alias, scope)
                .orElseThrow(() -> new IllegalArgumentException("No matching resource found in fasit with alias " + alias + " for scope"));
    }

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

    public Integer getCount(String url) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            checkResponseAndThrowException(response, url);
            
            List<String> totalCountHeader = response.getHeaders().get("total_count");
            if (totalCountHeader != null && !totalCountHeader.isEmpty()) {
                return Integer.valueOf(totalCountHeader.get(0));
            }
            throw new RuntimeException("Missing total_count header from " + url);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error getting count from " + url, e);
        }
    }

    public <T> List<T> searchFasit(String searchQuery, String type, Class<T> returnType) {
        String fullSearchUrl = String.format("%s/api/v1/search/?q=%s", fasitBaseUrl, searchQuery);
        FasitSearchResults fasitSearchResults = getAs(fullSearchUrl, new ParameterizedTypeReference<List<SearchResultPayload>>() {
        }).map(FasitSearchResults::new).orElse(emptySearchResult());

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

        return getAs(resourceApiUri.toString(), new ParameterizedTypeReference<List<ResourcePayload>>(){})
                .map(ResourcesListPayload::new).orElse(emptyResourcesList());
    }
    
    public boolean existsInFasit(ResourceType type, String alias, ScopePayload searchScope) {
        ResourcesListPayload resources = findFasitResources(type, alias, searchScope);
        return !resources.isEmpty();
    }

    public String getFasitSecret(String url) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            checkResponseAndThrowException(response, url);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Not found: " + url, e);
        }
    }

    private <T> Optional<T> getAs(String url, ParameterizedTypeReference<T> returnType) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, returnType);
            checkResponseAndThrowException(response, url);
            return of(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return empty();
        }
    }

    public <T> Optional<T> get(String url, Class<T> returnType) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, returnType);
            checkResponseAndThrowException(response, url);
            return of(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return empty();
        }
    }

    public ResponseEntity<String> delete(String url) {
        try {
            log.debug("DELETE {}", url);
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

            if (response.getStatusCode() != HttpStatus.NOT_FOUND) {
                checkResponseAndThrowException(response, url);
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<String> deleteFasitResource(String url, String onBehalfOfUser, String comment ) {
        try {
            log.debug("DELETE {}", url);
            HttpHeaders headers = createHeaders(onBehalfOfUser, comment);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

            if (response.getStatusCode() != HttpStatus.NOT_FOUND) {
                checkResponseAndThrowException(response, url);
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void checkResponseAndThrowException(ResponseEntity<?> response, String requestUrl) {
        HttpStatusCode status = response.getStatusCode();
        if (status.value() == 403) {
            throw new SecurityException("Access forbidden to " + requestUrl);
        }
        if (status.value() == 401) {
            throw new SecurityException("Unauthorized access to " + requestUrl);
        }
        if (status.value() == 404) {
            throw new IllegalArgumentException("Not found " + requestUrl);
        }
        if (status.is4xxClientError() || status.is5xxServerError()) {
            String entity = null;
            try {
                entity = response.hasBody() ? response.getBody().toString() : null;
            } catch (Exception e) {
                log.error("Unable to get fault reason", e);
            }
            throw new RuntimeException("Error calling " + requestUrl + " " + entity + ", status: " + status.value());
        }
    }

    /* 
     * Creates an entry in fasit, resources, nodes etc. 
     */
    public Optional<String> createFasitResource(String url, String payload, String onBehalfOfUser, String comment) {
        try {
            log.debug("POST {} as {}, payload: {} with user {}", url, onBehalfOfUser, payload, username);
            HttpHeaders headers = createHeaders(onBehalfOfUser, comment);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            checkResponseAndThrowException(response, url);
            return getIdFromLocationHeader(response);
        } catch (Exception e) {
            throw new RuntimeException("Error trying to POST payload " + payload + " to url " + url, e);
        }
    }
    
    private Optional<String> getIdFromLocationHeader(ResponseEntity<?> response) {
        List<String> location = response.getHeaders().get("Location");
        if (location != null && !location.isEmpty()) {
            String locationUrl = location.get(0).toString();
            String[] parts = locationUrl.split("/");
            String id = parts[parts.length - 1];
            return of(id);
        }
        return empty();
    }

    public ResponseEntity<String> post(String url, String payload) {
        try {
            log.debug("POST {}, payload: {} with user {}", url, payload, username);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            checkResponseAndThrowException(response, url);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error trying to POST payload " + payload + " to url " + url, e);
        }
    }

    public ResourcePayload updateFasitResourceAndReturnResourcePayload(String url, String payload, String onBehalfOfUser, String comment) {
        try {
            log.debug("PUT {}, payload: {}", url, payload);
            HttpHeaders headers = createHeaders(onBehalfOfUser, comment);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<ResourcePayload> response = restTemplate.exchange(url, HttpMethod.PUT, entity, ResourcePayload.class);
            checkResponseAndThrowException(response, url);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Optional<String> updateFasitResource(String url, String payload, String onBehalfOfUser, String comment) {
        try {
            log.debug("PUT {}, payload: {}", url, payload);
            HttpHeaders headers = createHeaders(onBehalfOfUser, comment);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            checkResponseAndThrowException(response, url);
            return getIdFromLocationHeader(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<String> put(String url, String payload) {
        try {
            log.debug("PUT {}, payload: {}", url, payload);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            checkResponseAndThrowException(response, url);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void patch(String url, String payload) {
        try {
            log.debug("PATCH {}, payload: {}", url, payload);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
            checkResponseAndThrowException(response, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        return getAs(applicationApiUri.toString(), new ParameterizedTypeReference<List<ApplicationPayload>>(){})
                .map(ApplicationListPayload::new).orElse(emptyApplicationList());
    }

    public EnvironmentPayload getEnvironmentByName(String environmentName) {
        String applicationApiUri = String.format(fasitBaseUrl + "/api/v2/environments/%s", environmentName);
        log.info("Getting fasit environment by name: " + applicationApiUri);
        return get(applicationApiUri, EnvironmentPayload.class)
                .orElseThrow(() -> new IllegalArgumentException("No matching environment found in fasit with name " + environmentName));
    }
    
    public EnvironmentListPayload getAllEnvironments() {
        String environmentApiUri = String.format(fasitBaseUrl + "/api/v2/environments");
        log.info("Getting fasit environments: " + environmentApiUri);
        return getAs(environmentApiUri.toString(), new ParameterizedTypeReference<List<EnvironmentPayload>>(){})
                .map(EnvironmentListPayload::new).orElse(emptyEnvironmentList());
    }

    // This error handler prevents RestTemplate from automatically throwing exceptions
    private static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
            return false;
        }
    }
}