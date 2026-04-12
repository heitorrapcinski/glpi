package com.glpi.problem.domain.port.in;

import com.glpi.problem.domain.model.Problem;

/**
 * Driving port — add a task to a problem.
 */
public interface AddTaskUseCase {

    Problem addTask(AddTaskCommand cmd);
}
