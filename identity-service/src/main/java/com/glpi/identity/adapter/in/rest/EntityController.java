package com.glpi.identity.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.identity.domain.model.Entity;
import com.glpi.identity.domain.model.EntityConfig;
import com.glpi.identity.domain.model.EntityNotFoundException;
import com.glpi.identity.domain.port.in.*;
import com.glpi.identity.domain.port.out.EntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for entity hierarchy management.
 */
@RestController
@RequestMapping("/entities")
@Tag(name = "Entities", description = "Organizational unit hierarchy management")
public class EntityController {

    private final CreateEntityUseCase createEntityUseCase;
    private final DeleteEntityUseCase deleteEntityUseCase;
    private final EntityRepository entityRepository;

    public EntityController(CreateEntityUseCase createEntityUseCase,
                            DeleteEntityUseCase deleteEntityUseCase,
                            EntityRepository entityRepository) {
        this.createEntityUseCase = createEntityUseCase;
        this.deleteEntityUseCase = deleteEntityUseCase;
        this.entityRepository = entityRepository;
    }

    @GetMapping
    @Operation(summary = "List entities (paginated)")
    public ResponseEntity<PagedResponse<EntityResponse>> listEntities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Entity> all = entityRepository.findAll();
        int total = all.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<EntityResponse> content = all.subList(fromIndex, toIndex).stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(PagedResponse.of(content, total, page, size));
    }

    @PostMapping
    @Operation(summary = "Create entity")
    public ResponseEntity<EntityResponse> createEntity(@Valid @RequestBody CreateEntityRequest request) {
        EntityResponse response = createEntityUseCase.createEntity(
                new CreateEntityCommand(request.name(), request.parentId(), request.config()));
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID")
    public ResponseEntity<EntityResponse> getEntity(@PathVariable String id) {
        Entity entity = entityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        return ResponseEntity.ok(toResponse(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update entity")
    public ResponseEntity<EntityResponse> updateEntity(@PathVariable String id,
                                                        @RequestBody UpdateEntityRequest request) {
        Entity entity = entityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));

        if (request.name() != null) entity.setName(request.name());
        if (request.config() != null) entity.setConfig(request.config());

        Entity saved = entityRepository.save(entity);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete entity")
    public ResponseEntity<Void> deleteEntity(@PathVariable String id) {
        deleteEntityUseCase.deleteEntity(id);
        return ResponseEntity.noContent().build();
    }

    // --- Mapping ---

    private EntityResponse toResponse(Entity entity) {
        return new EntityResponse(
                entity.getId(),
                entity.getName(),
                entity.getParentId(),
                entity.getLevel(),
                entity.getCompleteName(),
                entity.getConfig(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // --- Request DTOs ---

    public record CreateEntityRequest(
            @NotBlank String name,
            String parentId,
            EntityConfig config
    ) {}

    public record UpdateEntityRequest(String name, EntityConfig config) {}
}
