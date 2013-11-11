package no.nav.aura.bestillingsweb.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.springframework.stereotype.Component;

@Component
@Path("/")
public class OrdersRestService {

    @POST
    @Path("/environments/{envname}/orders")
    public void postOrder(@PathParam("envname") String environmentName, String content, @QueryParam("verify") String verify) {
        System.out.println("Environment: " + environmentName + ", content: " + content + ", verify: " + verify);
    }

}
