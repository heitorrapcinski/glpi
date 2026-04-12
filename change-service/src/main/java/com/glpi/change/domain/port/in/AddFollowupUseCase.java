package com.glpi.change.domain.port.in;

import com.glpi.change.domain.model.Change;

/**
 * Driving port — add a followup to a change.
 * Requirements: 11.1
 */
public interface AddFollowupUseCase {

    Change addFollowup(AddFollowupCommand cmd);
}
