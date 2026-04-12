package com.glpi.asset.domain.port.out;

import com.glpi.asset.domain.model.SoftwareLicense;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for SoftwareLicense.
 * Requirements: 13.2
 */
public interface SoftwareLicenseRepository {

    Optional<SoftwareLicense> findById(String id);

    SoftwareLicense save(SoftwareLicense license);

    List<SoftwareLicense> findAll(int page, int size);

    long countAll();
}
