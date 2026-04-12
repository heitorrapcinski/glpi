package com.glpi.problem.domain.port.in;

import com.glpi.problem.domain.model.Problem;

/**
 * Driving port — update an existing problem.
 */
public interface UpdateProblemUseCase {

    Problem updateProblem(UpdateProblemCommand cmd);
}
