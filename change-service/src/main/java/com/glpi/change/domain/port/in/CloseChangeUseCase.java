package com.glpi.change.domain.port.in;

import com.glpi.change.domain.model.Change;

/**
 * Driving port — close a change.
 * Requirements: 11.9
 */
public interface CloseChangeUseCase {

    Change closeChange(String changeId);
}
