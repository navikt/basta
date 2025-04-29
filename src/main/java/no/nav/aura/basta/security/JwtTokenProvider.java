package no.nav.aura.basta.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class JwtTokenProvider extends GenericFilterBean {

    static final String TOKEN_PREFIX = "Bearer ";
    static final String HEADER_STRING = "Authorization";
    final JWTClaimsSetVerifier<SecurityContext> jwtClaimsSetVerifier = new JwtClaimsVerifyer<SecurityContext>();
    private final URL KEY_SET_LOCATION = createURL("https://login.microsoftonline.com/common/discovery/keys");

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final GroupRoleMap groupRoleMap;

    public JwtTokenProvider(GroupRoleMap groupRoleMap/*String operationGroups, String prodOperationsGroups, String superUserGroups */) {
       this.groupRoleMap = groupRoleMap;
       log.info("GRM initialized " + groupRoleMap);
     }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        getToken(servletRequest).ifPresent(this::validateToken);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private Optional<String> getToken(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String header = request.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            return Optional.empty();
        }

        return Optional.of(header.replace(TOKEN_PREFIX, ""));
    }


    private void validateToken(String token) {
        log.debug("Validating token");
        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
//        JWKSource keySource = new RemoteJWKSet(KEY_SET_LOCATION);
        JWKSource<SecurityContext> keySource2 = JWKSourceBuilder.create(KEY_SET_LOCATION).build();
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource2);
        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.setJWTClaimsSetVerifier(jwtClaimsSetVerifier);

        SecurityContext ctx = null;
        JWTClaimsSet claimsSet;
        try {
            claimsSet = jwtProcessor.process(token, ctx);
            Authentication authentication = buildAuthenticationFrom(claimsSet);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ParseException | BadJOSEException | JOSEException e) {
            e.printStackTrace();
            throw new RuntimeException("Token validation error " + e.getMessage(), e);
        }
    }

    private Authentication buildAuthenticationFrom(JWTClaimsSet claimsSet) throws ParseException {
        Collection<? extends GrantedAuthority> authorities = getGroups(claimsSet);

        Date issueTime = claimsSet.getIssueTime();
        String username = claimsSet.getStringClaim("preferred_username");
        String fullName = claimsSet.getStringClaim("name");
        JwtUser userDetails = new JwtUser(username, fullName, issueTime, authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    public Collection<? extends GrantedAuthority> getGroups(JWTClaimsSet claims) throws ParseException {
        Set<ApplicationRole> groups = claims.getStringListClaim("groups")
                .stream()
                .map(group -> groupRoleMap.getRoles(group))
                .flatMap(Collection::stream)
                .collect(toSet());
        groups.add(ApplicationRole.ROLE_USER);

        return groups;
    }

    private static URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error parsing URL");
        }
    }
}
