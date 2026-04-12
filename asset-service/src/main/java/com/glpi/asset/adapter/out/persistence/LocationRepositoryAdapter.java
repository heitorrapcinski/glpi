package com.glpi.asset.adapter.out.persistence;

import com.glpi.asset.domain.model.Location;
import com.glpi.asset.domain.port.out.LocationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing LocationRepository.
 */
@Component
public class LocationRepositoryAdapter implements LocationRepository {

    private final MongoLocationRepository mongo;

    public LocationRepositoryAdapter(MongoLocationRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Location> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Location save(Location location) {
        return toDomain(mongo.save(toDocument(location)));
    }

    @Override
    public List<Location> findAll() {
        return mongo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return mongo.count();
    }

    private Location toDomain(LocationDocument doc) {
        Location l = new Location();
        l.setId(doc.getId());
        l.setName(doc.getName());
        l.setParentId(doc.getParentId());
        l.setLevel(doc.getLevel());
        l.setCompleteName(doc.getCompleteName());
        l.setCreatedAt(doc.getCreatedAt());
        l.setUpdatedAt(doc.getUpdatedAt());
        return l;
    }

    private LocationDocument toDocument(Location l) {
        LocationDocument doc = new LocationDocument();
        doc.setId(l.getId());
        doc.setName(l.getName());
        doc.setParentId(l.getParentId());
        doc.setLevel(l.getLevel());
        doc.setCompleteName(l.getCompleteName());
        doc.setCreatedAt(l.getCreatedAt());
        doc.setUpdatedAt(l.getUpdatedAt());
        return doc;
    }
}
