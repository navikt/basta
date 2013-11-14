package no.nav.aura.bestillingsweb.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import no.nav.aura.bestillingsweb.User;

import org.springframework.stereotype.Component;

@Component
@Path("/users")
public class UsersRestService {

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDO getCurrentUser() {
        User user = User.getCurrentUser();
        return new UserDO(user);
    }

}
