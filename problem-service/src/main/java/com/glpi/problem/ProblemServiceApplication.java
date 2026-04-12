package com.glpi.problem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Problem Service — ITIL Problem management microservice.
 * Requirements: 10.1
 */
@SpringBootApplication
public class ProblemServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProblemServiceApplication.class, args);
    }
}
