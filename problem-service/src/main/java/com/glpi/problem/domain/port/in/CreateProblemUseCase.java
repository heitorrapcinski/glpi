package com.glpi.problem.domain.port.in;

import com.glpi.problem.domain.model.Problem;

/**
 * Driving port — create a new problem.
 * Requirements: 10.2
 */
public interface CreateProblemUseCase {

    Problem createProblem(CreateProblemCommand cmd);
}
