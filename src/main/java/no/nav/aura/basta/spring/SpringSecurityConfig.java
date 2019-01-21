package no.nav.aura.basta.spring;

import no.nav.aura.basta.security.AuthoritiesMapper;
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

    @Inject
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
    }

  /*  @Configuration
    @Order(1)
    public static class OpenEndpointsSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable().authorizeRequests().antMatchers(HttpMethod.GET, "/rest/internal/**").permitAll();
        }
    }*/

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
                    //.antMatchers(HttpMethod.GET, "/rest/internal/**").permitAll()
                    .antMatchers("/rest/api/**").authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }
    }

    @Configuration
    @Order(2)
    public static class FormLoginWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/rest/**").permitAll()
                    .antMatchers("/rest/**").authenticated()
                    .antMatchers("/**").permitAll()
                    .and()
                    .addFilterAfter(new JwtTokenProvider(), UsernamePasswordAuthenticationFilter.class)
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
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /*@Bean
    public static BeanFactoryPostProcessor init() {
        PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
        propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return propertyConfigurer;
    }*/

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