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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.inject.Inject;

@EnableWebSecurity
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

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

    @Inject
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
    }

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        protected void configure(HttpSecurity http) throws Exception {
            http
                    .csrf().disable()
                    .requestMatchers()
                    .antMatchers("/rest/api/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/rest/api/**").authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }
    }

    @Configuration
    @Order(2)
    public class FormLoginWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/rest/**").permitAll()
                    .antMatchers("/rest/**").authenticated()
                    .antMatchers("/**").permitAll()
                    .and()
                    .addFilterAfter(getJwtTokenProviderBean(), UsernamePasswordAuthenticationFilter.class)
                    .formLogin()
                    .loginProcessingUrl("/security-check")
                    .failureForwardUrl("/loginfailure")
                    .successForwardUrl("/loginsuccess")
                    .and()
                    .httpBasic()
                    .and()
                    .logout().logoutSuccessHandler(logoutSuccessHandler()).logoutUrl("/logout")
                    .and()
                    .csrf().disable();
        }
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
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
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