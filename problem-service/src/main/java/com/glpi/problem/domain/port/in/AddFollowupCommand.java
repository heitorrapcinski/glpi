package com.glpi.problem.domain.port.in;

/**
 * Command to add a followup to a problem.
 */
public record AddFollowupCommand(
        String problemId,
        String content,
        String authorId,
        boolean isPrivate,
        String source
) {}
