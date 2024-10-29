package no.nav.aura.basta.util;

import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class RestEasyDetails {

    private static final Logger log = LoggerFactory.getLogger(RestEasyDetails.class);

    private final String credentials;

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
}

