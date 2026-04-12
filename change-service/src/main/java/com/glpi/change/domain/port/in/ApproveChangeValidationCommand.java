package com.glpi.change.domain.port.in;

/**
 * Command to approve a validation step on a change.
 * Requirements: 11.5
 */
public record ApproveChangeValidationCommand(
        String changeId,
        String validationId,
        String comment
) {}
