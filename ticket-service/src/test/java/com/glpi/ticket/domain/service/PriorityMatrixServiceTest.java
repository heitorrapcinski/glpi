package com.glpi.ticket.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PriorityMatrixService.
 * Requirements: 5.8, 27.1, 27.2
 */
class PriorityMatrixServiceTest {

    private final PriorityMatrixService service = new PriorityMatrixService();

    @Test
    void computePriority_lowestUrgencyAndImpact_returnsLowestPriority() {
        int priority = service.computePriority(1, 1, "entity-1");
        assertEquals(1, priority);
    }

    @Test
    void computePriority_highestUrgencyAndImpact_returnsMajorPriority() {
        int priority = service.computePriority(5, 5, "entity-1");
        assertEquals(6, priority);
    }

    @Test
    void computePriority_allValidCombinations_returnsBetween1And6() {
        for (int u = 1; u <= 5; u++) {
            for (int i = 1; i <= 5; i++) {
                int p = service.computePriority(u, i, "entity-1");
                assertTrue(p >= 1 && p <= 6,
                        "Priority out of range for urgency=" + u + " impact=" + i + ": " + p);
            }
        }
    }

    @Test
    void computePriority_invalidUrgency_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.computePriority(0, 3, "e1"));
        assertThrows(IllegalArgumentException.class, () -> service.computePriority(6, 3, "e1"));
    }

    @Test
    void computePriority_invalidImpact_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.computePriority(3, 0, "e1"));
        assertThrows(IllegalArgumentException.class, () -> service.computePriority(3, 6, "e1"));
    }
}
