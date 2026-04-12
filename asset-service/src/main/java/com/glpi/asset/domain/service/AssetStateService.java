package com.glpi.asset.domain.service;

import com.glpi.asset.domain.model.AssetState;
import com.glpi.asset.domain.port.out.AssetStateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain service managing configurable asset states lifecycle.
 * Requirements: 12.3
 */
@Service
public class AssetStateService {

    private final AssetStateRepository assetStateRepository;

    public AssetStateService(AssetStateRepository assetStateRepository) {
        this.assetStateRepository = assetStateRepository;
    }

    public AssetState createState(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("State name is required");
        }
        Instant now = Instant.now();
        AssetState state = new AssetState();
        state.setId(UUID.randomUUID().toString());
        state.setName(name);
        state.setCreatedAt(now);
        state.setUpdatedAt(now);
        return assetStateRepository.save(state);
    }

    public List<AssetState> findAll() {
        return assetStateRepository.findAll();
    }
}
