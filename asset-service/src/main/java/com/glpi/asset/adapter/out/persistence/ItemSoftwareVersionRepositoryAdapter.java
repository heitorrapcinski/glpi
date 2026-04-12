package com.glpi.asset.adapter.out.persistence;

import com.glpi.asset.domain.model.ItemSoftwareVersion;
import com.glpi.asset.domain.port.out.ItemSoftwareVersionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing ItemSoftwareVersionRepository.
 */
@Component
public class ItemSoftwareVersionRepositoryAdapter implements ItemSoftwareVersionRepository {

    private final MongoItemSoftwareVersionRepository mongo;

    public ItemSoftwareVersionRepositoryAdapter(MongoItemSoftwareVersionRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public ItemSoftwareVersion save(ItemSoftwareVersion item) {
        return toDomain(mongo.save(toDocument(item)));
    }

    @Override
    public List<ItemSoftwareVersion> findByAssetId(String assetId) {
        return mongo.findByAssetId(assetId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ItemSoftwareVersion> findByLicenseId(String licenseId) {
        return mongo.findByLicenseId(licenseId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByLicenseId(String licenseId) {
        return mongo.countByLicenseId(licenseId);
    }

    private ItemSoftwareVersion toDomain(ItemSoftwareVersionDocument doc) {
        ItemSoftwareVersion i = new ItemSoftwareVersion();
        i.setId(doc.getId());
        i.setAssetId(doc.getAssetId());
        i.setSoftwareId(doc.getSoftwareId());
        i.setVersion(doc.getVersion());
        i.setLicenseId(doc.getLicenseId());
        i.setInstalledAt(doc.getInstalledAt());
        return i;
    }

    private ItemSoftwareVersionDocument toDocument(ItemSoftwareVersion i) {
        ItemSoftwareVersionDocument doc = new ItemSoftwareVersionDocument();
        doc.setId(i.getId());
        doc.setAssetId(i.getAssetId());
        doc.setSoftwareId(i.getSoftwareId());
        doc.setVersion(i.getVersion());
        doc.setLicenseId(i.getLicenseId());
        doc.setInstalledAt(i.getInstalledAt());
        return doc;
    }
}
