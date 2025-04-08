package no.nav.aura.basta.spring;

import no.nav.aura.basta.security.GroupRoleMap;
import no.nav.aura.basta.security.JwtTokenProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import jakarta.inject.Inject;

@Configuration
@EnableWebSecurity
public class SpringSecurityTestConfig  {
    @Inject
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .passwordEncoder(encoder)
                .withUser("user").password(encoder.encode("user")).roles("USER")
                .and()
                .withUser("prodadmin").password(encoder.encode("prodadmin")).roles("USER", "OPERATIONS",
                "PROD_OPERATIONS")
                .and()
                .withUser("superuser").password(encoder.encode("superuser")).roles("USER", "OPERATIONS",
                "PROD_OPERATIONS", "SUPERUSER");
    }

    @Bean
    @Order(1)
	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http
        		.csrf(csrf -> csrf.disable())
        		.securityMatchers(matchers -> matchers.requestMatchers("/rest/api/**"))
	            .authorizeHttpRequests(authz -> authz
	            		.requestMatchers("/rest/api/**").authenticated()
	            )
	            .httpBasic(Customizer.withDefaults())
	            .sessionManagement(session -> session
	            		.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	            .build();
	}

	@Bean
	@Order(2)
    public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
        return http
        		.csrf(csrf -> csrf.disable())
        		.securityMatchers(matchers -> matchers.requestMatchers("/rest/**"))
	            .authorizeHttpRequests((authz) -> authz
	                    .requestMatchers(HttpMethod.GET, "/rest/**").permitAll()
	                    .requestMatchers("/rest/**").authenticated()
	                    .anyRequest().permitAll()
	            )
	            .formLogin(form -> form
	            		.loginProcessingUrl("/security-check")
	            		.failureForwardUrl("/loginfailure")
	            		.successForwardUrl("/loginsuccess")
	            		)
	            .addFilterAfter(getJwtTokenProviderBean(), UsernamePasswordAuthenticationFilter.class)
	            .httpBasic(Customizer.withDefaults())
	            .logout(logout -> logout
	                    .logoutSuccessHandler(logoutSuccessHandler())
	                    .logoutUrl("/logout")
	            )
	            .build();
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

