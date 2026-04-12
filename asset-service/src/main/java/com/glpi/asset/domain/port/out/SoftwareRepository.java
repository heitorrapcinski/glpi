package com.glpi.asset.domain.port.out;

import com.glpi.asset.domain.model.Software;

import java.util.Optional;

/**
 * Driven port — persistence contract for Software entities.
 * Requirements: 13.1
 */
public interface SoftwareRepository {

    Optional<Software> findById(String id);

    Software save(Software software);
}
