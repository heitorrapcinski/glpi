package com.glpi.change.domain.port.in;

import com.glpi.change.domain.model.Change;

/**
 * Driving port — create a new change.
 * Requirements: 11.2
 */
public interface CreateChangeUseCase {

    Change createChange(CreateChangeCommand cmd);
}
