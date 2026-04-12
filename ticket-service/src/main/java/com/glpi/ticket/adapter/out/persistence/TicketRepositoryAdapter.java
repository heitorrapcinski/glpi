package com.glpi.ticket.adapter.out.persistence;

import com.glpi.ticket.domain.model.Ticket;
import com.glpi.ticket.domain.model.TicketStatus;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing TicketRepository driven port.
 * Requirements: 22.3, 22.9
 */
@Component
public class TicketRepositoryAdapter implements TicketRepository {

    private final MongoTicketRepository mongo;

    public TicketRepositoryAdapter(MongoTicketRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Ticket> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Ticket save(Ticket ticket) {
        TicketDocument doc = toDocument(ticket);
        TicketDocument saved = mongo.save(doc);
        return toDomain(saved);
    }

    @Override
    public List<Ticket> findByEntityId(String entityId) {
        return mongo.findByEntityIdAndIsDeletedFalse(entityId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        return mongo.findByStatusAndIsDeletedFalse(status)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findAllNotDeleted(int page, int size) {
        return mongo.findByIsDeletedFalse(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAllNotDeleted() {
        return mongo.countByIsDeletedFalse();
    }

    // --- Mapping ---

    private Ticket toDomain(TicketDocument doc) {
        Ticket t = new Ticket();
        t.setId(doc.getId());
        t.setType(doc.getType());
        t.setStatus(doc.getStatus());
        t.setTitle(doc.getTitle());
        t.setContent(doc.getContent());
        t.setEntityId(doc.getEntityId());
        t.setPriority(doc.getPriority());
        t.setUrgency(doc.getUrgency());
        t.setImpact(doc.getImpact());
        t.setCategoryId(doc.getCategoryId());
        t.setDeleted(doc.isDeleted());
        t.setActors(doc.getActors());
        t.setFollowups(doc.getFollowups());
        t.setTasks(doc.getTasks());
        t.setSolution(doc.getSolution());
        t.setValidations(doc.getValidations());
        t.setSla(doc.getSla());
        t.setPriorityManualOverride(doc.isPriorityManualOverride());
        t.setCreatedAt(doc.getCreatedAt());
        t.setUpdatedAt(doc.getUpdatedAt());
        t.setSolvedAt(doc.getSolvedAt());
        t.setClosedAt(doc.getClosedAt());
        t.setTakeIntoAccountDelay(doc.getTakeIntoAccountDelay());
        t.setSolveDelayStat(doc.getSolveDelayStat());
        return t;
    }

    private TicketDocument toDocument(Ticket t) {
        TicketDocument doc = new TicketDocument();
        doc.setId(t.getId());
        doc.setType(t.getType());
        doc.setStatus(t.getStatus());
        doc.setTitle(t.getTitle());
        doc.setContent(t.getContent());
        doc.setEntityId(t.getEntityId());
        doc.setPriority(t.getPriority());
        doc.setUrgency(t.getUrgency());
        doc.setImpact(t.getImpact());
        doc.setCategoryId(t.getCategoryId());
        doc.setDeleted(t.isDeleted());
        doc.setActors(t.getActors());
        doc.setFollowups(t.getFollowups());
        doc.setTasks(t.getTasks());
        doc.setSolution(t.getSolution());
        doc.setValidations(t.getValidations());
        doc.setSla(t.getSla());
        doc.setPriorityManualOverride(t.isPriorityManualOverride());
        doc.setCreatedAt(t.getCreatedAt());
        doc.setUpdatedAt(t.getUpdatedAt());
        doc.setSolvedAt(t.getSolvedAt());
        doc.setClosedAt(t.getClosedAt());
        doc.setTakeIntoAccountDelay(t.getTakeIntoAccountDelay());
        doc.setSolveDelayStat(t.getSolveDelayStat());
        return doc;
    }
}
