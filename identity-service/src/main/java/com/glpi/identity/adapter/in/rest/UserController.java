package com.glpi.identity.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.identity.domain.model.AuthType;
import com.glpi.identity.domain.model.Email;
import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.model.UserNotFoundException;
import com.glpi.identity.domain.port.in.*;
import com.glpi.identity.domain.port.out.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for user management endpoints.
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User lifecycle management")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final PurgeUserUseCase purgeUserUseCase;
    private final GenerateApiTokenUseCase generateApiTokenUseCase;
    private final ImpersonateUserUseCase impersonateUserUseCase;
    private final UserRepository userRepository;

    public UserController(CreateUserUseCase createUserUseCase,
                          DeactivateUserUseCase deactivateUserUseCase,
                          PurgeUserUseCase purgeUserUseCase,
                          GenerateApiTokenUseCase generateApiTokenUseCase,
                          ImpersonateUserUseCase impersonateUserUseCase,
                          UserRepository userRepository) {
        this.createUserUseCase = createUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.purgeUserUseCase = purgeUserUseCase;
        this.generateApiTokenUseCase = generateApiTokenUseCase;
        this.impersonateUserUseCase = impersonateUserUseCase;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Operation(summary = "Create user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        List<Email> emails = request.emails() != null
                ? request.emails().stream().map(e -> new Email(e.email(), e.isDefault())).toList()
                : List.of();

        CreateUserCommand command = new CreateUserCommand(
                request.username(),
                request.password(),
                request.authType() != null ? request.authType() : AuthType.DB_GLPI,
                emails,
                request.entityId(),
                request.profileId()
        );
        UserResponse response = createUserUseCase.createUser(command);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @Operation(summary = "List users (paginated)")
    public ResponseEntity<PagedResponse<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(value = "expand_dropdowns", required = false) Boolean expandDropdowns) {
        int clampedSize = Math.min(Math.max(size, 1), 500);

        List<User> all = userRepository.findAll();
        int total = all.size();
        int fromIndex = Math.min(page * clampedSize, total);
        int toIndex = Math.min(fromIndex + clampedSize, total);
        List<UserResponse> content = all.subList(fromIndex, toIndex).stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(PagedResponse.of(content, total, page, clampedSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
                                                    @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.language() != null) user.setLanguage(request.language());
        if (request.entityId() != null) user.setEntityId(request.entityId());
        if (request.profileId() != null) user.setProfileId(request.profileId());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate user")
    public ResponseEntity<Void> deactivateUser(@PathVariable String id) {
        deactivateUserUseCase.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/purge")
    @Operation(summary = "Purge user (hard delete)")
    public ResponseEntity<Void> purgeUser(@PathVariable String id) {
        purgeUserUseCase.purgeUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/api-token")
    @Operation(summary = "Generate personal API token")
    public ResponseEntity<Map<String, String>> generateApiToken(@PathVariable String id) {
        String token = generateApiTokenUseCase.generateApiToken(id);
        return ResponseEntity.ok(Map.of("apiToken", token));
    }

    @PostMapping("/{id}/impersonate")
    @Operation(summary = "Impersonate user (requires IMPERSONATE right)")
    public ResponseEntity<AuthResponse> impersonate(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String requestingUserId) {
        AuthResponse response = impersonateUserUseCase.impersonate(
                new ImpersonateCommand(requestingUserId, id));
        return ResponseEntity.ok(response);
    }

    // --- Mapping ---

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getAuthType(),
                user.getAuthSourceId(),
                user.getEmails(),
                user.isActive(),
                user.isDeleted(),
                user.getEntityId(),
                user.getProfileId(),
                user.getLanguage(),
                user.isTwoFactorEnabled(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // --- Request DTOs ---

    public record CreateUserRequest(
            @NotBlank String username,
            @NotBlank String password,
            AuthType authType,
            List<EmailDto> emails,
            String entityId,
            String profileId
    ) {}

    public record EmailDto(String email, boolean isDefault) {}

    public record UpdateUserRequest(String language, String entityId, String profileId) {}
}
