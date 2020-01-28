package no.nav.aura.basta.backend;

import no.nav.aura.basta.backend.fasit.payload.*;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.*;
import static no.nav.aura.basta.backend.fasit.payload.ResourcesListPayload.*;

public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    private final static Charset UTF8 = Charset.forName("UTF-8");

    @Value("${fasit_resources_v2_url}")
    private String fasitResourcesUrl;

    @Value("${fasit_scopedresource_v2_url}")
    private String fasitScopedResourceUrl;

    @Value("${fasit_applicationinstances_v2_url}")
    private String fasitApplicationInstancesUrl;

    @Value("${fasit_nodes_v2")
    private String fasitNodesUrl;

    private String username;

    private ResteasyClient client;

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
        String scopedResourceApiUri = UriBuilder.fromPath(fasitScopedResourceUrl)
                .queryParam("type", type)
                .queryParam("alias", alias)
                .queryParam("environment", scope.environment)
                .queryParam("application", scope.application)
                .queryParam("zone", scope.zone).toString();

        return get(scopedResourceApiUri, ResourcePayload.class);
    }

    public Integer getNodeCountFor(String environment, String application) {
        String nodesApiUrl = UriBuilder.fromPath(fasitNodesUrl)
                .queryParam("environment", environment)
                .queryParam("application", application).toString();

        return getCount(nodesApiUrl);
    }

    public Integer getCount(String url) {
        Response response = createRequest(url).request().get();
        checkResponseAndThrowExeption(response, url);

        String totalCount = response.getHeaderString("total_count");
        response.close();
        return Integer.valueOf(totalCount);
    }

    public ResourcesListPayload findFasitResources(ResourceType type, String alias, ScopePayload searchScope) {
        UriBuilder resourceApiUri = UriBuilder.fromPath(fasitResourcesUrl)
                .queryParam("type", type)
                .queryParam("environmentclass", searchScope.environmentclass);
        ofNullable(alias).ifPresent(a -> resourceApiUri.queryParam("alias", a));
        ofNullable(searchScope.environment).ifPresent(env -> resourceApiUri.queryParam("environment", env));
        ofNullable(searchScope.application).ifPresent(app -> resourceApiUri.queryParam("application", app));
        ofNullable(searchScope.zone).ifPresent(zone -> resourceApiUri.queryParam("zone", zone));

        return get(resourceApiUri.toString(), ResourcesListPayload.class).orElse(emptyResourcesList());
    }

    public String getFasitSecret(String url) {
        Response response = client.target(url).request().get();
        checkResponseAndThrowExeption(response, url);

        String secret = response.readEntity(String.class);
        response.close();

        return secret;
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

    public Optional<String> createFasitResource(String url, String payload, String onBehalfOfUser, String comment) {
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

        if (location != null && location.size() > 0) {
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

    public Response patch(String url, String payload) {
        try {
            log.debug("PATCH {}, payload: {}", url, payload);
            Response response = createRequest(url).request().method("PATCH", Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));
            checkResponseAndThrowExeption(response, url);
            response.close();

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
