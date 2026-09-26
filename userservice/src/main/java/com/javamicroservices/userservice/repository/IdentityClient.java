package com.javamicroservices.userservice.repository;

import com.javamicroservices.userservice.dto.identity.TokenExchangeParam;
import com.javamicroservices.userservice.dto.identity.TokenExchangeResponse;
import com.javamicroservices.userservice.dto.identity.UserCreationParam;
import com.javamicroservices.userservice.dto.identity.UserTokenExchangeParam;

import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
}
