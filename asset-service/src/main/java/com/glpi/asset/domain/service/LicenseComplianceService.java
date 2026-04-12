package com.glpi.asset.domain.service;

import com.glpi.asset.domain.model.LicenseCompliance;
import com.glpi.asset.domain.model.SoftwareLicense;
import com.glpi.asset.domain.port.out.EventPublisherPort;
import com.glpi.asset.domain.port.out.ItemSoftwareVersionRepository;
import com.glpi.asset.domain.port.out.SoftwareLicenseRepository;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service for license compliance checking.
 * Counts active installations per license and publishes LicenseOverused when exceeded.
 * Requirements: 13.4, 13.5
 */
@Service
public class LicenseComplianceService {

    private final SoftwareLicenseRepository licenseRepository;
    private final ItemSoftwareVersionRepository installationRepository;
    private final EventPublisherPort eventPublisher;

    public LicenseComplianceService(SoftwareLicenseRepository licenseRepository,
                                    ItemSoftwareVersionRepository installationRepository,
                                    EventPublisherPort eventPublisher) {
        this.licenseRepository = licenseRepository;
        this.installationRepository = installationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Check compliance for a given license and publish event if overused.
     */
    public LicenseCompliance checkCompliance(String licenseId) {
        SoftwareLicense license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found: " + licenseId));

        long usedSeats = installationRepository.countByLicenseId(licenseId);
        int totalSeats = license.getNumberOfSeats();
        long remaining = totalSeats - usedSeats;

        if (usedSeats > totalSeats) {
            eventPublisher.publish(new DomainEventEnvelope(
                    UUID.randomUUID().toString(),
                    "LicenseOverused",
                    licenseId,
                    "SoftwareLicense",
                    Instant.now(),
                    1,
                    new LicenseCompliance(licenseId, totalSeats, usedSeats, remaining)
            ));
        }

        return new LicenseCompliance(licenseId, totalSeats, usedSeats, remaining);
    }
}
