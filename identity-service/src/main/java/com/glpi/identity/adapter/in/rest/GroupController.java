package com.glpi.identity.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.identity.domain.model.Group;
import com.glpi.identity.domain.model.GroupNotFoundException;
import com.glpi.identity.domain.port.out.GroupRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for group management.
 */
@RestController
@RequestMapping("/groups")
@Tag(name = "Groups", description = "User group management")
public class GroupController {

    private final GroupRepository groupRepository;

    public GroupController(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @GetMapping
    @Operation(summary = "List groups (paginated)")
    public ResponseEntity<PagedResponse<GroupResponse>> listGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(value = "expand_dropdowns", required = false) Boolean expandDropdowns) {
        int clampedSize = Math.min(Math.max(size, 1), 500);
        List<Group> all = groupRepository.findAll();
        int total = all.size();
        int fromIndex = Math.min(page * clampedSize, total);
        int toIndex = Math.min(fromIndex + clampedSize, total);
        List<GroupResponse> content = all.subList(fromIndex, toIndex).stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(PagedResponse.of(content, total, page, clampedSize));
    }

    @PostMapping
    @Operation(summary = "Create group")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        Group group = new Group(
                UUID.randomUUID().toString(),
                request.name(),
                request.entityId(),
                request.isRecursive() != null && request.isRecursive(),
                request.memberUserIds() != null ? request.memberUserIds() : List.of()
        );
        Group saved = groupRepository.save(group);
        return ResponseEntity.status(201).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable String id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));
        return ResponseEntity.ok(toResponse(group));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update group")
    public ResponseEntity<GroupResponse> updateGroup(@PathVariable String id,
                                                      @RequestBody UpdateGroupRequest request) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        if (request.name() != null) group.setName(request.name());
        if (request.memberUserIds() != null) group.setMemberUserIds(request.memberUserIds());
        if (request.isRecursive() != null) group.setRecursive(request.isRecursive());

        Group saved = groupRepository.save(group);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete group")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groupRepository.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Mapping ---

    private GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getEntityId(),
                group.isRecursive(),
                group.getMemberUserIds(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }

    // --- DTOs ---

    public record GroupResponse(
            String id,
            String name,
            String entityId,
            boolean isRecursive,
            List<String> memberUserIds,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record CreateGroupRequest(
            @NotBlank String name,
            String entityId,
            Boolean isRecursive,
            List<String> memberUserIds
    ) {}

    public record UpdateGroupRequest(
            String name,
            Boolean isRecursive,
            List<String> memberUserIds
    ) {}
}
