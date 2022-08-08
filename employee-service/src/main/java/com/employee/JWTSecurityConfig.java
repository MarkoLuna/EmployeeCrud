package com.employee;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 *
 * TODO pass to {@link org.springframework.security.web.SecurityFilterChain}
 */
@Configuration
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authz -> authz
                        .antMatchers("/employees/**")
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt());
    }
}
