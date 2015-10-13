package no.nav.aura.basta.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.aura.basta.domain.input.EnvironmentClass;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;


public class User {

    private final String name;
    private final Set<String> roles;
    private final boolean authenticated;
    private final String displayName;

    public User(String name, String displayName, Set<String> roles, boolean authenticated) {
        this.name = name;
        this.displayName = displayName;
        this.authenticated = authenticated;
        this.roles = Collections.unmodifiableSet(roles);
    }

    public User(String name, Set<String> roles) {
        this.name = name;
        this.displayName = name;
        this.roles = Collections.unmodifiableSet(roles);
        this.authenticated = false;
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new User("unauthenticated", Collections.<String> emptySet());
        }
        final Set<String> roles = new HashSet<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }

        String name = authentication.getName();
        if ("anonymousUser".equals(name)) {
            return new User(name, roles);
        } else {
            String displayName = authentication.getPrincipal() instanceof LdapUserDetails
                    ? ((LdapUserDetails) authentication.getPrincipal()).getDn()
                    : name;
            return new User(name, displayName, roles, authentication.isAuthenticated());
        }
    }

    public List<EnvironmentClass> getEnvironmentClasses() {
        return Arrays.asList(EnvironmentClass.values()).stream()
                .filter(envClass -> hasRestrictedAccess(roles, envClass))
                .collect(Collectors.toList());
    }

    private static boolean hasRestrictedAccess(Set<String> roles, EnvironmentClass environmentClass) {
        switch (environmentClass) {
        case p:
            return roles.contains(ApplicationRole.ROLE_PROD_OPERATIONS.name());
        case q:
        case t:
            return roles.contains(ApplicationRole.ROLE_OPERATIONS.name()) || roles.contains(ApplicationRole.ROLE_PROD_OPERATIONS.name());
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

    public boolean hasAccess(EnvironmentClass environmentClass) {
        return getEnvironmentClasses().contains(environmentClass);
    }

    public boolean hasSuperUserAccess() {

        return getRoles().contains(ApplicationRole.ROLE_SUPERUSER.name());
    }

    public String getDisplayName() {
        return displayName;
    }
}
