package no.nav.aura.basta.backend.bigip;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import org.springframework.stereotype.Component;

@Component
public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);

    private String credentials;

    public RestClient() {
    }

    public RestClient(String username, String password) {
        credentials = encodeCredentials(username, password);
    }

    private ClientRequest createClientRequest(String url) {
        ClientRequest clientRequest = new ClientRequest(url, TrustyExecutor.getTrustingExecutor());

        if (credentials != null) {
            clientRequest.header("Authorization", "Basic " + credentials);
        }

        clientRequest.header("Content-Type", "application/json");
        clientRequest.header("Accept", "application/json");
        return clientRequest;
    }

    private String encodeCredentials(String username, String password) {
        byte[] credentials = (username + ':' + password).getBytes();
        return new String(Base64.encodeBase64(credentials));
    }

    public <T> Optional<T> get(String url, Class<T> returnType) {
        try {
            log.debug("GET {}", url);
            ClientRequest client = createClientRequest(url);
            ClientResponse<T> response = client.get(returnType);

            if (notFound(response)) {
                return Optional.absent();
            }

            checkResponse(response, url);
            T result = response.getEntity();

            return Optional.of(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean notFound(Response response) {
        return response.getStatus() == NOT_FOUND.getStatusCode();
    }

    <T> void checkResponse(ClientResponse<T> response, String requestUrl) {
        log.debug("Got response {} calling {}", stringifyResponse(response), requestUrl);

        Response.Status status = response.getResponseStatus();
        if (status == Response.Status.FORBIDDEN) {
            response.releaseConnection();
            throw new SecurityException("Access forbidden to " + requestUrl + ". " + getResponseEntityAsString(response));
        }
        if (status == Response.Status.UNAUTHORIZED) {
            response.releaseConnection();
            throw new SecurityException("Unautorized access to " + requestUrl + ". " + getResponseEntityAsString(response));
        }

        if (status.getStatusCode() >= 400) {
            String responseEntityAsString = getResponseEntityAsString(response);
            log.debug("Got status {} on url {}. Response from server was {}", stringifyResponse(response), requestUrl, responseEntityAsString);

            response.releaseConnection();

            if (status == Response.Status.NOT_FOUND) {
                return;
            } else {
                throw new RuntimeException("Error calling " + requestUrl + ". Got " + stringifyResponse(response) + " with payload " + responseEntityAsString);
            }
        }
    }

    private <T> String getResponseEntityAsString(ClientResponse<T> response) {
        try {
            return response.getEntity(String.class);
        } catch (Exception e) {
            log.error("Unable to get entity from response", e);
            return null;
        }
    }

    public Response delete(String url) {
        try {
            log.debug("DELETE {}", url);
            ClientRequest client = createClientRequest(url);
            ClientResponse response = client.delete();

            checkResponse(response, url);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String stringifyResponse(ClientResponse response) {
        final Response.Status responseStatus = response.getResponseStatus();
        return "HTTP " + responseStatus.getStatusCode() + " (" + responseStatus.getReasonPhrase() + ")";
    }

    public Response post(String url, String payload) {
        try {
            log.debug("POST {}, payload: {}", url, payload);
            ClientRequest client = createClientRequest(url).body(MediaType.APPLICATION_JSON_TYPE, payload);
            ClientResponse response = client.post();

            checkResponse(response, url);

            if (notFound(response)) {
                throw new RuntimeException("Got HTTP 404 trying to POST payload " + payload + " to url " + url);
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Response put(String url, String payload) {
        try {
            log.debug("PUT {}, payload: {}", url, payload);
            ClientRequest client = createClientRequest(url).body(MediaType.APPLICATION_JSON_TYPE, payload);
            ClientResponse response = client.put();

            checkResponse(response, url);

            if (notFound(response)) {
                throw new RuntimeException("Got HTTP 404 trying to PUT payload " + payload + " to url " + url);
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
