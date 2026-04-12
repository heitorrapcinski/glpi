package com.glpi.asset.domain.service;

import com.glpi.asset.domain.model.Location;
import com.glpi.asset.domain.port.out.LocationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain service managing hierarchical location tree.
 * Requirements: 12.4
 */
@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location createLocation(String name, String parentId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name is required");
        }

        Instant now = Instant.now();
        Location location = new Location();
        location.setId(UUID.randomUUID().toString());
        location.setName(name);
        location.setParentId(parentId);
        location.setCreatedAt(now);
        location.setUpdatedAt(now);

        // Compute completeName from parent chain
        if (parentId != null) {
            Location parent = locationRepository.findById(parentId).orElse(null);
            if (parent != null) {
                location.setLevel(parent.getLevel() + 1);
                location.setCompleteName(parent.getCompleteName() + " > " + name);
            } else {
                location.setLevel(1);
                location.setCompleteName(name);
            }
        } else {
            location.setLevel(1);
            location.setCompleteName(name);
        }

        return locationRepository.save(location);
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }
}
