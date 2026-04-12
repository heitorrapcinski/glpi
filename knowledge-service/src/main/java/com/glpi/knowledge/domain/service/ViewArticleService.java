package com.glpi.knowledge.domain.service;

import com.glpi.knowledge.domain.model.ArticleNotFoundException;
import com.glpi.knowledge.domain.port.in.ViewArticleUseCase;
import com.glpi.knowledge.domain.port.out.KnowbaseItemRepository;
import org.springframework.stereotype.Service;

/**
 * Increments article view counter atomically using MongoDB $inc.
 * Requirements: 17.9
 */
@Service
public class ViewArticleService implements ViewArticleUseCase {

    private final KnowbaseItemRepository repository;

    public ViewArticleService(KnowbaseItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void viewArticle(String id) {
        if (repository.findById(id).isEmpty()) {
            throw new ArticleNotFoundException(id);
        }
        repository.incrementViewCount(id);
    }
}
