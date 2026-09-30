package com.javamicroservices.userservice.service;

import com.javamicroservices.userservice.dto.CreateUserRequestDTO;
import com.javamicroservices.userservice.dto.LoginRequestDTO;
import com.javamicroservices.userservice.dto.RefreshTokenRequestDTO;
import com.javamicroservices.userservice.dto.RoleAssignmentRequestDTO;
import com.javamicroservices.userservice.dto.UserResponseDTO;
import com.javamicroservices.userservice.dto.identity.RoleRepresentation;
import com.javamicroservices.userservice.dto.identity.TokenExchangeResponse;

import java.util.List;

public interface IUserService {
    UserResponseDTO createUser(CreateUserRequestDTO dto);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO updateUser(Long id, CreateUserRequestDTO dto);
    void deleteUser(Long id);
    TokenExchangeResponse login(LoginRequestDTO dto);
    TokenExchangeResponse refreshToken(RefreshTokenRequestDTO dto);
    UserResponseDTO getCurrentUser(String keycloakUserId);
    List<RoleRepresentation> getAssignableRoles();
    List<RoleRepresentation> getUserRoles(Long id);
    List<RoleRepresentation> assignRoles(Long id, RoleAssignmentRequestDTO dto);
    List<RoleRepresentation> removeRoles(Long id, RoleAssignmentRequestDTO dto);
}
