package com.glpi.change.domain.port.out;

import com.glpi.change.domain.model.Change;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for Change aggregate.
 * Requirements: 22.5
 */
public interface ChangeRepository {

    Optional<Change> findById(String id);

    Change save(Change change);

    void delete(String id);

    List<Change> findAll(int page, int size);

    long countAll();
}
