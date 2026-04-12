package com.glpi.change.domain.port.in;

import com.glpi.change.domain.model.Change;

/**
 * Driving port — add a task to a change.
 * Requirements: 11.1
 */
public interface AddTaskUseCase {

    Change addTask(AddTaskCommand cmd);
}
