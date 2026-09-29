package com.javamicroservices.userservice.controller;

import com.javamicroservices.userservice.dto.ApiResponse;
import com.javamicroservices.userservice.dto.CreateUserRequestDTO;
import com.javamicroservices.userservice.dto.UserResponseDTO;
import com.javamicroservices.userservice.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
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
}
