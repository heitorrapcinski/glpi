package com.glpi.knowledge.adapter.out.persistence;

import com.glpi.knowledge.domain.model.KnowbaseItem;
import com.glpi.knowledge.domain.port.out.KnowbaseItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing KnowbaseItemRepository driven port.
 * Requirements: 17.1, 17.9, 17.12
 */
@Component
public class KnowbaseItemRepositoryAdapter implements KnowbaseItemRepository {

    private final MongoKnowbaseItemRepository mongo;
    private final MongoTemplate mongoTemplate;

    public KnowbaseItemRepositoryAdapter(MongoKnowbaseItemRepository mongo,
                                          MongoTemplate mongoTemplate) {
        this.mongo = mongo;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<KnowbaseItem> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public KnowbaseItem save(KnowbaseItem item) {
        KnowbaseItemDocument doc = toDocument(item);
        KnowbaseItemDocument saved = mongo.save(doc);
        return toDomain(saved);
    }

    @Override
    public void deleteById(String id) {
        mongo.deleteById(id);
    }

    @Override
    public List<KnowbaseItem> findAll(int page, int size) {
        return mongo.findAllBy(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return mongo.count();
    }

    @Override
    public void incrementViewCount(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().inc("viewCount", 1);
        mongoTemplate.updateFirst(query, update, KnowbaseItemDocument.class);
    }

    @Override
    public List<KnowbaseItem> searchByText(String queryText, int page, int size) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingPhrase(queryText);
        Query query = TextQuery.queryText(criteria)
                .sortByScore()
                .skip((long) page * size)
                .limit(size);
        return mongoTemplate.find(query, KnowbaseItemDocument.class)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByTextSearch(String queryText) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingPhrase(queryText);
        Query query = TextQuery.queryText(criteria);
        return mongoTemplate.count(query, KnowbaseItemDocument.class);
    }

    private KnowbaseItem toDomain(KnowbaseItemDocument doc) {
        KnowbaseItem item = new KnowbaseItem();
        item.setId(doc.getId());
        item.setTitle(doc.getTitle());
        item.setAnswer(doc.getAnswer());
        item.setAuthorId(doc.getAuthorId());
        item.setFaq(doc.isFaq());
        item.setViewCount(doc.getViewCount());
        item.setVisibility(doc.getVisibility());
        item.setCategoryIds(doc.getCategoryIds());
        item.setRevisions(doc.getRevisions());
        item.setLinkedItems(doc.getLinkedItems());
        item.setComments(doc.getComments());
        item.setBeginDate(doc.getBeginDate());
        item.setEndDate(doc.getEndDate());
        item.setCreatedAt(doc.getCreatedAt());
        item.setUpdatedAt(doc.getUpdatedAt());
        return item;
    }

    private KnowbaseItemDocument toDocument(KnowbaseItem item) {
        KnowbaseItemDocument doc = new KnowbaseItemDocument();
        doc.setId(item.getId());
        doc.setTitle(item.getTitle());
        doc.setAnswer(item.getAnswer());
        doc.setAuthorId(item.getAuthorId());
        doc.setFaq(item.isFaq());
        doc.setViewCount(item.getViewCount());
        doc.setVisibility(item.getVisibility());
        doc.setCategoryIds(item.getCategoryIds());
        doc.setRevisions(item.getRevisions());
        doc.setLinkedItems(item.getLinkedItems());
        doc.setComments(item.getComments());
        doc.setBeginDate(item.getBeginDate());
        doc.setEndDate(item.getEndDate());
        doc.setCreatedAt(item.getCreatedAt());
        doc.setUpdatedAt(item.getUpdatedAt());
        return doc;
    }
}
