package com.glpi.problem.domain.port.in;

import com.glpi.problem.domain.model.Problem;

/**
 * Driving port — add a followup to a problem.
 */
public interface AddFollowupUseCase {

    Problem addFollowup(AddFollowupCommand cmd);
}
