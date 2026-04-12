package com.glpi.knowledge.domain.port.out;

import com.glpi.knowledge.domain.model.KnowbaseItem;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for KnowbaseItem aggregate.
 * Requirements: 17.1
 */
public interface KnowbaseItemRepository {

    Optional<KnowbaseItem> findById(String id);

    KnowbaseItem save(KnowbaseItem item);

    void deleteById(String id);

    List<KnowbaseItem> findAll(int page, int size);

    long countAll();

    /**
     * Atomically increment the viewCount by 1.
     * Requirements: 17.9
     */
    void incrementViewCount(String id);

    /**
     * Full-text search on title and answer fields.
     * Requirements: 17.12
     */
    List<KnowbaseItem> searchByText(String query, int page, int size);

    long countByTextSearch(String query);
}
