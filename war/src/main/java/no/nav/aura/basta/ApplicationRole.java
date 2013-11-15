package no.nav.aura.basta;

import org.springframework.security.core.GrantedAuthority;

public enum ApplicationRole implements GrantedAuthority {
    ROLE_ANONYMOUS, ROLE_USER, ROLE_OPERATIONS, ROLE_PROD_OPERATIONS;

    @Override
    public String getAuthority() {
        return name();
    }

    @Override
    public String toString() {
        return name();
    }

}
