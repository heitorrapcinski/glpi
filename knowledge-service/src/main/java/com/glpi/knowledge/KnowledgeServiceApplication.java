package com.glpi.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Knowledge Service — Knowledge base article management microservice.
 * Manages KB articles, categories, visibility rules, and FAQ.
 * Requirements: 17.1
 */
@SpringBootApplication
public class KnowledgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeServiceApplication.class, args);
    }
}
