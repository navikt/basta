package no.nav.aura.bestillingsweb;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class User {

    private final String name;
    private final Set<String> roles;
    private final boolean authenticated;

    public User(String name, Set<String> roles, boolean authenticated) {
        this.name = name;
        this.authenticated = authenticated;
        this.roles = ImmutableSet.copyOf(roles);
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Set<String> roles = Sets.newHashSet();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        String name = authentication.getName();
        return new User(name, roles, !"anonymousUser".equals(name) && authentication.isAuthenticated());
    }

    public List<EnvironmentClass> getEnvironmentClasses() {
        return Lists.newArrayList(Iterables.filter(Arrays.asList(EnvironmentClass.values()), new Predicate<EnvironmentClass>() {
            public boolean apply(EnvironmentClass environmentClass) {
                return hasRestrictedAccess(roles, environmentClass);
            }
        }));
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

    public Set<String> getRoles() {
        return roles;
    }

    public String getName() {
        return name;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

}
