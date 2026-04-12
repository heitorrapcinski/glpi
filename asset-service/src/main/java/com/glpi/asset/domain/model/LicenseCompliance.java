package com.glpi.asset.domain.model;

/**
 * License compliance status response.
 * Requirements: 13.5
 */
public record LicenseCompliance(
        String licenseId,
        int totalSeats,
        long usedSeats,
        long remainingSeats
) {}
