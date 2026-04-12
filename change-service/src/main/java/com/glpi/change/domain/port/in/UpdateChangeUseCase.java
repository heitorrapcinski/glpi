package com.glpi.change.domain.port.in;

import com.glpi.change.domain.model.Change;

/**
 * Driving port — update an existing change.
 * Requirements: 11.1
 */
public interface UpdateChangeUseCase {

    Change updateChange(UpdateChangeCommand cmd);
}
