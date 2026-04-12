package com.glpi.change.domain.port.in;

import com.glpi.change.domain.model.Actor;
import com.glpi.change.domain.model.LinkedAsset;
import com.glpi.change.domain.model.PlanningDocuments;

import java.util.List;

/**
 * Command to create a new change.
 * Requirements: 11.2
 */
public record CreateChangeCommand(
        String title,
        String content,
        String entityId,
        int urgency,
        int impact,
        int priority,
        PlanningDocuments planningDocuments,
        List<Actor> actors,
        List<LinkedAsset> linkedAssets
) {}
