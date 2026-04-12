package com.glpi.asset.adapter.out.persistence;

import com.glpi.asset.domain.model.Asset;
import com.glpi.asset.domain.model.AssetType;
import com.glpi.asset.domain.port.out.AssetRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing AssetRepository driven port.
 * Requirements: 22.6
 */
@Component
public class AssetRepositoryAdapter implements AssetRepository {

    private final MongoAssetRepository mongo;

    public AssetRepositoryAdapter(MongoAssetRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Asset> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Asset save(Asset asset) {
        return toDomain(mongo.save(toDocument(asset)));
    }

    @Override
    public void delete(String id) {
        mongo.deleteById(id);
    }

    @Override
    public List<Asset> findByType(AssetType type, int page, int size) {
        return mongo.findByAssetTypeAndIsDeletedFalse(type, PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByType(AssetType type) {
        return mongo.countByAssetTypeAndIsDeletedFalse(type);
    }

    @Override
    public List<Asset> findAllNotDeleted(int page, int size) {
        return mongo.findByIsDeletedFalse(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAllNotDeleted() {
        return mongo.countByIsDeletedFalse();
    }

    private Asset toDomain(AssetDocument doc) {
        Asset a = new Asset();
        a.setId(doc.getId());
        a.setAssetType(doc.getAssetType());
        a.setName(doc.getName());
        a.setEntityId(doc.getEntityId());
        a.setSerial(doc.getSerial());
        a.setOtherSerial(doc.getOtherSerial());
        a.setStateId(doc.getStateId());
        a.setLocationId(doc.getLocationId());
        a.setUserId(doc.getUserId());
        a.setGroupId(doc.getGroupId());
        a.setManufacturerId(doc.getManufacturerId());
        a.setModelId(doc.getModelId());
        a.setDeleted(doc.isDeleted());
        a.setNetworkPorts(doc.getNetworkPorts());
        a.setInfocom(doc.getInfocom());
        a.setContractIds(doc.getContractIds());
        a.setComputerDetails(doc.getComputerDetails());
        a.setCreatedAt(doc.getCreatedAt());
        a.setUpdatedAt(doc.getUpdatedAt());
        return a;
    }

    private AssetDocument toDocument(Asset a) {
        AssetDocument doc = new AssetDocument();
        doc.setId(a.getId());
        doc.setAssetType(a.getAssetType());
        doc.setName(a.getName());
        doc.setEntityId(a.getEntityId());
        doc.setSerial(a.getSerial());
        doc.setOtherSerial(a.getOtherSerial());
        doc.setStateId(a.getStateId());
        doc.setLocationId(a.getLocationId());
        doc.setUserId(a.getUserId());
        doc.setGroupId(a.getGroupId());
        doc.setManufacturerId(a.getManufacturerId());
        doc.setModelId(a.getModelId());
        doc.setDeleted(a.isDeleted());
        doc.setNetworkPorts(a.getNetworkPorts());
        doc.setInfocom(a.getInfocom());
        doc.setContractIds(a.getContractIds());
        doc.setComputerDetails(a.getComputerDetails());
        doc.setCreatedAt(a.getCreatedAt());
        doc.setUpdatedAt(a.getUpdatedAt());
        return doc;
    }
}
