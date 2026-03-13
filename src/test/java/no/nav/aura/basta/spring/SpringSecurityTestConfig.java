package no.nav.aura.basta.spring;

import no.nav.aura.basta.rest.config.security.RestAuthenticationSuccessHandler;
import no.nav.aura.basta.security.GroupRoleMap;
import no.nav.aura.basta.security.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableAspectJAutoProxy
@Import({ SpringSecurityHandlersConfig.class, SpringDomainConfig.class })
public class SpringSecurityTestConfig  {

    @Bean
    UserDetailsService inMemoryUserDetails() {
        
        UserDetails user = User.withUsername("user")
                .password("{noop}user")
                .roles("USER")
                .build();
        
        UserDetails operation = User.withUsername("operation")
                .password("{noop}operation")
                .roles("OPERATIONS")
                .build();

        UserDetails prodAdmin = User.withUsername("prodadmin")
                .password("{noop}prodadmin")
                .roles("USER", "OPERATIONS", "PROD_OPERATIONS")
                .build();

        UserDetails superuser = User.withUsername("superuser")
                .password("{noop}superuser")
                .roles("USER", "OPERATIONS", "PROD_OPERATIONS", "SUPERUSER")
                .build();
        
        return new InMemoryUserDetailsManager(prodAdmin, user, operation, superuser);
    }
    
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
                .csrf(csrf -> csrf
                		.disable())
                .authorizeHttpRequests(requests -> requests
                		.dispatcherTypeMatchers(DispatcherType.REQUEST, DispatcherType.ERROR).permitAll() // allow access to error dispatcher and drop redirects
                        .requestMatchers(HttpMethod.GET, "/rest/**").permitAll()
                        .requestMatchers("/rest/**").authenticated())
                .httpBasic(basic -> basic
                		.authenticationEntryPoint(restEntryPoint))
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
        final String operationGroups = "6283f2bd-8bb5-4d13-ae38-974e1bcc1aad,cd3983f1-fbc1-4c33-9f31-f7a40e422ccd";
        final String superUserGroups = "0000-GA-BASTA_SUPERUSER,6283f2bd-8bb5-4d13-ae38-974e1bcc1aad";
        final String prodOperationsGroups = "0000-ga-env_config_S,0000-GA-STDAPPS,6283f2bd-8bb5-4d13-ae38-974e1bcc1aad,cd3983f1-fbc1-4c33-9f31-f7a40e422ccd";

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

    
}

