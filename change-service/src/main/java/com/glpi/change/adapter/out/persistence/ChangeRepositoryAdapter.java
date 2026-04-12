package com.glpi.change.adapter.out.persistence;

import com.glpi.change.domain.model.Change;
import com.glpi.change.domain.port.out.ChangeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing ChangeRepository driven port.
 * Requirements: 22.5
 */
@Component
public class ChangeRepositoryAdapter implements ChangeRepository {

    private final MongoChangeRepository mongo;

    public ChangeRepositoryAdapter(MongoChangeRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Change> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Change save(Change change) {
        ChangeDocument doc = toDocument(change);
        ChangeDocument saved = mongo.save(doc);
        return toDomain(saved);
    }

    @Override
    public void delete(String id) {
        mongo.deleteById(id);
    }

    @Override
    public List<Change> findAll(int page, int size) {
        return mongo.findAllBy(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return mongo.count();
    }

    // --- Mapping ---

    private Change toDomain(ChangeDocument doc) {
        Change c = new Change();
        c.setId(doc.getId());
        c.setStatus(doc.getStatus());
        c.setTitle(doc.getTitle());
        c.setContent(doc.getContent());
        c.setEntityId(doc.getEntityId());
        c.setPriority(doc.getPriority());
        c.setUrgency(doc.getUrgency());
        c.setImpact(doc.getImpact());
        c.setActors(doc.getActors());
        c.setPlanningDocuments(doc.getPlanningDocuments());
        c.setValidationSteps(doc.getValidationSteps());
        c.setLinkedTicketIds(doc.getLinkedTicketIds());
        c.setLinkedProblemIds(doc.getLinkedProblemIds());
        c.setLinkedAssets(doc.getLinkedAssets());
        c.setFollowups(doc.getFollowups());
        c.setTasks(doc.getTasks());
        c.setSolution(doc.getSolution());
        c.setSatisfactionSurvey(doc.getSatisfactionSurvey());
        c.setCreatedAt(doc.getCreatedAt());
        c.setUpdatedAt(doc.getUpdatedAt());
        c.setClosedAt(doc.getClosedAt());
        return c;
    }

    private ChangeDocument toDocument(Change c) {
        ChangeDocument doc = new ChangeDocument();
        doc.setId(c.getId());
        doc.setStatus(c.getStatus());
        doc.setTitle(c.getTitle());
        doc.setContent(c.getContent());
        doc.setEntityId(c.getEntityId());
        doc.setPriority(c.getPriority());
        doc.setUrgency(c.getUrgency());
        doc.setImpact(c.getImpact());
        doc.setActors(c.getActors());
        doc.setPlanningDocuments(c.getPlanningDocuments());
        doc.setValidationSteps(c.getValidationSteps());
        doc.setLinkedTicketIds(c.getLinkedTicketIds());
        doc.setLinkedProblemIds(c.getLinkedProblemIds());
        doc.setLinkedAssets(c.getLinkedAssets());
        doc.setFollowups(c.getFollowups());
        doc.setTasks(c.getTasks());
        doc.setSolution(c.getSolution());
        doc.setSatisfactionSurvey(c.getSatisfactionSurvey());
        doc.setCreatedAt(c.getCreatedAt());
        doc.setUpdatedAt(c.getUpdatedAt());
        doc.setClosedAt(c.getClosedAt());
        return doc;
    }
}
