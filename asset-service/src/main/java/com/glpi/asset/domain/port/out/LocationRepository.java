package com.glpi.asset.domain.port.out;

import com.glpi.asset.domain.model.Location;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for locations.
 * Requirements: 12.4
 */
public interface LocationRepository {

    Optional<Location> findById(String id);

    Location save(Location location);

    List<Location> findAll();

    long count();
}
