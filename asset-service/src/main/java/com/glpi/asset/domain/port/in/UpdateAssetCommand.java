package com.glpi.asset.domain.port.in;

import com.glpi.asset.domain.model.ComputerDetails;
import com.glpi.asset.domain.model.Infocom;

/**
 * Command to update an existing asset.
 * Requirements: 12.6
 */
public record UpdateAssetCommand(
        String id,
        String name,
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
