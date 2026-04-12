package com.glpi.knowledge.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.knowledge.domain.model.ArticleNotFoundException;
import com.glpi.knowledge.domain.model.KnowbaseItem;
import com.glpi.knowledge.domain.model.KnowbaseItemRevision;
import com.glpi.knowledge.domain.port.in.UpdateArticleCommand;
import com.glpi.knowledge.domain.port.in.UpdateArticleUseCase;
import com.glpi.knowledge.domain.port.out.EventPublisherPort;
import com.glpi.knowledge.domain.port.out.KnowbaseItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Updates a KB article, creates a revision entry, and publishes KnowledgeArticleUpdated event.
 * Requirements: 17.7
 */
@Service
public class UpdateArticleService implements UpdateArticleUseCase {

    private final KnowbaseItemRepository repository;
    private final EventPublisherPort eventPublisher;

    public UpdateArticleService(KnowbaseItemRepository repository, EventPublisherPort eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public KnowbaseItem updateArticle(String id, UpdateArticleCommand command) {
        KnowbaseItem existing = repository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

        // Create revision entry before updating
        KnowbaseItemRevision revision = new KnowbaseItemRevision();
        revision.setId(UUID.randomUUID().toString());
        revision.setOldTitle(existing.getTitle());
        revision.setOldAnswer(existing.getAnswer());
        revision.setAuthorId(command.authorId());
        revision.setCreatedAt(Instant.now());
        existing.getRevisions().add(revision);

        // Apply updates
        existing.setTitle(command.title());
        existing.setAnswer(command.answer());
        existing.setFaq(command.isFaq());
        existing.setVisibility(command.visibility());
        existing.setCategoryIds(command.categoryIds() != null ? command.categoryIds() : existing.getCategoryIds());
        existing.setBeginDate(command.beginDate());
        existing.setEndDate(command.endDate());
        existing.setUpdatedAt(Instant.now());

        KnowbaseItem saved = repository.save(existing);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "KnowledgeArticleUpdated",
                saved.getId(),
                "KnowbaseItem",
                Instant.now(),
                1,
                Map.of("title", saved.getTitle(), "authorId", command.authorId())
        ));

        return saved;
    }
}
