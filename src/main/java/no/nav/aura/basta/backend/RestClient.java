package no.nav.aura.basta.backend;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    private final static Charset UTF8 = Charset.forName("UTF-8");
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

    public <T> Optional<T> get(String url, Class<T> returnType) {

            log.debug("GET {}", url);
        try {
            Response response = createRequest(url).request().get();
            checkResponseAndThrowExeption(response, url);
            T result = response.readEntity(returnType);
            response.close();

            return Optional.of(result);

        } catch (NotFoundException nfe) {
            return Optional.empty();
        }
    }

    public Response delete(String url, String payload) {
        try {
            log.debug("DELETE {}", url);
            Response response = createRequest(url).request().method("DELETE", Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));

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

    public  <T> T postAs(String url, String payload, String onBehalfOfUser, String comment, Class<T> returnType) {
            try {
                log.debug("POST {} as {}, payload: {} with user {}", url, onBehalfOfUser, payload, username);

                Response response = createRequest(url)
                        .request()
                        .header("x-onbehalfof", onBehalfOfUser)
                        .header("x-comment", comment)
                        .post(Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));

                checkResponseAndThrowExeption(response, url);
                T entity = response.readEntity(returnType);
                response.close();

                return entity;
            } catch (Exception e) {

                throw new RuntimeException("Error trying to POST payload " + payload + " to url " + url, e);
            }
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

    public <T> T  putAs(String url, String payload, String onBehalfOfUser, String comment, Class<T> returnType) {
        try {
            log.debug("PUT {}, payload: {}", url, payload);
            Response response = createRequest(url)
                    .request()
                    .header("x-onbehalfof", onBehalfOfUser)
                    .header("x-comment", comment)
                    .put(Entity.entity(payload.getBytes(UTF8), MediaType.APPLICATION_JSON));
            checkResponseAndThrowExeption(response, url);
            T entity = response.readEntity(returnType);
            response.close();

            return entity;
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
