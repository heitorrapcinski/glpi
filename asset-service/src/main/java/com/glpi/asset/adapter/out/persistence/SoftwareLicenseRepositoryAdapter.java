package com.glpi.asset.adapter.out.persistence;

import com.glpi.asset.domain.model.SoftwareLicense;
import com.glpi.asset.domain.port.out.SoftwareLicenseRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing SoftwareLicenseRepository.
 */
@Component
public class SoftwareLicenseRepositoryAdapter implements SoftwareLicenseRepository {

    private final MongoSoftwareLicenseRepository mongo;

    public SoftwareLicenseRepositoryAdapter(MongoSoftwareLicenseRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<SoftwareLicense> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public SoftwareLicense save(SoftwareLicense license) {
        return toDomain(mongo.save(toDocument(license)));
    }

    @Override
    public List<SoftwareLicense> findAll(int page, int size) {
        return mongo.findAllBy(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return mongo.count();
    }

    private SoftwareLicense toDomain(SoftwareLicenseDocument doc) {
        SoftwareLicense l = new SoftwareLicense();
        l.setId(doc.getId());
        l.setName(doc.getName());
        l.setSoftwareId(doc.getSoftwareId());
        l.setLicenseType(doc.getLicenseType());
        l.setSerial(doc.getSerial());
        l.setNumberOfSeats(doc.getNumberOfSeats());
        l.setExpiryDate(doc.getExpiryDate());
        l.setEntityId(doc.getEntityId());
        l.setCreatedAt(doc.getCreatedAt());
        l.setUpdatedAt(doc.getUpdatedAt());
        return l;
    }

    private SoftwareLicenseDocument toDocument(SoftwareLicense l) {
        SoftwareLicenseDocument doc = new SoftwareLicenseDocument();
        doc.setId(l.getId());
        doc.setName(l.getName());
        doc.setSoftwareId(l.getSoftwareId());
        doc.setLicenseType(l.getLicenseType());
        doc.setSerial(l.getSerial());
        doc.setNumberOfSeats(l.getNumberOfSeats());
        doc.setExpiryDate(l.getExpiryDate());
        doc.setEntityId(l.getEntityId());
        doc.setCreatedAt(l.getCreatedAt());
        doc.setUpdatedAt(l.getUpdatedAt());
        return doc;
    }
}
