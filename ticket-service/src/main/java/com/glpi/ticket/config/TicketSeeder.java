package com.glpi.ticket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Seeds default priority matrix configuration on startup.
 * Requirements: 29.6, 29.12
 */
@Component
public class TicketSeeder {

    private static final Logger log = LoggerFactory.getLogger(TicketSeeder.class);
    private static final String PRIORITY_MATRIX_COLLECTION = "priority_matrix";

    private final MongoTemplate mongoTemplate;

    public TicketSeeder(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (mongoTemplate.getCollection(PRIORITY_MATRIX_COLLECTION).countDocuments() == 0) {
            log.info("Seeding default priority matrix into '{}' collection", PRIORITY_MATRIX_COLLECTION);

            // Default ITIL priority matrix: matrix[urgency][impact] → priority
            // urgency 1..5, impact 1..5, priority 1..6
            Map<String, Object> matrix = Map.of(
                    "_id", "default",
                    "name", "Default Priority Matrix",
                    "entityId", "0",
                    "matrix", Map.of(
                            "1", Map.of("1", 1, "2", 2, "3", 2, "4", 3, "5", 3),
                            "2", Map.of("1", 2, "2", 2, "3", 3, "4", 3, "5", 4),
                            "3", Map.of("1", 2, "2", 3, "3", 3, "4", 4, "5", 4),
                            "4", Map.of("1", 3, "2", 3, "3", 4, "4", 4, "5", 5),
                            "5", Map.of("1", 3, "2", 4, "3", 4, "4", 5, "5", 6)
                    ),
                    "createdAt", Instant.now().toString()
            );

            mongoTemplate.save(matrix, PRIORITY_MATRIX_COLLECTION);
            log.info("Seeded 1 document into '{}' collection at {}", PRIORITY_MATRIX_COLLECTION, Instant.now());
        } else {
            log.debug("Priority matrix collection already populated, skipping seed");
        }
    }
}
