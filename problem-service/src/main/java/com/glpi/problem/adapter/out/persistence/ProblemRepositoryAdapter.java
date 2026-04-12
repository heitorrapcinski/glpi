package com.glpi.problem.adapter.out.persistence;

import com.glpi.problem.domain.model.Problem;
import com.glpi.problem.domain.port.out.ProblemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing ProblemRepository driven port.
 * Requirements: 22.4
 */
@Component
public class ProblemRepositoryAdapter implements ProblemRepository {

    private final MongoProblemRepository mongo;

    public ProblemRepositoryAdapter(MongoProblemRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Problem> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Problem save(Problem problem) {
        ProblemDocument doc = toDocument(problem);
        ProblemDocument saved = mongo.save(doc);
        return toDomain(saved);
    }

    @Override
    public void delete(String id) {
        mongo.deleteById(id);
    }

    @Override
    public List<Problem> findAll(int page, int size) {
        return mongo.findAllBy(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return mongo.count();
    }

    // --- Mapping ---

    private Problem toDomain(ProblemDocument doc) {
        Problem p = new Problem();
        p.setId(doc.getId());
        p.setStatus(doc.getStatus());
        p.setTitle(doc.getTitle());
        p.setContent(doc.getContent());
        p.setEntityId(doc.getEntityId());
        p.setPriority(doc.getPriority());
        p.setUrgency(doc.getUrgency());
        p.setImpact(doc.getImpact());
        p.setActors(doc.getActors());
        p.setLinkedTicketIds(doc.getLinkedTicketIds());
        p.setLinkedAssets(doc.getLinkedAssets());
        p.setImpactContent(doc.getImpactContent());
        p.setCauseContent(doc.getCauseContent());
        p.setSymptomContent(doc.getSymptomContent());
        p.setFollowups(doc.getFollowups());
        p.setTasks(doc.getTasks());
        p.setSolution(doc.getSolution());
        p.setCreatedAt(doc.getCreatedAt());
        p.setUpdatedAt(doc.getUpdatedAt());
        p.setSolvedAt(doc.getSolvedAt());
        p.setClosedAt(doc.getClosedAt());
        return p;
    }

    private ProblemDocument toDocument(Problem p) {
        ProblemDocument doc = new ProblemDocument();
        doc.setId(p.getId());
        doc.setStatus(p.getStatus());
        doc.setTitle(p.getTitle());
        doc.setContent(p.getContent());
        doc.setEntityId(p.getEntityId());
        doc.setPriority(p.getPriority());
        doc.setUrgency(p.getUrgency());
        doc.setImpact(p.getImpact());
        doc.setActors(p.getActors());
        doc.setLinkedTicketIds(p.getLinkedTicketIds());
        doc.setLinkedAssets(p.getLinkedAssets());
        doc.setImpactContent(p.getImpactContent());
        doc.setCauseContent(p.getCauseContent());
        doc.setSymptomContent(p.getSymptomContent());
        doc.setFollowups(p.getFollowups());
        doc.setTasks(p.getTasks());
        doc.setSolution(p.getSolution());
        doc.setCreatedAt(p.getCreatedAt());
        doc.setUpdatedAt(p.getUpdatedAt());
        doc.setSolvedAt(p.getSolvedAt());
        doc.setClosedAt(p.getClosedAt());
        return doc;
    }
}
