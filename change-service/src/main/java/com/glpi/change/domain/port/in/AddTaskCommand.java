package com.glpi.change.domain.port.in;

/**
 * Command to add a task to a change.
 * Requirements: 11.1
 */
public record AddTaskCommand(
        String changeId,
        String content,
        String assignedUserId,
        int status,
        boolean isPrivate
) {}
