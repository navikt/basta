package no.nav.aura.bestillingsweb.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import no.nav.aura.bestillingsweb.ApplicationRole;
import no.nav.aura.bestillingsweb.EnvironmentClass;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Component
@Path("/users")
public class UsersRestService {

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Set<String> roles = Sets.newHashSet();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        List<EnvironmentClass> environmentClasses = Lists.newArrayList(Iterables.filter(Arrays.asList(EnvironmentClass.values()), new Predicate<EnvironmentClass>() {
            public boolean apply(EnvironmentClass environmentClass) {
                return hasRestrictedAccess(roles, environmentClass);
            }
        }));
        return new UserDO(authentication, environmentClasses);
    }

    private static boolean hasRestrictedAccess(Set<String> roles, EnvironmentClass environmentClass) {
        switch (environmentClass) {
        case p:
            return roles.contains(ApplicationRole.ROLE_PROD_OPERATIONS.name());
        case q:
        case t:
            return roles.contains(ApplicationRole.ROLE_OPERATIONS.name());
        case u:
            return roles.contains(ApplicationRole.ROLE_USER.name());
        default:
            throw new RuntimeException("Unknown environment class " + environmentClass);
        }
    }

}
