package no.nav.aura.basta.backend;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.Optional;

@Component
public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    private final static Charset UTF8 = Charset.forName("UTF-8");

//    private String credentials;
    private ResteasyClient client;

    public RestClient() {
    }

    public RestClient(String username, String password) {
        client = new ResteasyClientBuilder()
                .disableTrustManager()
                .register(new BasicAuthentication(username, password))
                .build();

//        client.register(TrustyExecutor.getTrustingExecutor());
//        credentials = encodeCredentials(username, password);
    }

    WebTarget createRequest(String url) {

        ResteasyWebTarget target = client.target(url);

        target.request().header("Content-Type", "application/json");
        target.request().header("Accept", "application/json");

        return target;

    }

//    private Request createClientRequest(String url) {
//        ClientRequest clientRequest = new ClientRequest(url, TrustyExecutor.getTrustingExecutor());

//        if (credentials != null) {
//            clientRequest.header("Authorization", "Basic " + credentials);
//        }

//        clientRequest.header("Content-Type", "application/json");
//        clientRequest.header("Accept", "application/json");
//        return clientRequest;
//    }

//    private String encodeCredentials(String username, String password) {
//        byte[] credentials = (username + ':' + password).getBytes();
//        return new String(Base64.encodeBase64(credentials));
//    }

    public <T> Optional<T> get(String url, Class<T> returnType) {
        try {
            log.debug("GET {}", url);
//            ClientRequest client = createClientRequest(url);
            Response response = createRequest(url).request().get();

            checkResponseAndThrowExeption(response, url);
            T result = response.readEntity(returnType);
            response.close();

//            if (notFound(response)) {
//                return Optional.empty();
//            }

//            checkResponse(response, url);

            return Optional.of(result);

        } catch (NotFoundException nfe) {
            return Optional.empty();
        }
//        catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

//    public static boolean notFound(Response response) {
//        return response.getStatus() == NOT_FOUND.getStatusCode();
//    }

//    <T> void checkResponse(Response response, String requestUrl) {
//        log.debug("Got response {} calling {}", stringifyResponse(response), requestUrl);
//
//        Response.Status status = response.getResponseStatus();
//        if (status == Response.Status.FORBIDDEN) {
//            response.releaseConnection();
//            throw new SecurityException("Access forbidden to " + requestUrl + ". " + getResponseEntityAsString(response));
//        }
//        if (status == Response.Status.UNAUTHORIZED) {
//            response.releaseConnection();
//            throw new SecurityException("Unautorized access to " + requestUrl + ". " + getResponseEntityAsString(response));
//        }
//
//        if (status.getStatusCode() >= 400) {
//            String responseEntityAsString = getResponseEntityAsString(response);
//            log.debug("Got status {} on url {}. Response from server was {}", stringifyResponse(response), requestUrl, responseEntityAsString);
//
//            response.releaseConnection();
//

//            if (status == Response.Status.NOT_FOUND) {
//                return;
//            } else {
//                throw new RuntimeException("Error calling " + requestUrl + ". Got " + stringifyResponse(response) + " with payload " + responseEntityAsString);
//            }
//        }
//    }

//    private <T> String getResponseEntityAsString(ClientResponse<T> response) {
//        try {
//            return response.getEntity(String.class);
//        } catch (Exception e) {
//            log.error("Unable to get entity from response", e);
//            return null;
//        }
//    }

    public Response delete(String url) {
        try {
            log.debug("DELETE {}", url);
            Response response = createRequest(url).request().delete();

            checkResponseAndThrowExeption(response, url);

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
            throw new NotAuthorizedException("Unautorized access to " + requestUrl);
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

//    public static String stringifyResponse(Response response) {
//        final int responseStatus = response.getStatus();
//        response.get
//        return "HTTP " + responseStatus + " (" + responseStatus.getReasonPhrase() + ")";
//    }

    public Response post(String url, String payload) {
        try {
            log.debug("POST {}, payload: {}", url, payload);
//            ClientRequest client = createRequest(url).request().body(MediaType.APPLICATION_JSON_TYPE, payload);
            Response response = createRequest(url).request().post(Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));
//            ClientResponse response = client.post();

            checkResponseAndThrowExeption(response, url);
            response.close();

//            if (notFound(response)) {
//                throw new RuntimeException("Got HTTP 404 trying to POST payload " + payload + " to url " + url);
//            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error trying to POST payload " + payload + " to url " + url, e);
        }
    }

    public Response put(String url, String payload) {
        try {
            log.debug("PUT {}, payload: {}", url, payload);
            Response response = createRequest(url).request().put(Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));
//            ClientRequest client = createClientRequest(url).body(MediaType.APPLICATION_JSON_TYPE, payload);
//            ClientResponse response = client.put();

            checkResponseAndThrowExeption(response, url);

//            checkResponse(response, url);

//            if (notFound(response)) {
//                throw new RuntimeException("Got HTTP 404 trying to PUT payload " + payload + " to url " + url);
//            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
