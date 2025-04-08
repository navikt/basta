package no.nav.aura.basta.backend.fasit.deprecated;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.DomainDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.EnvironmentDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.NodeDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.ResourceTypeDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ApplicationInstancePayload;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Deprecated
public class FasitRestClient {

    private URI baseUrl;
    private HttpClient httpClient;
    private static final Logger log = LoggerFactory.getLogger(FasitRestClient.class);
    private Map<URI, Object> cache = new HashMap<URI, Object>();
    private String onBehalfOf;
    private boolean useCache = true;

    public FasitRestClient(String baseUrl, String username, String password) {
        this.baseUrl = UriBuilder.fromUri(baseUrl).build();

        Credentials credentials = new UsernamePasswordCredentials(username, password);
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        // The number of concurrent requests allowed, default is two. Set to 1 for now, realizing only thread safety, but no
        // concurrency.
        connectionManager.setDefaultMaxPerRoute(1);
        
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setConnectionManager(connectionManager);
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        CloseableHttpClient closeableHttpClient = clientBuilder.build();
        		
//        defaultHttpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        this.httpClient = closeableHttpClient;

        log.info("using rest based envconfig client with url : {} and user {}", baseUrl, username);
    }
  
    public UriBuilder getBaseUrl() {
        return UriBuilder.fromUri(baseUrl);
    }

    public ApplicationInstancePayload getApplicationInstance(String environment, String appName) {
        URI url = getBaseUrl().path("environments/{env}/applications/{app}").build(environment, appName);
        ApplicationInstancePayload appInstance = get(url, ApplicationInstancePayload.class);
        return appInstance;
    }

    public Collection<EnvironmentDO> getEnvironments() {
        URI url = getBaseUrl().path("environments").build();
        EnvironmentDO[] environments = get(url, EnvironmentDO[].class);
        return Arrays.asList(environments);
    }

    public String getSecret(URI url) {
        try {
            if (cache.containsKey(url)) {
                log.debug("Fetching {} from cache", url);
                return (String) cache.get(url);
            }
            log.debug("Calling url {}", url);

            WebTarget client = createWebTarget(url.toString());
            client.property("showsecret", true);
            Response response = client.request().get();
            checkResponse(response, url);
            String result = response.readEntity(String.class);
            putInCache(url, result);
            return result;
        } catch (Exception e) {
            throw rethrow(e);
        }

    }

    /** Find resources matching given scope */
    public Collection<ResourceElement> findResources(EnvClass envClass, String environment, DomainDO domain, String appName, ResourceTypeDO type, String alias) {
        URI url = buildResourceQuery(envClass, environment, domain, appName, type, alias, false, false);
        ResourceElement[] resources = get(url, ResourceElement[].class);
        return Arrays.asList(resources);
    }

    public URI buildResourceQuery(EnvClass envClass, String environment, DomainDO domain, String appName, ResourceTypeDO type, String alias, Boolean bestMatch, Boolean usage) {
        UriBuilder uribuilder = getBaseUrl().path("resources");
        if (envClass != null) {
            uribuilder.queryParam("envClass", envClass);
        }
        if (environment != null) {
            uribuilder.queryParam("envName", environment);
        }
        if (domain != null) {
            uribuilder.queryParam("domain", domain.getFqn());
        }
        if (appName != null) {
            uribuilder.queryParam("app", appName);
        }
        if (type != null) {
            uribuilder.queryParam("type", type);
        }
        if (alias != null) {
            uribuilder.queryParam("alias", alias);
        }
        if(bestMatch !=null){
            uribuilder.queryParam("bestmatch", bestMatch);
        }
        if(usage !=null){
            uribuilder.queryParam("usage", usage);
        }

        URI url = uribuilder.build();
        log.debug("REST URL " + url);
        return url;
    }

    public boolean resourceExists(EnvClass envClass, String environment, DomainDO domain, String appName, ResourceTypeDO type, String alias) {
        return !findResources(envClass, environment, domain, appName, type, alias).isEmpty();
    }

    /** Find the best matching resource given a full scope */
    public ResourceElement getResource(String environment, String alias, ResourceTypeDO type, DomainDO domain, String appName) {
        URI url = getBaseUrl().path("resources/bestmatch").queryParam("envName", environment).queryParam("domain", domain.getFqn()).queryParam("type", type)
                .queryParam("alias", alias).queryParam("app", appName).build();
        log.debug("REST URL " + url);
        ResourceElement resource = get(url, ResourceElement.class);
        return resource;
    }

