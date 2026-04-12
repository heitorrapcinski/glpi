package com.glpi.knowledge.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.knowledge.domain.model.KnowbaseItem;
import com.glpi.knowledge.domain.port.in.CreateArticleCommand;
import com.glpi.knowledge.domain.port.in.CreateArticleUseCase;
import com.glpi.knowledge.domain.port.out.EventPublisherPort;
import com.glpi.knowledge.domain.port.out.KnowbaseItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a new KB article and publishes KnowledgeArticleCreated event.
 * Requirements: 17.1
 */
@Service
public class CreateArticleService implements CreateArticleUseCase {

    private final KnowbaseItemRepository repository;
    private final EventPublisherPort eventPublisher;

    public CreateArticleService(KnowbaseItemRepository repository, EventPublisherPort eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public KnowbaseItem createArticle(CreateArticleCommand command) {
        KnowbaseItem item = new KnowbaseItem();
        item.setId(UUID.randomUUID().toString());
        item.setTitle(command.title());
        item.setAnswer(command.answer());
        item.setAuthorId(command.authorId());
        item.setFaq(command.isFaq());
        item.setVisibility(command.visibility());
        item.setCategoryIds(command.categoryIds() != null ? command.categoryIds() : java.util.List.of());
        item.setBeginDate(command.beginDate());
        item.setEndDate(command.endDate());
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());

        KnowbaseItem saved = repository.save(item);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "KnowledgeArticleCreated",
                saved.getId(),
                "KnowbaseItem",
                Instant.now(),
                1,
                Map.of("title", saved.getTitle(), "authorId", saved.getAuthorId())
        ));

        return saved;
    }
}
