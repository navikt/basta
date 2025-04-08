package no.nav.aura.basta.rest.api;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import no.nav.aura.basta.backend.serviceuser.FasitServiceUserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import no.nav.aura.basta.rest.serviceuser.ServiceUserCredentialOperationRestService;

@Component
@Path("/api/orders/serviceuser")
@Transactional
public class ServiceuserRestApi {

    private static final Logger logger = LoggerFactory.getLogger(ServiceuserRestApi.class);

    @Inject
    private ServiceUserCredentialOperationRestService operationsService;
    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response stopCredential(Map<String, String> input) {
    	Map<String, String> params = null;
    	try {
    		params = validateAndEnrichInput(input);
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(e.getMessage())
					.build();
		}
        logger.info("Stopping credential with params {}" + params);
        Response response = operationsService.stopServiceUserCredential(params, uriInfo);
        return response;
    }

    @POST
    @Path("/remove")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
        public Response deleteCredential(Map<String, String> input) {
            Map<String, String> params = validateAndEnrichInput(input);
            logger.info("Delteing credential with params {}" + params);
            Response response = operationsService.deleteServiceUser(params, uriInfo);
            return response;
    }

    private Map<String, String> validateAndEnrichInput(Map<String, String> input) {
        if (!input.containsKey("environmentClass")) {
            throw new BadRequestException("Missing required inputparameter environmentClass");
        }
        if (!input.containsKey("zone")) {
            throw new BadRequestException("Missing required inputparameter zone");
        }

        if (!input.containsKey("application") && !input.containsKey("fasitAlias")) {
            throw new BadRequestException("Input must contain parameter application or fasitAlias");
        }

        if (input.containsKey("fasitAlias")) {
            String alias = input.get("fasitAlias");
            logger.info("Parameter application not found. Using parameter fasitAlias {} to find application name", alias);
            input.put("application", FasitServiceUserAccount.getApplicationNameFromAlias(alias));
        }

        return input;
    }

}
