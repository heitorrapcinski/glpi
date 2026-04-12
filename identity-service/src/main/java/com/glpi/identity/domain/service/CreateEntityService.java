package com.glpi.identity.domain.service;

import com.glpi.identity.domain.model.DuplicateEntityNameException;
import com.glpi.identity.domain.model.Entity;
import com.glpi.identity.domain.model.EntityConfig;
import com.glpi.identity.domain.model.EntityNotFoundException;
import com.glpi.identity.domain.port.in.CreateEntityCommand;
import com.glpi.identity.domain.port.in.CreateEntityUseCase;
import com.glpi.identity.domain.port.in.EntityResponse;
import com.glpi.identity.domain.port.out.EntityRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain service implementing CreateEntityUseCase.
 * Enforces unique name within parent and computes completeName and level.
 */
@Service
public class CreateEntityService implements CreateEntityUseCase {

    private final EntityRepository entityRepository;

    public CreateEntityService(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    @Override
    public EntityResponse createEntity(CreateEntityCommand command) {
        String parentId = command.parentId();

        // 1. Enforce unique name within parent
        if (entityRepository.existsByNameAndParentId(command.name(), parentId)) {
            throw new DuplicateEntityNameException(command.name(), parentId);
        }

        // 2. Compute level and completeName by traversing ancestors
        int level;
        String completeName;

        if (parentId == null) {
            // Root entity
            level = 1;
            completeName = command.name();
        } else {
            Entity parent = entityRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException(parentId));
            level = parent.getLevel() + 1;
            completeName = buildCompleteName(parentId, command.name());
        }

        // 3. Build and persist the entity
        String entityId = UUID.randomUUID().toString();
        EntityConfig config = command.config() != null ? command.config() : new EntityConfig();

        Entity entity = new Entity(entityId, command.name(), parentId, level, completeName, config);
        Entity saved = entityRepository.save(entity);

        return toResponse(saved);
    }

    /**
     * Traverses ancestors to build the full path name (e.g. "Root > Parent > Child").
     */
    private String buildCompleteName(String parentId, String childName) {
        List<String> parts = new ArrayList<>();
        parts.add(childName);

        String currentParentId = parentId;
        while (currentParentId != null) {
            Entity ancestor = entityRepository.findById(currentParentId).orElse(null);
            if (ancestor == null) break;
            parts.add(0, ancestor.getName());
            currentParentId = ancestor.getParentId();
        }

        return String.join(" > ", parts);
    }

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
}
