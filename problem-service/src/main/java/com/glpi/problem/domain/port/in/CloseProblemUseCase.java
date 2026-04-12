package com.glpi.problem.domain.port.in;

import com.glpi.problem.domain.model.Problem;

/**
 * Driving port — close a problem.
 * Requirements: 10.9
 */
public interface CloseProblemUseCase {

    Problem closeProblem(String problemId);
}
