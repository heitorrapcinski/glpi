package com.glpi.sla.adapter.out.persistence;

import com.glpi.sla.domain.model.Sla;
import com.glpi.sla.domain.model.SlaAction;
import com.glpi.sla.domain.model.SlaLevel;
import com.glpi.sla.domain.model.SlaType;
import com.glpi.sla.domain.port.out.SlaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MongoDB adapter implementing SlaRepository.
 */
@Component
public class SlaRepositoryAdapter implements SlaRepository {

    private final MongoSlaRepository mongo;

    public SlaRepositoryAdapter(MongoSlaRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Sla> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Sla save(Sla sla) {
        return toDomain(mongo.save(toDocument(sla)));
    }

    @Override
    public void delete(String id) {
        mongo.deleteById(id);
    }

    @Override
    public List<Sla> findAll() {
        return mongo.findAll().stream().map(this::toDomain).toList();
    }

    // --- mapping ---

    @SuppressWarnings("unchecked")
    private Sla toDomain(SlaDocument doc) {
        List<SlaLevel> levels = new ArrayList<>();
        if (doc.getLevels() != null) {
            for (SlaDocument.LevelDoc ld : doc.getLevels()) {
                List<SlaAction> actions = new ArrayList<>();
                if (ld.getActions() != null) {
                    for (Map<String, Object> a : ld.getActions()) {
                        actions.add(new SlaAction(
                                (String) a.get("actionType"),
                                (Map<String, Object>) a.getOrDefault("parameters", new HashMap<>())
                        ));
                    }
                }
                levels.add(new SlaLevel(ld.getId(), ld.getName(), ld.getExecutionDelaySeconds(), actions));
            }
        }
        return new Sla(doc.getId(), doc.getName(), doc.getEntityId(),
                SlaType.fromValue(doc.getType()), doc.getDurationSeconds(),
                doc.getCalendarId(), levels, doc.getCreatedAt(), doc.getUpdatedAt());
    }

    private SlaDocument toDocument(Sla sla) {
        SlaDocument doc = new SlaDocument();
        doc.setId(sla.getId());
        doc.setName(sla.getName());
        doc.setEntityId(sla.getEntityId());
        doc.setType(sla.getType().getValue());
        doc.setDurationSeconds(sla.getDurationSeconds());
        doc.setCalendarId(sla.getCalendarId());
        doc.setCreatedAt(sla.getCreatedAt());
        doc.setUpdatedAt(sla.getUpdatedAt());

        List<SlaDocument.LevelDoc> levelDocs = new ArrayList<>();
        if (sla.getLevels() != null) {
            for (SlaLevel l : sla.getLevels()) {
                SlaDocument.LevelDoc ld = new SlaDocument.LevelDoc();
                ld.setId(l.getId());
                ld.setName(l.getName());
                ld.setExecutionDelaySeconds(l.getExecutionDelaySeconds());
                List<Map<String, Object>> actionDocs = new ArrayList<>();
                if (l.getActions() != null) {
                    for (SlaAction a : l.getActions()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("actionType", a.getActionType());
                        m.put("parameters", a.getParameters() != null ? a.getParameters() : new HashMap<>());
                        actionDocs.add(m);
                    }
                }
                ld.setActions(actionDocs);
                levelDocs.add(ld);
            }
        }
        doc.setLevels(levelDocs);
        return doc;
    }
}
