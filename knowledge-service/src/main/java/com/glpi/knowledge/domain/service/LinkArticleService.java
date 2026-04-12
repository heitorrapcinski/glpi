package com.glpi.knowledge.domain.service;

import com.glpi.knowledge.domain.model.ArticleNotFoundException;
import com.glpi.knowledge.domain.model.KnowbaseItem;
import com.glpi.knowledge.domain.model.LinkedItem;
import com.glpi.knowledge.domain.port.in.LinkArticleCommand;
import com.glpi.knowledge.domain.port.in.LinkArticleUseCase;
import com.glpi.knowledge.domain.port.out.KnowbaseItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Links a KB article to another ITIL item (ticket, problem, change).
 * Requirements: 17.11
 */
@Service
public class LinkArticleService implements LinkArticleUseCase {

    private final KnowbaseItemRepository repository;

    public LinkArticleService(KnowbaseItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public KnowbaseItem linkArticle(String articleId, LinkArticleCommand command) {
        KnowbaseItem article = repository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId));

        article.getLinkedItems().add(new LinkedItem(command.itemType(), command.itemId()));
        article.setUpdatedAt(Instant.now());

        return repository.save(article);
    }
}
