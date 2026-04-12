package com.glpi.asset.adapter.out.persistence;

import com.glpi.asset.domain.model.AssetState;
import com.glpi.asset.domain.port.out.AssetStateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing AssetStateRepository.
 */
@Component
public class AssetStateRepositoryAdapter implements AssetStateRepository {

    private final MongoAssetStateRepository mongo;

    public AssetStateRepositoryAdapter(MongoAssetStateRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<AssetState> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public AssetState save(AssetState state) {
        return toDomain(mongo.save(toDocument(state)));
    }

    @Override
    public List<AssetState> findAll() {
        return mongo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return mongo.count();
    }

    private AssetState toDomain(AssetStateDocument doc) {
        AssetState s = new AssetState();
        s.setId(doc.getId());
        s.setName(doc.getName());
        s.setCreatedAt(doc.getCreatedAt());
        s.setUpdatedAt(doc.getUpdatedAt());
        return s;
    }

    private AssetStateDocument toDocument(AssetState s) {
        AssetStateDocument doc = new AssetStateDocument();
        doc.setId(s.getId());
        doc.setName(s.getName());
        doc.setCreatedAt(s.getCreatedAt());
        doc.setUpdatedAt(s.getUpdatedAt());
        return doc;
    }
}
