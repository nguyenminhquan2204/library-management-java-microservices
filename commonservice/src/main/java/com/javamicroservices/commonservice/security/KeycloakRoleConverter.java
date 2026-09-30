package com.javamicroservices.commonservice.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Chuyển role trong JWT của Keycloak thành GrantedAuthority dạng ROLE_xxx.
 * Ưu tiên claim phẳng "roles" (protocol mapper), fallback về "realm_access.roles".
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return extractRoles(jwt).stream()
            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + role))
            .toList();
    }

    public static List<String> extractRoles(Jwt jwt) {
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
