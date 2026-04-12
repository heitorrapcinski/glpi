package com.glpi.identity.domain.port.out;

import com.glpi.identity.domain.model.Group;

import java.util.List;
import java.util.Optional;

/**
 * Driven port: persistence contract for the Group aggregate.
 */
public interface GroupRepository {

    Optional<Group> findById(String id);

    Group save(Group group);

    void delete(String id);

    List<Group> findAll();

    List<Group> findByEntityId(String entityId);
}
