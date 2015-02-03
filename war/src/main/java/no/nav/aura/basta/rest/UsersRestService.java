package no.nav.aura.basta.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import no.nav.aura.basta.rest.dataobjects.UserDO;
import no.nav.aura.basta.security.User;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.springframework.stereotype.Component;

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
