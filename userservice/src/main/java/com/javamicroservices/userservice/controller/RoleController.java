package com.javamicroservices.userservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javamicroservices.userservice.dto.ApiResponse;
import com.javamicroservices.userservice.dto.identity.RoleRepresentation;
import com.javamicroservices.userservice.service.IUserService;

@RestController
@RequestMapping("/api/v1/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    @Autowired
    private IUserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleRepresentation>>> getAssignableRoles() {
        return ResponseEntity.ok(ApiResponse.success("Get roles successfully", userService.getAssignableRoles()));
    }
}
