package com.glpi.asset.domain.port.out;

import com.glpi.asset.domain.model.ItemSoftwareVersion;

import java.util.List;

/**
 * Driven port — persistence contract for software installation records.
 * Requirements: 13.3
 */
public interface ItemSoftwareVersionRepository {

    ItemSoftwareVersion save(ItemSoftwareVersion item);

    List<ItemSoftwareVersion> findByAssetId(String assetId);

    List<ItemSoftwareVersion> findByLicenseId(String licenseId);

    long countByLicenseId(String licenseId);
}
