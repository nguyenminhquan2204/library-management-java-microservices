package com.javamicroservices.apigateway.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String LIBRARIAN = "LIBRARIAN";

    /**
     * Lớp chặn thô theo path + method. Phân quyền chi tiết (ownership, ...) do từng service tự kiểm tra.
     */
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                    .pathMatchers("/api/v1/public/**").permitAll()

                    .pathMatchers(HttpMethod.GET, "/api/v1/books/**").authenticated()
                    .pathMatchers("/api/v1/books/**").hasAnyRole(LIBRARIAN, ADMIN)

                    .pathMatchers(HttpMethod.GET, "/api/v1/employees/**").hasAnyRole(LIBRARIAN, ADMIN)
                    .pathMatchers("/api/v1/employees/**").hasRole(ADMIN)

                    .pathMatchers("/api/v1/borrowing/**").authenticated()

                    .pathMatchers("/api/v1/users/me").authenticated()
                    .pathMatchers("/api/v1/users/**", "/api/v1/roles/**").hasRole(ADMIN)

                    .anyExchange().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler())
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter()))
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler())
                );
        return http.build();
    }

    /** Thiếu token / token sai hoặc hết hạn -> 401 theo format ApiResponse. */
    private ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> writeError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or missing access token");
    }

    /** Token hợp lệ nhưng không đủ role -> 403 theo format ApiResponse. */
    private ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, ex) -> writeError(exchange, HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
    }

    /** Message là hằng số nên không cần escape JSON. */
    private static Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format(
            "{\"statusCode\":%d,\"message\":\"%s\",\"error\":\"%s\"}",
            status.value(), message, status.getReasonPhrase());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Đọc role của Keycloak (claim "roles", fallback "realm_access.roles") thành ROLE_xxx.
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> keycloakJwtConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> extractRoles(jwt).stream()
            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
            .toList());
        converter.setPrincipalClaimName("preferred_username");
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    private static List<String> extractRoles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            return roles;
        }
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> realmRoles) {
            return realmRoles.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
