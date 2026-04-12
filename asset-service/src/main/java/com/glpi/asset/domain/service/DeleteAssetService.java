package com.glpi.asset.domain.service;

import com.glpi.asset.domain.model.Asset;
import com.glpi.asset.domain.model.AssetNotFoundException;
import com.glpi.asset.domain.port.in.DeleteAssetUseCase;
import com.glpi.asset.domain.port.out.AssetRepository;
import com.glpi.asset.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing DeleteAssetUseCase (soft delete).
 * Requirements: 12.7
 */
@Service
public class DeleteAssetService implements DeleteAssetUseCase {

    private final AssetRepository assetRepository;
    private final EventPublisherPort eventPublisher;

    public DeleteAssetService(AssetRepository assetRepository, EventPublisherPort eventPublisher) {
        this.assetRepository = assetRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void deleteAsset(String id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));

        Instant now = Instant.now();
        asset.setDeleted(true);
        asset.setUpdatedAt(now);
        assetRepository.save(asset);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "AssetDeleted",
                asset.getId(),
                "Asset",
                now,
                1,
                asset
        ));
    }
}
