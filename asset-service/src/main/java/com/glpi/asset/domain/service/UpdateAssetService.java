package com.glpi.asset.domain.service;

import com.glpi.asset.domain.model.Asset;
import com.glpi.asset.domain.model.AssetNotFoundException;
import com.glpi.asset.domain.port.in.UpdateAssetCommand;
import com.glpi.asset.domain.port.in.UpdateAssetUseCase;
import com.glpi.asset.domain.port.out.AssetRepository;
import com.glpi.asset.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing UpdateAssetUseCase.
 * Requirements: 12.6
 */
@Service
public class UpdateAssetService implements UpdateAssetUseCase {

    private final AssetRepository assetRepository;
    private final EventPublisherPort eventPublisher;

    public UpdateAssetService(AssetRepository assetRepository, EventPublisherPort eventPublisher) {
        this.assetRepository = assetRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Asset updateAsset(UpdateAssetCommand cmd) {
        Asset asset = assetRepository.findById(cmd.id())
                .orElseThrow(() -> new AssetNotFoundException(cmd.id()));

        Instant now = Instant.now();

        if (cmd.name() != null) asset.setName(cmd.name());
        if (cmd.serial() != null) asset.setSerial(cmd.serial());
        if (cmd.otherSerial() != null) asset.setOtherSerial(cmd.otherSerial());
        if (cmd.stateId() != null) asset.setStateId(cmd.stateId());
        if (cmd.locationId() != null) asset.setLocationId(cmd.locationId());
        if (cmd.userId() != null) asset.setUserId(cmd.userId());
        if (cmd.groupId() != null) asset.setGroupId(cmd.groupId());
        if (cmd.manufacturerId() != null) asset.setManufacturerId(cmd.manufacturerId());
        if (cmd.modelId() != null) asset.setModelId(cmd.modelId());
        if (cmd.infocom() != null) asset.setInfocom(cmd.infocom());
        if (cmd.computerDetails() != null) asset.setComputerDetails(cmd.computerDetails());
        asset.setUpdatedAt(now);

        Asset saved = assetRepository.save(asset);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "AssetUpdated",
                saved.getId(),
                "Asset",
                now,
                1,
                saved
        ));

        return saved;
    }
}
