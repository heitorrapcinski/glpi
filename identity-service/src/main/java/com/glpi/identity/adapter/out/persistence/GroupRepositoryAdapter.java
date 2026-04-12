package com.glpi.identity.adapter.out.persistence;

import com.glpi.identity.domain.model.Group;
import com.glpi.identity.domain.port.out.GroupRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing the GroupRepository driven port using MongoDB.
 */
@Component
public class GroupRepositoryAdapter implements GroupRepository {

    private final MongoGroupRepository mongoRepo;

    public GroupRepositoryAdapter(MongoGroupRepository mongoRepo) {
        this.mongoRepo = mongoRepo;
    }

    @Override
    public Optional<Group> findById(String id) {
        return mongoRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Group save(Group group) {
        GroupDocument doc = toDocument(group);
        GroupDocument saved = mongoRepo.save(doc);
        return toDomain(saved);
    }

    @Override
    public void delete(String id) {
        mongoRepo.deleteById(id);
    }

    @Override
    public List<Group> findAll() {
        return mongoRepo.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Group> findByEntityId(String entityId) {
        return mongoRepo.findByEntityId(entityId).stream()
                .map(this::toDomain)
                .toList();
    }

    // --- Mapping ---

    private Group toDomain(GroupDocument doc) {
        Group group = new Group(
                doc.getId(),
                doc.getName(),
                doc.getEntityId(),
                doc.isRecursive(),
                doc.getMemberUserIds()
        );
        group.setCreatedAt(doc.getCreatedAt());
        group.setUpdatedAt(doc.getUpdatedAt());
        return group;
    }

    private GroupDocument toDocument(Group group) {
        GroupDocument doc = new GroupDocument();
        doc.setId(group.getId());
        doc.setName(group.getName());
        doc.setEntityId(group.getEntityId());
        doc.setRecursive(group.isRecursive());
        doc.setMemberUserIds(group.getMemberUserIds());
        doc.setCreatedAt(group.getCreatedAt());
        doc.setUpdatedAt(group.getUpdatedAt());
        return doc;
    }
}
