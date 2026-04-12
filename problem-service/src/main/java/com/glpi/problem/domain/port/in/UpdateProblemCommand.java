package com.glpi.problem.domain.port.in;

import com.glpi.problem.domain.model.ProblemStatus;

/**
 * Command to update an existing problem.
 */
public record UpdateProblemCommand(
        String id,
        String title,
        String content,
        ProblemStatus status,
        int urgency,
        int impact,
        int priority,
        String impactContent,
        String causeContent,
        String symptomContent
) {}
