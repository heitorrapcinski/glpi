package com.glpi.asset.adapter.in.rest;

import com.glpi.asset.domain.model.LicenseCompliance;
import com.glpi.asset.domain.model.SoftwareLicense;
import com.glpi.asset.domain.port.out.SoftwareLicenseRepository;
import com.glpi.asset.domain.service.LicenseComplianceService;
import com.glpi.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for software license management and compliance.
 * Requirements: 13.1–13.5, 19.1, 19.6
 */
@RestController
@RequestMapping("/assets/licenses")
@Tag(name = "Licenses", description = "Software license management")
public class LicenseController {

    private final SoftwareLicenseRepository licenseRepository;
    private final LicenseComplianceService complianceService;

    public LicenseController(SoftwareLicenseRepository licenseRepository,
                             LicenseComplianceService complianceService) {
        this.licenseRepository = licenseRepository;
        this.complianceService = complianceService;
    }

    @GetMapping
    @Operation(summary = "List all software licenses (paginated)")
    public PagedResponse<SoftwareLicense> listLicenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(value = "expand_dropdowns", required = false) Boolean expandDropdowns) {
        int clampedSize = Math.min(Math.max(size, 1), 500);
        List<SoftwareLicense> licenses = licenseRepository.findAll(page, clampedSize);
        long total = licenseRepository.countAll();
        return PagedResponse.of(licenses, total, page, clampedSize);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new software license")
    public SoftwareLicense createLicense(@RequestBody SoftwareLicense license) {
        Instant now = Instant.now();
        license.setId(UUID.randomUUID().toString());
        license.setCreatedAt(now);
        license.setUpdatedAt(now);
        return licenseRepository.save(license);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a software license by ID")
    public ResponseEntity<SoftwareLicense> getLicense(@PathVariable String id) {
        return licenseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/compliance")
    @Operation(summary = "Get license compliance status")
    public LicenseCompliance getCompliance(@PathVariable String id) {
        return complianceService.checkCompliance(id);
    }
}
