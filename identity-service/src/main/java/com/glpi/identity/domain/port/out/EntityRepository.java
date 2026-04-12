package com.glpi.identity.domain.port.out;

import com.glpi.identity.domain.model.Entity;

import java.util.List;
import java.util.Optional;

/**
 * Driven port: persistence contract for the Entity aggregate.
 */
public interface EntityRepository {

    Optional<Entity> findById(String id);

    List<Entity> findChildren(String parentId);

    Entity save(Entity entity);

    void delete(String id);

    boolean existsByNameAndParentId(String name, String parentId);

    List<Entity> findAll();
}
