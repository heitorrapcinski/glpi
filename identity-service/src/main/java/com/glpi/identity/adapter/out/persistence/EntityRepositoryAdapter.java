package com.glpi.identity.adapter.out.persistence;

import com.glpi.identity.domain.model.Entity;
import com.glpi.identity.domain.model.EntityConfig;
import com.glpi.identity.domain.port.out.EntityRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing the EntityRepository driven port using MongoDB.
 */
@Component
public class EntityRepositoryAdapter implements EntityRepository {

    private final MongoEntityRepository mongoRepo;

    public EntityRepositoryAdapter(MongoEntityRepository mongoRepo) {
        this.mongoRepo = mongoRepo;
    }

    @Override
    public Optional<Entity> findById(String id) {
        return mongoRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Entity> findChildren(String parentId) {
        return mongoRepo.findByParentId(parentId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Entity save(Entity entity) {
        EntityDocument doc = toDocument(entity);
        EntityDocument saved = mongoRepo.save(doc);
        return toDomain(saved);
    }

    @Override
    public void delete(String id) {
        mongoRepo.deleteById(id);
    }

    @Override
    public boolean existsByNameAndParentId(String name, String parentId) {
        return mongoRepo.existsByNameAndParentId(name, parentId);
    }

    @Override
    public List<Entity> findAll() {
        return mongoRepo.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    // --- Mapping ---

    private Entity toDomain(EntityDocument doc) {
        EntityConfig config = new EntityConfig();
        if (doc.getConfig() != null) {
            EntityDocument.EntityConfigDocument cfg = doc.getConfig();
            config.setDefaultTicketType(cfg.getDefaultTicketType());
            config.setAutoAssignMode(cfg.getAutoAssignMode());
            config.setAutoCloseDelay(cfg.getAutoCloseDelay());
            config.setCalendarId(cfg.getCalendarId());
            config.setSatisfactionSurveyEnabled(cfg.getSatisfactionSurveyEnabled());
            config.setNotificationSenderEmail(cfg.getNotificationSenderEmail());
        }

        Entity entity = new Entity(
                doc.getId(),
                doc.getName(),
                doc.getParentId(),
                doc.getLevel(),
                doc.getCompleteName(),
                config
        );
        entity.setCreatedAt(doc.getCreatedAt());
        entity.setUpdatedAt(doc.getUpdatedAt());
        return entity;
    }

    private EntityDocument toDocument(Entity entity) {
        EntityDocument doc = new EntityDocument();
        doc.setId(entity.getId());
        doc.setName(entity.getName());
        doc.setParentId(entity.getParentId());
        doc.setLevel(entity.getLevel());
        doc.setCompleteName(entity.getCompleteName());

        EntityDocument.EntityConfigDocument cfg = new EntityDocument.EntityConfigDocument();
        EntityConfig config = entity.getConfig();
        cfg.setDefaultTicketType(config.getDefaultTicketType());
        cfg.setAutoAssignMode(config.getAutoAssignMode());
        cfg.setAutoCloseDelay(config.getAutoCloseDelay());
        cfg.setCalendarId(config.getCalendarId());
        cfg.setSatisfactionSurveyEnabled(config.getSatisfactionSurveyEnabled());
        cfg.setNotificationSenderEmail(config.getNotificationSenderEmail());
        doc.setConfig(cfg);

        doc.setCreatedAt(entity.getCreatedAt());
        doc.setUpdatedAt(entity.getUpdatedAt());
        return doc;
    }
}