    public ResourceElement getResourceById(long resourceId) {
        URI url = getBaseUrl().path("resources/" + resourceId).build();
        log.debug("REST URL " + url);
        ResourceElement resource = get(url, ResourceElement.class);
        return resource;
    }

    public NodeDO registerNode(NodeDO nodeDO, String comment) {
        URI uri = withComment(getBaseUrl().path("nodes"), comment).build();
        WebTarget client = createWebTarget(uri.toString());
        try {
            Response put = client.request().put(Entity.entity(nodeDO, MediaType.APPLICATION_XML));
            checkResponse(put, uri);
            return put.readEntity(NodeDO.class);
        } catch (Exception e) {
            log.warn("unable to register node", e);
            throw rethrow(e);
        }
    }

    /**
     * Oppdaterer ett node objekt.
     * 
     * @param nodeDO
     * @param comment
     * @return
     */
    public Response updateNode(NodeDO nodeDO, String comment) {
        URI uri = getBaseUrl().path("nodes").path(nodeDO.getHostname()).build();
        log.debug("Updating node on url", uri);
        return post(uri, nodeDO, comment);
    }

    public Response deleteResource(long id, String comment) {
        URI url = getBaseUrl().path("resources/{id}").build(id);
        return delete(url, comment);
    }

    public ResourceElement updateResource(long id, ResourceElement resource, String comment) {
        MultipartFormDataOutput data = createFormData(resource);
        return executeMultipart("POST", "resources/" + id, data, comment, ResourceElement.class);
    }

    /**
     * NB: Not implemented for file resources
     * 
     * @return
     */
    public ResourceElement registerResource(ResourceElement resource, String comment) {
        MultipartFormDataOutput data = createFormData(resource);
        return executeMultipart("PUT", "resources", data, comment, ResourceElement.class);
    }

    /**
     * Eksempel på data input<br/>
     * <code>
     *  MultipartFormDataOutput data = new MultipartFormDataOutput();<br/>
     *   data.addFormData("alias", "mintjeneste", MediaType.TEXT_PLAIN_TYPE);<br/>
     *   data.addFormData("scope.environmentclass", "u", MediaType.TEXT_PLAIN_TYPE);<br/>
     *   data.addFormData("scope.environmentname", "myTestEnv", MediaType.TEXT_PLAIN_TYPE);<br/>
     *   data.addFormData("scope.domain", "devillo.no", MediaType.TEXT_PLAIN_TYPE);<br/>
     *   data.addFormData("scope.application", "myApp", MediaType.TEXT_PLAIN_TYPE);<br/>
     *   data.addFormData("type", ResourceTypeDO.Certificate, MediaType.TEXT_PLAIN_TYPE);<br/>
     * <br/>
     *   data.addFormData("keystorealias", "app-key", MediaType.TEXT_PLAIN_TYPE);<br/>
     * </code>
     * 
     * @param method
     *            POST eller PUT
     * @param path
     *            i tillegg til baseurl til fasit ressurs
     * @param data
     *            multipart data
     * @param comment
     *            kommentar til fasit
     * @param responseClass
     * @return
     */

    public <T> T executeMultipart(String method, String path, MultipartFormDataOutput data, String comment, Class<T> responseClass) {
        URI url = withComment(getBaseUrl().path(path), comment).build();
        WebTarget client = createWebTarget(url.toString());
//        		.body(MediaType.MULTIPART_FORM_DATA, data);
        try {
            log.debug("Sending multipart to {} with method {} ", url, method);
            Response response = null;
            if ("PUT".equals(method)) {
//                response = client.put(responseClass);
                response = client.request().put(Entity.entity(data, MediaType.MULTIPART_FORM_DATA));
            } else if ("POST".equals(method)) {
//                response = client.post(responseClass);
            	response = client.request().post(Entity.entity(data, MediaType.MULTIPART_FORM_DATA));
            } else {
                throw new IllegalArgumentException("Expected HTTP method POST or PUT. Got " + method);
            }
            checkResponse(response, url);
            return response.readEntity(responseClass);
        } catch (Exception e) {
            log.warn("unable to register resource", e);
            throw rethrow(e);
        }
    }

