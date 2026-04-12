package com.glpi.identity.domain.service;

import com.glpi.identity.domain.model.EntityHasChildrenException;
import com.glpi.identity.domain.model.EntityNotFoundException;
import com.glpi.identity.domain.port.in.DeleteEntityUseCase;
import com.glpi.identity.domain.port.out.EntityRepository;
import org.springframework.stereotype.Service;

/**
 * Domain service implementing DeleteEntityUseCase.
 * Rejects deletion if the entity has child entities.
 */
@Service
public class DeleteEntityService implements DeleteEntityUseCase {

    private final EntityRepository entityRepository;

    public DeleteEntityService(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    @Override
    public void deleteEntity(String entityId) {
        // Verify entity exists
        entityRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException(entityId));

        // Reject if entity has children
        if (!entityRepository.findChildren(entityId).isEmpty()) {
            throw new EntityHasChildrenException(entityId);
        }

        entityRepository.delete(entityId);
    }
}
