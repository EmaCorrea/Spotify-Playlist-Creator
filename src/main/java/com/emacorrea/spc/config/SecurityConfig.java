package com.emacorrea.spc.config;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String [] ALLOWED = {
            "/",
            "/api/**",
            "/health",
            "/info",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/v2/api-docs"
    };

    @Bean
    public SecurityWebFilterChain configure(final ServerHttpSecurity http) {
        http.csrf().disable()
                .headers()
                .and()
                    .authorizeExchange()
                    .pathMatchers(ALLOWED).permitAll()
                    .matchers(EndpointRequest.to("health", "info")).permitAll()
                    .matchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
                .and()
                    .httpBasic().disable();

        return http.build();
    }

}
