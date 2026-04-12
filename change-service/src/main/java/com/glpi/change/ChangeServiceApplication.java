package com.glpi.change;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GLPI Change Service — ITIL Change management microservice.
 * Requirements: 11.1
 */
@SpringBootApplication
public class ChangeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangeServiceApplication.class, args);
    }
}
