package no.nav.aura.basta.backend;

import no.nav.aura.basta.backend.fasit.deprecated.payload.FasitSearchResults;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ScopePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.SearchResultPayload;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;
import static no.nav.aura.basta.backend.fasit.deprecated.payload.FasitSearchResults.emptySearchResult;
import static no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcesListPayload.emptyResourcesList;

public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    private final static Charset UTF8 = StandardCharsets.UTF_8;

    private String fasitResourcesUrl;
    private String fasitScopedResourceUrl;
    private String fasitEnvironmentsUrl;
    private String fasitNodesUrl;
    private String username;
    private final ResteasyClient client;

    public RestClient() {
        client = new ResteasyClientBuilder()
                .disableTrustManager()
                .connectionPoolSize(50)
                .establishConnectionTimeout(3, TimeUnit.SECONDS)
                .socketTimeout(3, TimeUnit.SECONDS)
                .connectionTTL(500, TimeUnit.MILLISECONDS)
                .build();
    }

    public RestClient(String username, String password) {
        this.username = username;
        client = new ResteasyClientBuilder()
                .disableTrustManager()
                .connectionPoolSize(50)
                .establishConnectionTimeout(3, TimeUnit.SECONDS)
                .socketTimeout(3, TimeUnit.SECONDS)
                .connectionTTL(500, TimeUnit.MILLISECONDS)
                .register(new BasicAuthentication(username, password))
                .build();
    }

    public RestClient(
            String fasitResourcesUrl,
            String fasitScopedUrl,
            String fasitApplicationInstancesUrl,
            String fasitEnvironmentsUrl,
            String fasitNodesUrl,
            String fasitUsername,
            String fasitPassword) {
        this(fasitUsername, fasitPassword);
        this.fasitResourcesUrl = fasitResourcesUrl;
        this.fasitScopedResourceUrl = fasitScopedUrl;
        this.fasitNodesUrl = fasitNodesUrl;
        this.fasitEnvironmentsUrl = fasitEnvironmentsUrl;

        log.info("Creating FasitRestClient with urls");
        log.info("Resources: " + fasitResourcesUrl);
        log.info("Scoped: " + fasitScopedUrl);
        log.info("AppInstances: " + fasitApplicationInstancesUrl);
        log.info("Nodes:" + fasitNodesUrl);
    }

    WebTarget createRequest(String url) {
        ResteasyWebTarget target = client.target(url);

        target.request().header("Content-Type", "application/json");
        target.request().header("Accept", "application/json");

        return target;
    }

    public ResourcePayload getScopedFasitResource(ResourceType type, String alias, ScopePayload scope ) {
        return findScopedFasitResource(type, alias, scope)
                .orElseThrow(() -> new NotFoundException("No matching resource found in fasit with alias " + alias + " for scope"));
    }

    public Optional<ResourcePayload> findScopedFasitResource(ResourceType type, String alias, ScopePayload scope ) {

        String scopedResourceApiUri = String.format(
                fasitScopedResourceUrl + "?type=%s&alias=%s&environment=%s&application=%s&zone=%s",
                type, alias, scope.environment, scope.application, scope.zone ) ;

        log.info("Finding scoped fasit resource: " + scopedResourceApiUri);

        return get(scopedResourceApiUri, ResourcePayload.class);
    }

    public Integer getNodeCountFor(String environment, String application) {
        String nodesApiUrl = String.format(fasitNodesUrl + "?environment=%s&application=%s", environment, application);
        return getCount(nodesApiUrl);
    }

    public Integer getCount(String url) {
        Response response = createRequest(url).request().get();
        checkResponseAndThrowExeption(response, url);

        String totalCount = response.getHeaderString("total_count");
        response.close();
        return Integer.valueOf(totalCount);
    }

    public <T> List<T> searchFasit(String searchQuery, String type, Class<T> returnType) {
        String fasitSearchUrl = "http://fasit/api/v1/search/";
        String fullSearchUrl = fasitSearchUrl + "?q=" + searchQuery;
        FasitSearchResults fasitSearchResults = getAs(fullSearchUrl, new GenericType<List<SearchResultPayload>>() {
        }).map(FasitSearchResults::new).orElse(emptySearchResult());

        return fasitSearchResults
                .getSearchResults()
                .stream()
                .filter(result -> result.type.equals(type))
                .map(searchResult -> get(searchResult.link, returnType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    public ResourcesListPayload findFasitResources(ResourceType type, String alias, ScopePayload searchScope) {
        StringBuilder resourceApiUri = new StringBuilder().append(fasitResourcesUrl).append("?type=").append(type).append("&environmentclass=").append(searchScope.environmentclass);
        ofNullable(alias).ifPresent(a -> resourceApiUri.append("&alias=").append(a));
        ofNullable(searchScope.environment).ifPresent(env -> resourceApiUri.append("&environment=").append(env));
        ofNullable(searchScope.application).ifPresent(app -> resourceApiUri.append("&application=").append(app));
        ofNullable(searchScope.zone).ifPresent(zone -> resourceApiUri.append("&zone=").append(zone));
        log.info("Finding fasit resources with query: {}", resourceApiUri.toString());

        return getAs(resourceApiUri.toString(), new GenericType<List<ResourcePayload>>(){})
                .map(ResourcesListPayload::new).orElse(emptyResourcesList());
    }

    public String getFasitSecret(String url) {
        Response response = client.target(url).request().get();
        checkResponseAndThrowExeption(response, url);

        String secret = response.readEntity(String.class);
        response.close();

        return secret;
    }

    private <T> Optional<T> getAs(String url, GenericType<T> returnType) {
        try {
            Response response = createRequest(url).request().get();
            checkResponseAndThrowExeption(response, url);
            T result = response.readEntity(returnType);
            response.close();

            return of(result);

        } catch (NotFoundException nfe) {
            return empty();
        }
    }

    public <T> Optional<T> get(String url, Class<T> returnType) {
        try {
            Response response = createRequest(url).request().get();
            checkResponseAndThrowExeption(response, url);
            T result = response.readEntity(returnType);
            response.close();

            return of(result);

        } catch (NotFoundException nfe) {
            return empty();
        }
    }

    public Response delete(String url) {
        try {
            log.debug("DELETE {}", url);
            Response response = createRequest(url).request().delete();

            if (response.getStatus() != 404) {
                checkResponseAndThrowExeption(response, url);
            }

            response.close();
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Response deleteFasitResource(String url, String onBehalfOfUser, String comment ) {
        try {
            log.debug("DELETE {}", url);

            Response response = createRequest(url)
                    .request()
                    .header("x-onbehalfof", onBehalfOfUser)
                    .header("x-comment", comment)
                    .delete();

            if (response.getStatus() != 404) {
                checkResponseAndThrowExeption(response, url);
            }

            response.close();
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void checkResponseAndThrowExeption(Response response, String requestUrl) {
        int status = response.getStatus();
        if (status == 403) {
            response.close();
            throw new ForbiddenException("Access forbidden to " + requestUrl);
        }
        if (status == 401) {
            response.close();
            throw new NotAuthorizedException("Unauthorized access to " + requestUrl);
        }

        if (status == 404) {
            response.close();
            throw new NotFoundException("Not found " + requestUrl);
        }

        if (status >= 400) {
            String entity = null;
            try {
                entity = response.readEntity(String.class);
            } catch (Exception e) {
                log.error("Unable to get fault reason", e);
            }
            response.close();
            throw new WebApplicationException("Error calling " + requestUrl + entity, status);
        }
    }

    public Optional<String>     createFasitResource(String url, String payload, String onBehalfOfUser, String comment) {
            try {
                log.debug("POST {} as {}, payload: {} with user {}", url, onBehalfOfUser, payload, username);

                Response response = createRequest(url)
                        .request()
                        .header("x-onbehalfof", onBehalfOfUser)
                        .header("x-comment", comment)
                        .post(Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));

                checkResponseAndThrowExeption(response, url);
                Optional<String> createdResourceId = getIdFromLocationHeader(response);
                response.close();

                return createdResourceId;
            } catch (Exception e) {

                throw new RuntimeException("Error trying to POST payload " + payload + " to url " + url, e);
            }
    }

    private Optional<String> getIdFromLocationHeader(Response response) {
        List<Object> location = response.getHeaders().get("Location");

        if (location != null && !location.isEmpty()) {
            String locationUrl = location.get(0).toString();
            String[] parts = locationUrl.split("/");
            String id = parts[parts.length - 1];
            return of(id);
        }
        return empty();
    }

    public Response post(String url, String payload) {
        try {
            log.debug("POST {}, payload: {} with user {}", url, payload, username);

            Response response = createRequest(url).request().post(Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));
            checkResponseAndThrowExeption(response, url);
            response.close();

            return response;
        } catch (Exception e) {

            throw new RuntimeException("Error trying to POST payload " + payload + " to url " + url, e);
        }
    }

    public Optional<String> updateFasitResource(String url, String payload, String onBehalfOfUser, String comment) {
        try {
            log.debug("PUT {}, payload: {}", url, payload);
            Response response = createRequest(url)
                    .request()
                    .header("x-onbehalfof", onBehalfOfUser)
                    .header("x-comment", comment)
                    .put(Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));
            checkResponseAndThrowExeption(response, url);
            Optional<String> resourceId = getIdFromLocationHeader(response);
            response.close();

            return resourceId;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Response put(String url, String payload) {
        try {
            log.debug("PUT {}, payload: {}", url, payload);
            Response response = createRequest(url).request().put(Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));
            checkResponseAndThrowExeption(response, url);
            response.close();

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void patch(String url, String payload) {
        try {
            log.debug("PATCH {}, payload: {}", url, payload);
            Response response = createRequest(url).request().method("PATCH", Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));
            checkResponseAndThrowExeption(response, url);
            response.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
