package com.employee;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration Class.
 *
 */
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeRequests(authz -> authz
                        .requestMatchers("/employees/**")
                        .authenticated())
                .csrf().disable()
                .oauth2ResourceServer(oauth2 -> oauth2.jwt());

        return http.build();
    }
}
