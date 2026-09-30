package com.javamicroservices.commonservice.security;

/**
 * Tên các realm role trên Keycloak (realm javamicroservice).
 * Dùng trong code Java; trong @PreAuthorize vẫn viết chuỗi, vd: hasAnyRole('LIBRARIAN','ADMIN').
 */
public final class Roles {

    public static final String ADMIN = "ADMIN";
    public static final String LIBRARIAN = "LIBRARIAN";
    public static final String MEMBER = "MEMBER";

    private Roles() {
    }
}
