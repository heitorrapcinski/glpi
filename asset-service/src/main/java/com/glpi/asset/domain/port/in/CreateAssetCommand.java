package com.glpi.asset.domain.port.in;

import com.glpi.asset.domain.model.AssetType;
import com.glpi.asset.domain.model.ComputerDetails;
import com.glpi.asset.domain.model.Infocom;

/**
 * Command to create a new asset.
 * Requirements: 12.5
 */
public record CreateAssetCommand(
        AssetType assetType,
        String name,
        String entityId,
        String serial,
        String otherSerial,
        String stateId,
        String locationId,
        String userId,
        String groupId,
        String manufacturerId,
        String modelId,
        Infocom infocom,
        ComputerDetails computerDetails
) {}
