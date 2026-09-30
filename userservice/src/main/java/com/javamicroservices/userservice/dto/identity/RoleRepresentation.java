package com.javamicroservices.userservice.dto.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Realm role trên Keycloak (Admin API cần cả id và name khi gán/gỡ role).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleRepresentation {
    String id;
    String name;
    String description;
}
