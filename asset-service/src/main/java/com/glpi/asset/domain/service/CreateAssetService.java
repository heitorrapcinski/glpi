package com.glpi.asset.domain.service;

import com.glpi.asset.domain.model.Asset;
import com.glpi.asset.domain.port.in.CreateAssetCommand;
import com.glpi.asset.domain.port.in.CreateAssetUseCase;
import com.glpi.asset.domain.port.out.AssetRepository;
import com.glpi.asset.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Domain service implementing CreateAssetUseCase.
 * Requirements: 12.5
 */
@Service
public class CreateAssetService implements CreateAssetUseCase {

    private final AssetRepository assetRepository;
    private final EventPublisherPort eventPublisher;

    public CreateAssetService(AssetRepository assetRepository, EventPublisherPort eventPublisher) {
        this.assetRepository = assetRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Asset createAsset(CreateAssetCommand cmd) {
        if (cmd.name() == null || cmd.name().isBlank()) {
            throw new IllegalArgumentException("Asset name is required");
        }
        if (cmd.entityId() == null || cmd.entityId().isBlank()) {
            throw new IllegalArgumentException("Entity ID is required");
        }
        if (cmd.assetType() == null) {
            throw new IllegalArgumentException("Asset type is required");
        }

        Instant now = Instant.now();

        Asset asset = new Asset();
        asset.setId(UUID.randomUUID().toString());
        asset.setAssetType(cmd.assetType());
        asset.setName(cmd.name());
        asset.setEntityId(cmd.entityId());
        asset.setSerial(cmd.serial());
        asset.setOtherSerial(cmd.otherSerial());
        asset.setStateId(cmd.stateId());
        asset.setLocationId(cmd.locationId());
        asset.setUserId(cmd.userId());
        asset.setGroupId(cmd.groupId());
        asset.setManufacturerId(cmd.manufacturerId());
        asset.setModelId(cmd.modelId());
        asset.setDeleted(false);
        asset.setNetworkPorts(new ArrayList<>());
        asset.setInfocom(cmd.infocom());
        asset.setContractIds(new ArrayList<>());
        asset.setComputerDetails(cmd.computerDetails());
        asset.setCreatedAt(now);
        asset.setUpdatedAt(now);

        Asset saved = assetRepository.save(asset);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "AssetCreated",
                saved.getId(),
                "Asset",
                now,
                1,
                saved
        ));

        return saved;
    }
}
