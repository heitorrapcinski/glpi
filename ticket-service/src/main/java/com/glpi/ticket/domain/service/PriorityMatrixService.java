package com.glpi.ticket.domain.service;

import org.springframework.stereotype.Service;

/**
 * Computes ticket priority from urgency and impact using the ITIL priority matrix.
 * Default matrix follows standard GLPI/ITIL mapping where both urgency and impact
 * range from 1 (very low) to 5 (very high) and priority ranges from 1 to 6.
 * Requirements: 5.8, 27.1, 27.2, 27.3, 29.6
 */
@Service
public class PriorityMatrixService {

    /**
     * Default ITIL priority matrix: matrix[urgency][impact] → priority.
     * Indices are 1-based (urgency 1..5, impact 1..5).
     * Priority values: 1=Very Low, 2=Low, 3=Medium, 4=High, 5=Very High, 6=Major
     */
    private static final int[][] DEFAULT_MATRIX = {
        // impact: 1   2   3   4   5
        {0,  0,  0,  0,  0,  0}, // padding (index 0 unused)
        {0,  1,  2,  2,  3,  3}, // urgency=1
        {0,  2,  2,  3,  3,  4}, // urgency=2
        {0,  2,  3,  3,  4,  4}, // urgency=3
        {0,  3,  3,  4,  4,  5}, // urgency=4
        {0,  3,  4,  4,  5,  6}, // urgency=5
    };

    /**
     * Compute priority for the given urgency, impact, and entity.
     * Entity-specific matrix loading is a future extension; currently uses the default matrix.
     *
     * @param urgency  1..5
     * @param impact   1..5
     * @param entityId entity identifier (reserved for future entity-specific matrix lookup)
     * @return priority 1..6
     */
    public int computePriority(int urgency, int impact, String entityId) {
        if (urgency < 1 || urgency > 5) {
            throw new IllegalArgumentException("Urgency must be between 1 and 5, got: " + urgency);
        }
        if (impact < 1 || impact > 5) {
            throw new IllegalArgumentException("Impact must be between 1 and 5, got: " + impact);
        }
        return DEFAULT_MATRIX[urgency][impact];
    }
}
