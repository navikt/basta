package no.nav.aura.basta.spring;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nav.aura.basta.rest.config.security.RestAuthenticationSuccessHandler;
import no.nav.aura.basta.security.AuthoritiesMapper;

@Configuration
public class SpringSecurityHandlersConfig {

    @Bean(name = "grantedAuthoritiesMapper")
    AuthoritiesMapper grantedAuthoritiesMapper() {
        return new AuthoritiesMapper();
    }
    
    @Bean(name = "restLoginSuccessHandler")
    RestAuthenticationSuccessHandler restLoginSuccessHandler() {
        return new RestAuthenticationSuccessHandler();
    }
    
    @Bean(name = "restLoginFailureHandler")
    SimpleUrlAuthenticationFailureHandler restfulAuthenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler(){
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                PrintWriter writer = response.getWriter();
                writer.write(exception.getMessage());
                writer.flush();

            }
        };
    }
    
    @Bean(name = "restLogoutSuccessHandler")
    SimpleUrlLogoutSuccessHandler restfulLogoutSuccessHandler() {
        return new SimpleUrlLogoutSuccessHandler(){
            @Override
            public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().flush();
            }
        };

    }
    
    
    /**
     * @return 401 i stedet for redirect
     */
    @Bean(name = "restEntryPoint")
	AuthenticationEntryPoint restEntryPoint(){
        return new AuthenticationEntryPoint() {

            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                response.setHeader("WWW-Authenticate","Basic realm=\"fasit\"" );
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
            }
        };
    }
}
