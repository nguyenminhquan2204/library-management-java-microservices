package com.javamicroservices.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javamicroservices.userservice.dto.ApiResponse;
import com.javamicroservices.userservice.dto.LoginRequestDTO;
import com.javamicroservices.userservice.dto.RefreshTokenRequestDTO;
import com.javamicroservices.userservice.dto.identity.TokenExchangeResponse;
import com.javamicroservices.userservice.service.IUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenExchangeResponse>> login(@Valid @RequestBody LoginRequestDTO body) {
        return ResponseEntity.ok(ApiResponse.success("Login successfully", userService.login(body)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenExchangeResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO body) {
        return ResponseEntity.ok(ApiResponse.success("Refresh token successfully", userService.refreshToken(body)));
    }
}
