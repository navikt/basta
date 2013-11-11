package no.nav.aura.bestillingsweb.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Path("/users")
public class UsersRestService {

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new UserDO(authentication);
    }

}
