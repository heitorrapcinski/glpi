package com.glpi.change.domain.port.in;

/**
 * Command to add a followup to a change.
 * Requirements: 11.1
 */
public record AddFollowupCommand(
        String changeId,
        String content,
        String authorId,
        boolean isPrivate,
        String source
) {}
