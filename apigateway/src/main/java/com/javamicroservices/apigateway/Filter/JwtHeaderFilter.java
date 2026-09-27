package com.javamicroservices.apigateway.Filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtHeaderFilter extends AbstractGatewayFilterFactory<JwtHeaderFilter.Config> {

    public JwtHeaderFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(JwtHeaderFilter.Config config) {
        return (exchange, chain) -> exchange.getPrincipal()
            .filter(principal -> principal instanceof JwtAuthenticationToken)
            .cast(JwtAuthenticationToken.class)
            .map(jwtAuthenticationToken -> {
                var jwt = jwtAuthenticationToken.getToken();
                var userId = jwt.getSubject();
                var userName = jwt.getClaimAsString("preferred_username");
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-Username");
                        if (userId != null) headers.set("X-User-Id", userId);
                        if (userName != null) headers.set("X-Username", userName);
                    })
                    .build();
                return exchange.mutate().request(mutatedRequest).build();
            })
            .defaultIfEmpty(exchange)
            .flatMap(chain::filter);
    }

    public static class Config {

    }
}
