package com.glpi.knowledge.domain.service;

import com.glpi.knowledge.domain.model.KnowbaseItemCategory;
import com.glpi.knowledge.domain.port.in.CreateCategoryCommand;
import com.glpi.knowledge.domain.port.in.ManageCategoryUseCase;
import com.glpi.knowledge.domain.port.out.KnowbaseItemCategoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages KB categories — creation with hierarchical completeName computation.
 * Requirements: 17.2
 */
@Service
public class ManageCategoryService implements ManageCategoryUseCase {

    private final KnowbaseItemCategoryRepository repository;

    public ManageCategoryService(KnowbaseItemCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public KnowbaseItemCategory createCategory(CreateCategoryCommand command) {
        KnowbaseItemCategory category = new KnowbaseItemCategory();
        category.setId(UUID.randomUUID().toString());
        category.setName(command.name());
        category.setParentId(command.parentId());
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());

        if (command.parentId() != null) {
            repository.findById(command.parentId()).ifPresent(parent -> {
                category.setLevel(parent.getLevel() + 1);
                category.setCompleteName(parent.getCompleteName() + " > " + command.name());
            });
        }

        if (category.getCompleteName() == null) {
            category.setLevel(1);
            category.setCompleteName(command.name());
        }

        return repository.save(category);
    }
}
