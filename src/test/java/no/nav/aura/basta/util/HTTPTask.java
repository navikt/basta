package no.nav.aura.basta.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
* Created with IntelliJ IDEA.
* User: j116592
* Date: 27.08.14
* Time: 13:33
* To change this template use File | Settings | File Templates.
*/
public class HTTPTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HTTPTask.class);

    private final URI uri;
    private final Object xmldata;
    private final HTTPOperation httpOperation;

    public HTTPTask(URI uri, Object xmldata, HTTPOperation httpOperation) {
        this.uri = uri;
        this.xmldata = xmldata;
        this.httpOperation = httpOperation;
    }

    public void run() {
        try {
            Thread.sleep(3000);
            
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodeCredentials("user", "user"));
            headers.set("Accept-Encoding", "");
        
            ResponseEntity<?> response = null;
            
            switch (this.httpOperation.toString().toLowerCase()) {
                case "get":
                    response = restTemplate.getForEntity(uri, String.class);
                    break;
                case "post": {
                    HttpEntity<Object> request = new HttpEntity<>(xmldata, headers);
                    response = restTemplate.postForEntity(uri, request, String.class);
                    break;
                }
                case "put": {
                    HttpEntity<Object> request = new HttpEntity<>(xmldata, headers);
                    response = restTemplate.exchange(uri, HttpMethod.PUT, request, String.class);
                    break;
                }
                case "delete":
                    response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP operation: " + httpOperation);
            }
            
            logger.debug("HTTP {} request to {} completed with status: {}", httpOperation, uri, 
                    response != null ? response.getStatusCode() : "N/A");
            
        } catch (Exception e) {
            logger.error("Error executing HTTP task for URI: {}", uri, e);
        }
    }

    private String encodeCredentials(String username, String password) {
        byte[] credentials = (username + ':' + password).getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(credentials);
    }
}