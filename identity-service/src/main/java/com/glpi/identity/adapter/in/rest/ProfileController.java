package com.glpi.identity.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.identity.domain.model.Profile;
import com.glpi.identity.domain.model.ProfileNotFoundException;
import com.glpi.identity.domain.model.TicketStatusMatrix;
import com.glpi.identity.domain.port.in.AssignProfileCommand;
import com.glpi.identity.domain.port.in.AssignProfileUseCase;
import com.glpi.identity.domain.port.out.ProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for profile (RBAC role) management.
 */
@RestController
@RequestMapping("/profiles")
@Tag(name = "Profiles", description = "RBAC profile management")
public class ProfileController {

    private final ProfileRepository profileRepository;
    private final AssignProfileUseCase assignProfileUseCase;

    public ProfileController(ProfileRepository profileRepository,
                             AssignProfileUseCase assignProfileUseCase) {
        this.profileRepository = profileRepository;
        this.assignProfileUseCase = assignProfileUseCase;
    }

    @GetMapping
    @Operation(summary = "List profiles (paginated)")
    public ResponseEntity<PagedResponse<ProfileResponse>> listProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Profile> all = profileRepository.findAll();
        int total = all.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<ProfileResponse> content = all.subList(fromIndex, toIndex).stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(PagedResponse.of(content, total, page, size));
    }

    @PostMapping
    @Operation(summary = "Create profile")
    public ResponseEntity<ProfileResponse> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        Profile profile = new Profile(
                UUID.randomUUID().toString(),
                request.name(),
                request.interface_() != null ? request.interface_() : "central",
                request.isDefault() != null && request.isDefault(),
                request.twoFactorEnforced() != null && request.twoFactorEnforced(),
                request.rights() != null ? request.rights() : Map.of(),
                new TicketStatusMatrix()
        );
        Profile saved = profileRepository.save(profile);
        return ResponseEntity.status(201).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get profile by ID")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable String id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));
        return ResponseEntity.ok(toResponse(profile));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update profile")
    public ResponseEntity<ProfileResponse> updateProfile(@PathVariable String id,
                                                          @RequestBody UpdateProfileRequest request) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));

        if (request.name() != null) profile.setName(request.name());
        if (request.rights() != null) profile.setRights(request.rights());
        if (request.twoFactorEnforced() != null) profile.setTwoFactorEnforced(request.twoFactorEnforced());

        Profile saved = profileRepository.save(profile);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete profile")
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        profileRepository.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign profile to user in entity")
    public ResponseEntity<Void> assignProfile(@PathVariable String id,
                                               @Valid @RequestBody AssignRequest request) {
        assignProfileUseCase.assignProfile(
                new AssignProfileCommand(request.userId(), id, request.entityId()));
        return ResponseEntity.ok().build();
    }

    // --- Mapping ---

    private ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getInterface(),
                profile.isDefault(),
                profile.isTwoFactorEnforced(),
                profile.getRights(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    // --- DTOs ---

    public record ProfileResponse(
            String id,
            String name,
            String interface_,
            boolean isDefault,
            boolean twoFactorEnforced,
            Map<String, Integer> rights,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record CreateProfileRequest(
            @NotBlank String name,
            String interface_,
            Boolean isDefault,
            Boolean twoFactorEnforced,
            Map<String, Integer> rights
    ) {}

    public record UpdateProfileRequest(
            String name,
            Map<String, Integer> rights,
            Boolean twoFactorEnforced
    ) {}

    public record AssignRequest(
            @NotBlank String userId,
            @NotBlank String entityId
    ) {}
}