    private MultipartFormDataOutput createFormData(ResourceElement resource) {
        MultipartFormDataOutput data = new MultipartFormDataOutput();
        Map<String, String> fields = new HashMap<>();
        fields.put("alias", resource.getAlias());
        fields.put("type", resource.getType().name());
        fields.put("scope.environmentclass", resource.getEnvironmentClass());
        fields.put("scope.domain", resource.getDomain() != null ? resource.getDomain().getFqn() : null);
        fields.put("scope.environmentname", resource.getEnvironmentName());
        fields.put("scope.application", resource.getApplication());
        if(resource.getLifeCycleStatus()!=null){
            fields.put("lifeCycleStatus",resource.getLifeCycleStatus().name()); 
        }
        if(resource.getAccessAdGroup()!=null){
            fields.put("accessAdGroup",resource.getAccessAdGroup()); 
        }
        for (Entry<String, String> entry : fields.entrySet()) {
            if (entry.getValue() != null) {
                data.addFormData(entry.getKey(), entry.getValue(), MediaType.TEXT_PLAIN_TYPE);
            }
        }
        for (PropertyElement element : resource.getProperties()) {
            data.addFormData(element.getName(), element.getValue(), MediaType.TEXT_PLAIN_TYPE);
        }
        return data;
    }

    public Response deleteNode(String hostname, String comment) {
        URI url = getBaseUrl().path("nodes").path(hostname).build();
        cache.clear();
        return delete(url, comment);
    }

    private UriBuilder withComment(UriBuilder uriBuilder, String comment) {
        if (comment != null) {
            return uriBuilder.queryParam("entityStoreComment", comment);
        }
        return uriBuilder;
    }

    private Response delete(URI url, String comment) {
        try {
            String urlString = withComment(UriBuilder.fromUri(url), comment).build().toString();
            Response response = createWebTarget(urlString).request().delete();
            checkResponse(response, url);
            response.close();
            log.debug("DELETE {} with comment {}", url, comment);
            return response;
        } catch (Exception e) {
            log.warn("Could not DELETE {} with comment {}", url, comment);
            throw rethrow(e);
        }
    }

    private Response post(URI url, Object data, String comment) {
        return post(url, data, comment, MediaType.APPLICATION_XML);
    }

    private Response post(URI url, Object data, String comment, String mediaType) {
        try {
            String urlString = withComment(UriBuilder.fromUri(url), comment).build().toString();
            WebTarget request = createWebTarget(urlString);
            Response response = request.request().post(Entity.entity(data, mediaType));
            checkResponse(response, url);
            response.close();
            log.debug("POST {} with comment {}", url, comment);
            return response;
        } catch (Exception e) {
            log.warn("Could not POST {} with comment {}", url, comment);
            throw rethrow(e);
        }
    }

    private WebTarget createWebTarget(String url) {
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        Client client = ClientBuilder.newBuilder().build();
    	

        WebTarget webTarget = client.target(url);
        if (onBehalfOf != null) {
            webTarget = webTarget.property("x-onbehalfof", onBehalfOf);
        }

        return webTarget;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(URI url, Class<T> returnType) {
        try {
            if (cache.containsKey(url)) {
                log.debug("Fetching {} from cache", url);
                return (T) cache.get(url);
            }
            log.debug("Calling url {}", url);
            WebTarget client = createWebTarget(url.toString());
            Response response = client.request().get();
            checkResponse(response, url);
            T result = response.readEntity(returnType);
            putInCache(url, result);
            return result;
        } catch (Exception e) {
            throw rethrow(e);
        }
    }

    private <T> void putInCache(URI url, T result) {
        if (result instanceof InputStream) {
            log.debug("No caching of streams");
            return;
        }
        if (useCache) {
            cache.put(url, result);
        }
    }

    private <T> void checkResponse(Response response, URI requestUrl) {
        StatusType status = response.getStatusInfo();
        if (status == Status.FORBIDDEN) {
            response.close();
            throw new SecurityException("Access forbidden to " + requestUrl);
        }
        if (status == Status.UNAUTHORIZED) {
            response.close();
            throw new SecurityException("Unautorized access to " + requestUrl);
        }
        if (status == Status.NOT_FOUND) {
            response.close();
            throw new IllegalArgumentException("Not found " + requestUrl);
        }
        if (status.getStatusCode() >= 400) {
            throw new RuntimeException("Error calling " + requestUrl + " code: " + status.getStatusCode() + "\n " + response.readEntity(String.class));
        }
    }

    /**
     * This is just a rethrow of resteasy client exceptions which should have been RuntimeExceptions. Who uses Exception as a
     * API exception in 2013?
     */
    private RuntimeException rethrow(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException(e);
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setOnBehalfOf(String onBehalfOf) {
        this.onBehalfOf = onBehalfOf;
    }

    public void useCache(boolean useCache) {
        this.useCache = useCache;
    }
}