package com.glpi.knowledge.domain.port.in;

import com.glpi.knowledge.domain.model.KnowbaseItemCategory;

/**
 * Driving port — manage KB categories.
 * Requirements: 17.2
 */
public interface ManageCategoryUseCase {

    KnowbaseItemCategory createCategory(CreateCategoryCommand command);
}
