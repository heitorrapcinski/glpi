package com.glpi.asset.config;

import com.glpi.asset.domain.model.AssetState;
import com.glpi.asset.domain.model.Location;
import com.glpi.asset.domain.port.out.AssetStateRepository;
import com.glpi.asset.domain.port.out.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Seeds default asset states and root location on startup when collections are empty.
 * Requirements: 29.7, 29.8, 29.12
 */
@Component
public class AssetSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AssetSeeder.class);

    private final AssetStateRepository assetStateRepository;
    private final LocationRepository locationRepository;

    public AssetSeeder(AssetStateRepository assetStateRepository,
                       LocationRepository locationRepository) {
        this.assetStateRepository = assetStateRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public void run(String... args) {
        seedAssetStates();
        seedRootLocation();
    }

    private void seedAssetStates() {
        if (assetStateRepository.count() > 0) {
            log.info("asset_states collection already populated, skipping seed");
            return;
        }

        Instant now = Instant.now();
        List<String> defaultStates = List.of("In Stock", "In Use", "Maintenance", "Retired", "Disposed");

        for (int i = 0; i < defaultStates.size(); i++) {
            AssetState state = new AssetState();
            state.setId(String.valueOf(i + 1));
            state.setName(defaultStates.get(i));
            state.setCreatedAt(now);
            state.setUpdatedAt(now);
            assetStateRepository.save(state);
        }

        log.info("Seeded asset_states collection with {} documents at {}", defaultStates.size(), now);
    }

    private void seedRootLocation() {
        if (locationRepository.count() > 0) {
            log.info("locations collection already populated, skipping seed");
            return;
        }

        Instant now = Instant.now();
        Location root = new Location();
        root.setId("0");
        root.setName("Root Location");
        root.setParentId(null);
        root.setLevel(1);
        root.setCompleteName("Root Location");
        root.setCreatedAt(now);
        root.setUpdatedAt(now);
        locationRepository.save(root);

        log.info("Seeded locations collection with root location at {}", now);
    }
}
