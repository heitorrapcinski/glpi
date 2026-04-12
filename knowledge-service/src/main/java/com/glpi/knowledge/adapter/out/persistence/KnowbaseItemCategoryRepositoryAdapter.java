package com.glpi.knowledge.adapter.out.persistence;

import com.glpi.knowledge.domain.model.KnowbaseItemCategory;
import com.glpi.knowledge.domain.port.out.KnowbaseItemCategoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing KnowbaseItemCategoryRepository driven port.
 * Requirements: 17.2
 */
@Component
public class KnowbaseItemCategoryRepositoryAdapter implements KnowbaseItemCategoryRepository {

    private final MongoKnowbaseItemCategoryRepository mongo;

    public KnowbaseItemCategoryRepositoryAdapter(MongoKnowbaseItemCategoryRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<KnowbaseItemCategory> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public KnowbaseItemCategory save(KnowbaseItemCategory category) {
        KnowbaseItemCategoryDocument doc = toDocument(category);
        KnowbaseItemCategoryDocument saved = mongo.save(doc);
        return toDomain(saved);
    }

    @Override
    public List<KnowbaseItemCategory> findAll(int page, int size) {
        return mongo.findAllBy(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return mongo.count();
    }

    private KnowbaseItemCategory toDomain(KnowbaseItemCategoryDocument doc) {
        KnowbaseItemCategory cat = new KnowbaseItemCategory();
        cat.setId(doc.getId());
        cat.setName(doc.getName());
        cat.setParentId(doc.getParentId());
        cat.setLevel(doc.getLevel());
        cat.setCompleteName(doc.getCompleteName());
        cat.setCreatedAt(doc.getCreatedAt());
        cat.setUpdatedAt(doc.getUpdatedAt());
        return cat;
    }

    private KnowbaseItemCategoryDocument toDocument(KnowbaseItemCategory cat) {
        KnowbaseItemCategoryDocument doc = new KnowbaseItemCategoryDocument();
        doc.setId(cat.getId());
        doc.setName(cat.getName());
        doc.setParentId(cat.getParentId());
        doc.setLevel(cat.getLevel());
        doc.setCompleteName(cat.getCompleteName());
        doc.setCreatedAt(cat.getCreatedAt());
        doc.setUpdatedAt(cat.getUpdatedAt());
        return doc;
    }
}
