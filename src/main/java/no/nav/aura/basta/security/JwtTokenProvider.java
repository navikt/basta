package no.nav.aura.basta.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
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
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static java.util.stream.Collectors.toSet;


public class JwtTokenProvider extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    static final String TOKEN_PREFIX = "Bearer ";
    static final String HEADER_STRING = "Authorization";
    final JWTClaimsSetVerifier jwtClaimsSetVerifier = new JwtClaimsVerifyer();
    private final URL KEY_SET_LOCATION = createURL("https://login.microsoftonline.com/common/discovery/keys");


    private static GroupRoleMap groupRoleMap;

    public JwtTokenProvider() {
        if(groupRoleMap == null ) {
            groupRoleMap = GroupRoleMap.builGroupRoleMapping();
        }
     }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        getToken(servletRequest).ifPresent(token -> validateToken(token));
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private Optional<String> getToken(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String header = request.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            logger.debug("No Bearer token in request");
            return Optional.empty();
        }

        return Optional.of(header.replace(TOKEN_PREFIX, ""));
    }


    private void validateToken(String token) {

        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();

        JWKSource keySource = new RemoteJWKSet(KEY_SET_LOCATION);
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.setJWTClaimsSetVerifier(jwtClaimsSetVerifier);

        SecurityContext ctx = null;
        JWTClaimsSet claimsSet;
        try {
            claimsSet = jwtProcessor.process(token, ctx);
            Authentication authentication = buildAuthenticationFrom(claimsSet);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new RuntimeException("Token validation error " + e.getMessage());
        }
    }

    private Authentication buildAuthenticationFrom(JWTClaimsSet claimsSet) throws ParseException {
        Collection<? extends GrantedAuthority> authorities = getGroups(claimsSet);



        Date issueTime = claimsSet.getIssueTime();
        String username = claimsSet.getStringClaim("upn");
        String fullName = claimsSet.getStringClaim("given_name") + " " + claimsSet.getStringClaim("family_name");
        JwtUser userDetails = new JwtUser(username, fullName, issueTime, authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    public static Collection<? extends GrantedAuthority> getGroups(JWTClaimsSet claims) throws ParseException {
        return claims.getStringListClaim("groups")
                .stream()
                .map(group -> groupRoleMap.getRoles(group))
                .flatMap(Collection::stream)
                .collect(toSet());
    }

    private static URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error parsing URL");
        }
    }


}
