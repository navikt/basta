package no.nav.aura.basta.util;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestEasyDetails {

    private static final Logger log = LoggerFactory.getLogger(RestEasyDetails.class);

    private String credentials;

    public RestEasyDetails(String username, String password) {
        credentials = encodeCredentials(username, password);
    }

    ClientRequest createClientRequest(URI url) {
        ClientRequest clientRequest = new ClientRequest(url.toString());
        clientRequest.header("Authorization", "Basic " + credentials);
        // remove gzip
        clientRequest.header("Accept-Encoding", "");
        return clientRequest;
    }

    private String encodeCredentials(String username, String password) {
        byte[] credentials = (username + ':' + password).getBytes();
        return new String(Base64.encodeBase64(credentials));
    }

    <T> T get(URI url, Class<T> returnType) {
        try {

            log.debug("Calling url {}", url);
            ClientRequest client = createClientRequest(url);
            ClientResponse<T> response = client.get(returnType);
            checkResponse(response, url);
            T result = response.getEntity();

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ClientResponse performHttpOperation(ClientRequest request, HTTPOperation httpOperation) {
        try{
            switch(httpOperation){
                case PUT:  return request.put();
                case POST: return request.post();
                case GET:
                default:
                    return request.get();

            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }

    <T> void checkResponse(ClientResponse<T> response, URI requestUrl) {
        Response.Status status = response.getResponseStatus();
        if (status == Response.Status.FORBIDDEN) {
            response.releaseConnection();
            throw new SecurityException("Access forbidden to " + requestUrl);
        }
        if (status == Response.Status.UNAUTHORIZED) {
            response.releaseConnection();
            throw new SecurityException("Unautorized access to " + requestUrl);
        }

        if (status == Response.Status.NOT_FOUND) {
            response.releaseConnection();
            throw new IllegalArgumentException("Not found " + requestUrl);
        }

        if (status.getStatusCode() >= 400) {
            // String entity = response.getEntity(String.class);
            response.releaseConnection();
            throw new RuntimeException("Error calling " + requestUrl + " code: " + status.getStatusCode());
        }
    }
}

