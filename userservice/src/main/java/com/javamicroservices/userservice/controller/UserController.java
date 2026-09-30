package com.javamicroservices.userservice.controller;

import com.javamicroservices.userservice.dto.ApiResponse;
import com.javamicroservices.userservice.dto.CreateUserRequestDTO;
import com.javamicroservices.userservice.dto.RoleAssignmentRequestDTO;
import com.javamicroservices.userservice.dto.UserResponseDTO;
import com.javamicroservices.userservice.dto.identity.RoleRepresentation;
import com.javamicroservices.userservice.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody CreateUserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("User created successfully", userService.createUser(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Get all users successfully", userService.getAllUsers()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Get current user successfully", userService.getCurrentUser(jwt.getSubject())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Get user detail successfully", userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(@PathVariable Long id, @RequestBody CreateUserRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userService.updateUser(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<List<RoleRepresentation>>> getUserRoles(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Get user roles successfully", userService.getUserRoles(id)));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<List<RoleRepresentation>>> assignRoles(@PathVariable Long id, @Valid @RequestBody RoleAssignmentRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Assign roles successfully", userService.assignRoles(id, dto)));
    }

    @DeleteMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<List<RoleRepresentation>>> removeRoles(@PathVariable Long id, @Valid @RequestBody RoleAssignmentRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Remove roles successfully", userService.removeRoles(id, dto)));
    }
}
