package com.glpi.knowledge.config;

import com.glpi.knowledge.adapter.out.persistence.KnowbaseItemCategoryDocument;
import com.glpi.knowledge.adapter.out.persistence.MongoKnowbaseItemCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Seeds default root KB category on first startup.
 * Requirements: 29.9, 29.12
 */
@Component
public class KnowledgeSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeSeeder.class);

    private final MongoKnowbaseItemCategoryRepository categoryRepository;

    public KnowledgeSeeder(MongoKnowbaseItemCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("knowledge_categories collection already contains data, skipping seeding");
            return;
        }

        KnowbaseItemCategoryDocument root = new KnowbaseItemCategoryDocument();
        root.setId("0");
        root.setName("Root");
        root.setParentId(null);
        root.setLevel(1);
        root.setCompleteName("Root");
        root.setCreatedAt(Instant.now());
        root.setUpdatedAt(Instant.now());

        categoryRepository.save(root);
        log.info("Seeded default root KB category (id=0, name=Root) at {}", Instant.now());
    }
}
