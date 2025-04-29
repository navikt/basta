package no.nav.aura.basta.spring;

import no.nav.aura.basta.security.AuthoritiesMapper;
import no.nav.aura.basta.security.GroupRoleMap;
import no.nav.aura.basta.security.JwtTokenProvider;
import no.nav.aura.basta.security.NAVLdapUserDetailsMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@EnableWebSecurity
@Configuration
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