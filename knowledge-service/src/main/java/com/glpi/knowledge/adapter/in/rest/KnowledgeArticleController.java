package com.glpi.knowledge.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.knowledge.domain.model.ArticleNotFoundException;
import com.glpi.knowledge.domain.model.KnowbaseItem;
import com.glpi.knowledge.domain.model.KnowbaseItemRevision;
import com.glpi.knowledge.domain.model.UserContext;
import com.glpi.knowledge.domain.port.in.*;
import com.glpi.knowledge.domain.port.out.KnowbaseItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * REST controller for KB articles.
 * Requirements: 17.1, 19.1, 19.6
 */
@RestController
@RequestMapping("/knowledge/articles")
@Tag(name = "Knowledge Articles", description = "Knowledge base article management")
public class KnowledgeArticleController {

    private final CreateArticleUseCase createArticleUseCase;
    private final UpdateArticleUseCase updateArticleUseCase;
    private final ViewArticleUseCase viewArticleUseCase;
    private final AddCommentUseCase addCommentUseCase;
    private final LinkArticleUseCase linkArticleUseCase;
    private final VisibilityResolverPort visibilityResolver;
    private final KnowbaseItemRepository repository;

    public KnowledgeArticleController(CreateArticleUseCase createArticleUseCase,
                                       UpdateArticleUseCase updateArticleUseCase,
                                       ViewArticleUseCase viewArticleUseCase,
                                       AddCommentUseCase addCommentUseCase,
                                       LinkArticleUseCase linkArticleUseCase,
                                       VisibilityResolverPort visibilityResolver,
                                       KnowbaseItemRepository repository) {
        this.createArticleUseCase = createArticleUseCase;
        this.updateArticleUseCase = updateArticleUseCase;
        this.viewArticleUseCase = viewArticleUseCase;
        this.addCommentUseCase = addCommentUseCase;
        this.linkArticleUseCase = linkArticleUseCase;
        this.visibilityResolver = visibilityResolver;
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List KB articles (paginated, visibility-filtered)")
    public PagedResponse<KnowbaseItem> listArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Entity-Id", required = false) String entityId,
            @RequestHeader(value = "X-Profile-Id", required = false) String profileId,
            @RequestHeader(value = "X-Profile-Interface", required = false) String profileInterface,
            @RequestParam(value = "expand_dropdowns", required = false) Boolean expandDropdowns) {
        int clampedSize = Math.min(Math.max(size, 1), 500);
        UserContext userContext = new UserContext(userId, entityId, profileId, profileInterface, Collections.emptyList());
        List<KnowbaseItem> all = repository.findAll(page, clampedSize);
        List<KnowbaseItem> visible = visibilityResolver.filterVisible(all, userContext);
        long total = repository.countAll();
        return PagedResponse.of(visible, total, page, clampedSize);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new KB article")
    public KnowbaseItem createArticle(@Valid @RequestBody CreateArticleCommand command) {
        return createArticleUseCase.createArticle(command);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a KB article by ID and increment view counter")
    public ResponseEntity<KnowbaseItem> getArticle(@PathVariable String id) {
        viewArticleUseCase.viewArticle(id);
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a KB article")
    public KnowbaseItem updateArticle(@PathVariable String id,
                                       @RequestBody UpdateArticleCommand command) {
        return updateArticleUseCase.updateArticle(id, command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a KB article")
    public void deleteArticle(@PathVariable String id) {
        if (repository.findById(id).isEmpty()) {
            throw new ArticleNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @GetMapping("/{id}/revisions")
    @Operation(summary = "Get revision history for a KB article")
    public ResponseEntity<List<KnowbaseItemRevision>> getRevisions(@PathVariable String id) {
        return repository.findById(id)
                .map(article -> ResponseEntity.ok(article.getRevisions()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a comment to a KB article")
    public KnowbaseItem addComment(@PathVariable String id,
                                    @RequestBody AddCommentCommand command) {
        return addCommentUseCase.addComment(id, command);
    }

    @PostMapping("/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Link a KB article to another ITIL item")
    public KnowbaseItem linkArticle(@PathVariable String id,
                                     @RequestBody LinkArticleCommand command) {
        return linkArticleUseCase.linkArticle(id, command);
    }

    @GetMapping("/search")
    @Operation(summary = "Full-text search on KB articles")
    public PagedResponse<KnowbaseItem> searchArticles(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "ASC") String order) {
        int clampedSize = Math.min(Math.max(size, 1), 500);
        List<KnowbaseItem> results = repository.searchByText(query, page, clampedSize);
        long total = repository.countByTextSearch(query);
        return PagedResponse.of(results, total, page, clampedSize);
    }
}
