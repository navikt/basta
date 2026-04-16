package no.nav.aura.basta.backend;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;

public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);

    private final String username;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate restTemplate;
    HttpHeaders headers = new HttpHeaders();
//    private DomainValidator validator = DomainValidator.getInstance();

    /** Setter to allow test code to inject a mock RestTemplate. */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public RestClient(String username, String password) {
        this.username = username;
        this.restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
        log.info("Initialized RestClient with Username: {}", username);
        // Allow Jackson to parse responses that arrive with content-type text/plain
        // (Fasit sometimes returns JSON with the wrong content-type header).
        List<HttpMessageConverter<?>> converters = new ArrayList<>(restTemplate.getMessageConverters());
        converters.stream()
                .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                .map(c -> (MappingJackson2HttpMessageConverter) c)
                .forEach(c -> {
                    List<MediaType> types = new ArrayList<>(c.getSupportedMediaTypes());
                    types.add(MediaType.TEXT_PLAIN);
                    c.setSupportedMediaTypes(types);
                });
        restTemplate.setMessageConverters(converters);

        // Configure basic authentication
        
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
    }

    // Helper method to create HTTP headers
    private HttpHeaders createHeaders(String onBehalfOfUser, String comment) {
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

    public Integer getCount(String url) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
//            log.debug("headers: " + entity.getHeaders());
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

    public String getFasitSecret(String url) {
        try {
            HttpHeaders headers = createHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.ALL));
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            checkResponseAndThrowException(response, url);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Not found: " + url, e);
        }
    }

    public <T> Optional<T> get(String url, Class<T> returnType) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            // Fetch as String first so we can check the status before attempting deserialization
            ResponseEntity<String> rawResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            checkResponseAndThrowException(rawResponse, url);
            String body = rawResponse.getBody();
            if (body == null || body.isBlank()) {
                return empty();
            }
            try {
                return of(objectMapper.readValue(body, returnType));
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize response from " + url + ". Body was: " + body, e);
            }
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
            log.info("Creating fasit resource with payload: {} to url: {} onBehalfOf: {}", payload, url, onBehalfOfUser);
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
            log.info("PUT {}, payload: {}", url, payload);
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
    


    // This error handler prevents RestTemplate from automatically throwing exceptions
    private static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
            return false;
        }
    }
}