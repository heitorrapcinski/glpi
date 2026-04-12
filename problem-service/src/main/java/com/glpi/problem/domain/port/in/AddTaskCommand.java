package com.glpi.problem.domain.port.in;

/**
 * Command to add a task to a problem.
 */
public record AddTaskCommand(
        String problemId,
        String content,
        String assignedUserId,
        int status,
        boolean isPrivate
) {}
