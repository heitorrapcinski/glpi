package com.glpi.knowledge.domain.port.out;

import com.glpi.knowledge.domain.model.KnowbaseItemCategory;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for KnowbaseItemCategory aggregate.
 * Requirements: 17.2
 */
public interface KnowbaseItemCategoryRepository {

    Optional<KnowbaseItemCategory> findById(String id);

    KnowbaseItemCategory save(KnowbaseItemCategory category);

    List<KnowbaseItemCategory> findAll(int page, int size);

    long countAll();
}
