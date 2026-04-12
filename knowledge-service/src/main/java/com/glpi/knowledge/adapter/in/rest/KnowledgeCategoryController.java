package com.glpi.knowledge.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.knowledge.domain.model.CategoryNotFoundException;
import com.glpi.knowledge.domain.model.KnowbaseItemCategory;
import com.glpi.knowledge.domain.port.in.CreateCategoryCommand;
import com.glpi.knowledge.domain.port.in.ManageCategoryUseCase;
import com.glpi.knowledge.domain.port.out.KnowbaseItemCategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for KB categories.
 * Requirements: 17.2, 19.1, 19.6
 */
@RestController
@RequestMapping("/knowledge/categories")
@Tag(name = "Knowledge Categories", description = "Knowledge base category management")
public class KnowledgeCategoryController {

    private final ManageCategoryUseCase manageCategoryUseCase;
    private final KnowbaseItemCategoryRepository repository;

    public KnowledgeCategoryController(ManageCategoryUseCase manageCategoryUseCase,
                                        KnowbaseItemCategoryRepository repository) {
        this.manageCategoryUseCase = manageCategoryUseCase;
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List KB categories (paginated)")
    public PagedResponse<KnowbaseItemCategory> listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(value = "expand_dropdowns", required = false) Boolean expandDropdowns) {
        int clampedSize = Math.min(Math.max(size, 1), 500);
        List<KnowbaseItemCategory> categories = repository.findAll(page, clampedSize);
        long total = repository.countAll();
        return PagedResponse.of(categories, total, page, clampedSize);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new KB category")
    public KnowbaseItemCategory createCategory(@RequestBody CreateCategoryCommand command) {
        return manageCategoryUseCase.createCategory(command);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a KB category by ID")
    public ResponseEntity<KnowbaseItemCategory> getCategory(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
