package com.glpi.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Notification Service — Event-driven notification microservice.
 * Consumes domain events from Kafka and dispatches notifications via email/webhook.
 * Requirements: 16.1
 */
@SpringBootApplication
@EnableRetry
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
