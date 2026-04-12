package com.glpi.sla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SLA Service — manages SLA/OLA definitions, calendars, business-hours computation,
 * and escalation scheduling.
 * Requirements: 14.1
 */
@SpringBootApplication
@EnableScheduling
public class SlaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlaServiceApplication.class, args);
    }
}
