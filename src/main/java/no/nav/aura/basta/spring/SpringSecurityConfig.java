package no.nav.aura.basta.spring;

import no.nav.aura.basta.rest.config.security.RestAuthenticationSuccessHandler;
import no.nav.aura.basta.security.AuthoritiesMapper;
import no.nav.aura.basta.security.GroupRoleMap;
import no.nav.aura.basta.security.JwtTokenProvider;
import no.nav.aura.basta.security.NAVLdapUserDetailsMapper;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@Import({ SpringSecurityHandlersConfig.class, SpringDomainConfig.class })

public class SpringSecurityConfig {

    @Value("${LDAP_DOMAIN}")
    private String domainName;
    @Value("${LDAP_URL}")
    private String ldapUrl;
    @Value("${BASTA_OPERATIONS_GROUPS}")
    private String operationGroups;
    @Value("${BASTA_SUPERUSER_GROUPS}")
    private String superUserGroups;
    @Value("${BASTA_PRODOPERATIONS_GROUPS}")
    private String prodOperationsGroups;

    @Bean
    SecurityFilterChain securityFilterChain(
    		HttpSecurity http,
    		@Autowired CorsConfigurationSource corsConfigurationSource,
    		@Autowired AuthenticationEntryPoint restEntryPoint,
    		@Autowired RestAuthenticationSuccessHandler restLoginSuccessHandler,
    		@Autowired SimpleUrlAuthenticationFailureHandler restfulAuthenticationFailureHandler,
    		@Autowired SimpleUrlLogoutSuccessHandler restfulLogoutSuccessHandler) throws Exception {
        http
        		.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(requests -> requests
                		.dispatcherTypeMatchers(DispatcherType.REQUEST, DispatcherType.ERROR).permitAll() // allow access to error dispatcher and drop redirects
                        .requestMatchers(HttpMethod.GET, "/rest/**").permitAll()
                        .requestMatchers("/rest/**").authenticated())
                .httpBasic(basic -> basic
                		.authenticationEntryPoint(restEntryPoint))
                .csrf(csrf -> csrf
                        .disable())
                .formLogin(login -> login
                        .loginProcessingUrl("/api/login")
                        .successHandler(restLoginSuccessHandler)
                        .failureHandler(restfulAuthenticationFailureHandler))
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(restfulLogoutSuccessHandler))
                .exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(restEntryPoint)
		                .accessDeniedHandler((request, response, accessDeniedException) -> {
		                    response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
		                })
	                );
                
        return http.build();
    }
    
    @Bean
    public GroupRoleMap getGroupRoleMap() {
        return GroupRoleMap.builGroupRoleMapping(operationGroups, superUserGroups, prodOperationsGroups);
    }

    @Bean
    public JwtTokenProvider getJwtTokenProviderBean() {
        return new JwtTokenProvider(getGroupRoleMap());
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public static LogoutSuccessHandler logoutSuccessHandler() {
        return new SimpleUrlLogoutSuccessHandler();
    }

    @Bean
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(domainName, ldapUrl);
        provider.setUserDetailsContextMapper(new NAVLdapUserDetailsMapper());
        provider.setAuthoritiesMapper(new AuthoritiesMapper());
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setSearchFilter("(&(objectClass=user)(|(sAMAccountName={1})(userPrincipalName={0})(mail={0})))");

        return provider;
    }
 
    
}