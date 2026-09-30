package com.javamicroservices.commonservice.security;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Truy cập thông tin user hiện tại (lấy từ JWT của request đang xử lý).
 */
public final class SecurityUtils {

    public static final String EMPLOYEE_ID_CLAIM = "employeeId";

    private SecurityUtils() {
    }

    public static Optional<Jwt> currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return Optional.of(jwtAuthentication.getToken());
        }
        return Optional.empty();
    }

    /** Keycloak user id (claim "sub"). */
    public static String currentUserId() {
        return currentJwt().map(Jwt::getSubject).orElse(null);
    }

    public static String currentUsername() {
        return currentJwt().map(jwt -> jwt.getClaimAsString("preferred_username")).orElse(null);
    }

    /** employeeId gắn với user (user attribute trên Keycloak, map vào token). */
    public static String currentEmployeeId() {
        return currentJwt().map(jwt -> jwt.getClaimAsString(EMPLOYEE_ID_CLAIM)).orElse(null);
    }

    public static boolean hasAnyRole(String... roles) {
        return currentJwt()
            .map(KeycloakRoleConverter::extractRoles)
            .map(userRoles -> Arrays.stream(roles).anyMatch(userRoles::contains))
            .orElse(false);
    }
}
