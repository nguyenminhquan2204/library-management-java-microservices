package com.javamicroservices.userservice.service.impl;

import com.javamicroservices.userservice.dto.CreateUserRequestDTO;
import com.javamicroservices.userservice.dto.LoginRequestDTO;
import com.javamicroservices.userservice.dto.RefreshTokenRequestDTO;
import com.javamicroservices.userservice.dto.RoleAssignmentRequestDTO;
import com.javamicroservices.userservice.dto.UserResponseDTO;
import com.javamicroservices.userservice.dto.identity.Credential;
import com.javamicroservices.userservice.dto.identity.RoleRepresentation;
import com.javamicroservices.userservice.dto.identity.TokenExchangeParam;
import com.javamicroservices.userservice.dto.identity.TokenExchangeResponse;
import com.javamicroservices.userservice.dto.identity.UserCreationParam;
import com.javamicroservices.userservice.dto.identity.UserTokenExchangeParam;
import com.javamicroservices.userservice.entity.User;
import com.javamicroservices.userservice.exception.BadRequestException;
import com.javamicroservices.userservice.exception.ConflictException;
import com.javamicroservices.userservice.exception.NotFoundException;
import com.javamicroservices.userservice.exception.UnauthorizedException;
import com.javamicroservices.userservice.repository.IdentityClient;
import com.javamicroservices.userservice.repository.UserRepository;

import com.javamicroservices.userservice.service.IUserService;
import feign.FeignException;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdentityClient identityClient;

    @Value("${idp.client-id}")
    @NonFinal
    String clientId;

    @Value("${idp.client-secret}")
    @NonFinal
    String clientSecret;

    /** Chỉ cho phép gán/gỡ các role nghiệp vụ, không đụng tới role hệ thống của Keycloak. */
    private static final Set<String> ASSIGNABLE_ROLES = Set.of("ADMIN", "LIBRARIAN", "MEMBER");

    @Override
    public UserResponseDTO createUser(CreateUserRequestDTO dto) {
        ResponseEntity<?> creationResponse;
        try {
            creationResponse = identityClient.createUser(UserCreationParam.builder()
                .username(dto.getUsername())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .enabled(true)
                .emailVerified(false)
                .credentials(List.of(Credential.builder()
                        .type("password")
                        .temporary(false)
                        .value(dto.getPassword())
                        .build()))
                .build(), adminAccessToken());
        } catch (FeignException.Conflict ex) {
            throw new ConflictException("Username or email already exists");
        }

        String userId = extractUserId(creationResponse);
        log.info("UserId {}", userId);

        User user = new User();
        user.setUserId(userId);
        // user.setUserId(UUID.randomUUID().toString());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDob(dto.getDob());
        user.setName(dto.getName());

        user = userRepository.save(user);
        return toDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return toDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(Long id, CreateUserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDob(dto.getDob());
        user.setName(dto.getName());

        return toDTO(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public TokenExchangeResponse login(LoginRequestDTO dto) {
        try {
            return identityClient.exchangeUserToken(UserTokenExchangeParam.builder()
                    .grant_type("password")
                    .client_secret(clientSecret)
                    .client_id(clientId)
                    .scope("openid")
                    .username(dto.getUsername())
                    .password(dto.getPassword())
                    .build());
        } catch (FeignException.Unauthorized | FeignException.BadRequest ex) {
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    @Override 
    public TokenExchangeResponse refreshToken(RefreshTokenRequestDTO dto) {
        try {
            return identityClient.exchangeUserToken(UserTokenExchangeParam.builder()
                    .grant_type("refresh_token")
                    .client_secret(clientSecret)
                    .client_id(clientId)
                    .refresh_token(dto.getRefreshToken())
                    .scope("openid")
                    .build());
        } catch (FeignException.Unauthorized | FeignException.BadRequest ex) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }

    @Override
    public UserResponseDTO getCurrentUser(String keycloakUserId) {
        User user = userRepository.findByUserId(keycloakUserId)
                .orElseThrow(() -> new NotFoundException("User not found with userId: " + keycloakUserId));
        return toDTO(user);
    }

    @Override
    public List<RoleRepresentation> getAssignableRoles() {
        return identityClient.getRealmRoles(adminAccessToken()).stream()
                .filter(role -> ASSIGNABLE_ROLES.contains(role.getName()))
                .toList();
    }

    @Override
    public List<RoleRepresentation> getUserRoles(Long id) {
        return fetchUserRoles(findKeycloakUserId(id), adminAccessToken());
    }

    @Override
    public List<RoleRepresentation> assignRoles(Long id, RoleAssignmentRequestDTO dto) {
        String keycloakUserId = findKeycloakUserId(id);
        String token = adminAccessToken();
        identityClient.addUserRealmRoles(keycloakUserId, resolveRoles(dto.getRoles(), token), token);
        return fetchUserRoles(keycloakUserId, token);
    }

    @Override
    public List<RoleRepresentation> removeRoles(Long id, RoleAssignmentRequestDTO dto) {
        String keycloakUserId = findKeycloakUserId(id);
        String token = adminAccessToken();
        identityClient.removeUserRealmRoles(keycloakUserId, resolveRoles(dto.getRoles(), token), token);
        return fetchUserRoles(keycloakUserId, token);
    }

    /** Access token của service account (client_credentials) để gọi Keycloak Admin API. */
    private String adminAccessToken() {
        var token = identityClient.exchangeClientToken(TokenExchangeParam.builder()
                .grant_type("client_credentials")
                .client_secret(clientSecret)
                .client_id(clientId)
                .scope("openid")
                .build());
        return "Bearer " + token.getAccessToken();
    }

    private String findKeycloakUserId(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return user.getUserId();
    }

    private List<RoleRepresentation> fetchUserRoles(String keycloakUserId, String token) {
        return identityClient.getUserRealmRoles(keycloakUserId, token).stream()
                .filter(role -> ASSIGNABLE_ROLES.contains(role.getName()))
                .toList();
    }

    /** Đổi tên role -> RoleRepresentation (Admin API cần cả id). */
    private List<RoleRepresentation> resolveRoles(List<String> roleNames, String token) {
        List<String> invalid = roleNames.stream().filter(name -> !ASSIGNABLE_ROLES.contains(name)).toList();
        if (!invalid.isEmpty()) {
            throw new BadRequestException("Invalid roles: " + invalid + ". Allowed roles: " + ASSIGNABLE_ROLES);
        }
        return identityClient.getRealmRoles(token).stream()
                .filter(role -> roleNames.contains(role.getName()))
                .toList();
    }

    private UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dob(user.getDob())
                .name(user.getName())
                .id(user.getId())
                .build();
    }

    private String extractUserId(ResponseEntity<?> response) {
        List<String> locations = response.getHeaders().get("Location");
        if (locations == null || locations.isEmpty()) {
            throw new IllegalStateException("Location header is missing in the response");
        }

        String location = locations.get(0);
        String[] splitedStr = location.split("/");
        return splitedStr[splitedStr.length - 1];
    }
}
