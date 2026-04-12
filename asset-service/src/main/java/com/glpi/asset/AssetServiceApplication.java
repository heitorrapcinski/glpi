package com.glpi.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GLPI Asset Service — CMDB asset and license management microservice.
 * Requirements: 12.1
 */
@SpringBootApplication
public class AssetServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetServiceApplication.class, args);
    }
}
