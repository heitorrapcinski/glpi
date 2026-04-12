package com.glpi.asset.adapter.out.persistence;

import com.glpi.asset.domain.model.Software;
import com.glpi.asset.domain.port.out.SoftwareRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * MongoDB adapter implementing SoftwareRepository.
 */
@Component
public class SoftwareRepositoryAdapter implements SoftwareRepository {

    private final MongoSoftwareRepository mongo;

    public SoftwareRepositoryAdapter(MongoSoftwareRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Software> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Software save(Software software) {
        return toDomain(mongo.save(toDocument(software)));
    }

    private Software toDomain(SoftwareDocument doc) {
        Software s = new Software();
        s.setId(doc.getId());
        s.setName(doc.getName());
        s.setManufacturer(doc.getManufacturer());
        s.setCategory(doc.getCategory());
        s.setVersions(doc.getVersions());
        s.setCreatedAt(doc.getCreatedAt());
        s.setUpdatedAt(doc.getUpdatedAt());
        return s;
    }

    private SoftwareDocument toDocument(Software s) {
        SoftwareDocument doc = new SoftwareDocument();
        doc.setId(s.getId());
        doc.setName(s.getName());
        doc.setManufacturer(s.getManufacturer());
        doc.setCategory(s.getCategory());
        doc.setVersions(s.getVersions());
        doc.setCreatedAt(s.getCreatedAt());
        doc.setUpdatedAt(s.getUpdatedAt());
        return doc;
    }
}
