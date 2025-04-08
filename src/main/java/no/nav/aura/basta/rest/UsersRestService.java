package no.nav.aura.basta.rest;


import no.nav.aura.basta.rest.dataobjects.UserDO;
import no.nav.aura.basta.security.User;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Component
@Path("/users")
public class UsersRestService {

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public UserDO getCurrentUser() {
        User user = User.getCurrentUser();
        return new UserDO(user);
    }

}
