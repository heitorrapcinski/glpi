package com.glpi.problem.domain.port.in;

/**
 * Command to solve a problem.
 * Requirements: 10.6
 */
public record SolveProblemCommand(
        String problemId,
        String solutionContent,
        String solutionType,
        String authorId
) {}
