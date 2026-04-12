package com.glpi.knowledge.domain.service;

import com.glpi.knowledge.domain.model.ArticleNotFoundException;
import com.glpi.knowledge.domain.model.KnowbaseItem;
import com.glpi.knowledge.domain.model.KnowbaseItemComment;
import com.glpi.knowledge.domain.port.in.AddCommentCommand;
import com.glpi.knowledge.domain.port.in.AddCommentUseCase;
import com.glpi.knowledge.domain.port.out.KnowbaseItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Adds a comment to a KB article.
 * Requirements: 17.8
 */
@Service
public class AddCommentService implements AddCommentUseCase {

    private final KnowbaseItemRepository repository;

    public AddCommentService(KnowbaseItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public KnowbaseItem addComment(String articleId, AddCommentCommand command) {
        KnowbaseItem article = repository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId));

        KnowbaseItemComment comment = new KnowbaseItemComment();
        comment.setId(UUID.randomUUID().toString());
        comment.setContent(command.content());
        comment.setAuthorId(command.authorId());
        comment.setCreatedAt(Instant.now());

        article.getComments().add(comment);
        article.setUpdatedAt(Instant.now());

        return repository.save(article);
    }
}
