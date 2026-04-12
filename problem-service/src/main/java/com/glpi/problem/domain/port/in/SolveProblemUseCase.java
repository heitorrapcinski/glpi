package com.glpi.problem.domain.port.in;

import com.glpi.problem.domain.model.Problem;

/**
 * Driving port — solve a problem.
 * Requirements: 10.6
 */
public interface SolveProblemUseCase {

    Problem solveProblem(SolveProblemCommand cmd);
}
