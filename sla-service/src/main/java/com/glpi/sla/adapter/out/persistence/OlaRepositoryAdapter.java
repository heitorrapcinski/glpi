package com.glpi.sla.adapter.out.persistence;

import com.glpi.sla.domain.model.Ola;
import com.glpi.sla.domain.model.SlaAction;
import com.glpi.sla.domain.model.SlaLevel;
import com.glpi.sla.domain.model.SlaType;
import com.glpi.sla.domain.port.out.OlaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MongoDB adapter implementing OlaRepository.
 */
@Component
public class OlaRepositoryAdapter implements OlaRepository {

    private final MongoOlaRepository mongo;

    public OlaRepositoryAdapter(MongoOlaRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Ola> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Ola save(Ola ola) {
        return toDomain(mongo.save(toDocument(ola)));
    }

    @Override
    public void delete(String id) {
        mongo.deleteById(id);
    }

    @Override
    public List<Ola> findAll() {
        return mongo.findAll().stream().map(this::toDomain).toList();
    }

    @SuppressWarnings("unchecked")
    private Ola toDomain(OlaDocument doc) {
        List<SlaLevel> levels = new ArrayList<>();
        if (doc.getLevels() != null) {
            for (OlaDocument.LevelDoc ld : doc.getLevels()) {
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
        return new Ola(doc.getId(), doc.getName(), doc.getEntityId(),
                SlaType.fromValue(doc.getType()), doc.getDurationSeconds(),
                doc.getCalendarId(), levels, doc.getCreatedAt(), doc.getUpdatedAt());
    }

    private OlaDocument toDocument(Ola ola) {
        OlaDocument doc = new OlaDocument();
        doc.setId(ola.getId());
        doc.setName(ola.getName());
        doc.setEntityId(ola.getEntityId());
        doc.setType(ola.getType().getValue());
        doc.setDurationSeconds(ola.getDurationSeconds());
        doc.setCalendarId(ola.getCalendarId());
        doc.setCreatedAt(ola.getCreatedAt());
        doc.setUpdatedAt(ola.getUpdatedAt());

        List<OlaDocument.LevelDoc> levelDocs = new ArrayList<>();
        if (ola.getLevels() != null) {
            for (SlaLevel l : ola.getLevels()) {
                OlaDocument.LevelDoc ld = new OlaDocument.LevelDoc();
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
