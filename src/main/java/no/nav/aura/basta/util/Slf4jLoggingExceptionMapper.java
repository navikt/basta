package no.nav.aura.basta.util;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Provider
@Component
public class Slf4jLoggingExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jLoggingExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException e) {
        int status;
        String message;
        if (e instanceof Failure) {
            // Errors from RestEasy
            Failure f = (Failure) e;
            if (f.getErrorCode() != -1) {
                status = f.getErrorCode();
            } else {
                status = f.getResponse().getStatus();
            }
            message = f.getMessage();
            logger.warn("Rest returned code: {} reason: {}", status, message);
        } else if (e instanceof IllegalArgumentException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            message = e.getMessage();
            logger.warn("Bad request", e);
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            message = e.getMessage();
            logger.error("Internal error", e);
        }
        return Response.status(status).entity(message).type(TEXT_PLAIN_TYPE).build();
    }
}
