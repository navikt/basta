package no.nav.aura.basta.spring;

import no.nav.aura.basta.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.inject.Inject;

@Configuration
@EnableWebSecurity
public class SpringSecurityTestConfig extends WebSecurityConfigurerAdapter {

    @Inject
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("user").password("user").roles("USER")
                .and()
                .withUser("prodadmin").password("prodadmin").roles("USER", "OPERATIONS", "PROD_OPERATIONS")
                .and()
                .withUser("superuser").password("superuser").roles("USER", "OPERATIONS", "PROD_OPERATIONS", "SUPERUSER");
    }

    @Configuration
    @Order(0)
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
    @Order(1)
    public static class FormLoginWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .csrf().disable()
                    .authorizeRequests()
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
                    .logout().logoutSuccessHandler(logoutSuccessHandler()).logoutUrl("/logout");
        }
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
}
