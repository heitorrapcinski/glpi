package com.glpi.problem.domain.port.in;

import com.glpi.problem.domain.model.Actor;
import com.glpi.problem.domain.model.LinkedAsset;

import java.util.List;

/**
 * Command to create a new problem.
 * Requirements: 10.2
 */
public record CreateProblemCommand(
        String title,
        String content,
        String entityId,
        int urgency,
        int impact,
        int priority,
        String impactContent,
        String causeContent,
        String symptomContent,
        List<Actor> actors,
        List<LinkedAsset> linkedAssets
) {}
