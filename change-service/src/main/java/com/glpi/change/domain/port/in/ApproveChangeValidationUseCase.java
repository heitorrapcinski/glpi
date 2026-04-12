package com.glpi.change.domain.port.in;

import com.glpi.change.domain.model.Change;

/**
 * Driving port — approve a validation step on a change.
 * Requirements: 11.5
 */
public interface ApproveChangeValidationUseCase {

    Change approveValidation(ApproveChangeValidationCommand cmd);
}
