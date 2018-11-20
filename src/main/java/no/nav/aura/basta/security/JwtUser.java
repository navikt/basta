package no.nav.aura.basta.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class JwtUser implements UserDetails {

    private String username;
    private String fullName;

    private Collection<? extends GrantedAuthority> authorities;

    private Date creationDate;

    private JwtUser(String username, String fullName,  Date creationDate) {
        this(username, fullName, creationDate, Collections.EMPTY_LIST);
    }

    public JwtUser(String username, String fullName, Date creationDate, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.fullName = fullName;
        this.creationDate = creationDate;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // no password inside JWT token.
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // A token is never locked
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // == token expiration
        // TODO
        return true;
    }

    @Override
    public boolean isEnabled() {
        // always enabled in JWT case.
        return true;
    }
}
