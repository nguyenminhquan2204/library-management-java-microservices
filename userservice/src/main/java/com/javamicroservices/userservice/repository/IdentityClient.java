package com.javamicroservices.userservice.repository;

import com.javamicroservices.userservice.dto.identity.RoleRepresentation;
import com.javamicroservices.userservice.dto.identity.TokenExchangeParam;
import com.javamicroservices.userservice.dto.identity.TokenExchangeResponse;
import com.javamicroservices.userservice.dto.identity.UserCreationParam;
import com.javamicroservices.userservice.dto.identity.UserTokenExchangeParam;

import feign.QueryMap;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "identity-client", url = "${idp.url}")
public interface IdentityClient {

    @PostMapping(
            value = "/realms/javamicroservice/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeClientToken(@QueryMap() TokenExchangeParam param);

    @PostMapping(
            value = "admin/realms/javamicroservice/users",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<?> createUser(@RequestBody() UserCreationParam body, @RequestHeader("authorization") String token);

    @PostMapping(
            value = "/realms/javamicroservice/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeUserToken(@QueryMap UserTokenExchangeParam param);

    @GetMapping(value = "admin/realms/javamicroservice/roles")
    List<RoleRepresentation> getRealmRoles(@RequestHeader("authorization") String token);

    @GetMapping(value = "admin/realms/javamicroservice/users/{userId}/role-mappings/realm")
    List<RoleRepresentation> getUserRealmRoles(@PathVariable("userId") String userId, @RequestHeader("authorization") String token);

    @PostMapping(
            value = "admin/realms/javamicroservice/users/{userId}/role-mappings/realm",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> addUserRealmRoles(@PathVariable("userId") String userId, @RequestBody List<RoleRepresentation> roles, @RequestHeader("authorization") String token);

    @DeleteMapping(
            value = "admin/realms/javamicroservice/users/{userId}/role-mappings/realm",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> removeUserRealmRoles(@PathVariable("userId") String userId, @RequestBody List<RoleRepresentation> roles, @RequestHeader("authorization") String token);
}
