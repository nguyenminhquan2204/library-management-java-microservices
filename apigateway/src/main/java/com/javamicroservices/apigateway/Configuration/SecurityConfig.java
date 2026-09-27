package com.javamicroservices.apigateway.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration 
@EnableReactiveMethodSecurity 
public class SecurityConfig {

    @Bean 
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                    .pathMatchers("api/v1/public/**").permitAll()
                    .anyExchange().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                    .jwt(Customizer.withDefaults())
                );
        return http.build();
    }
}
