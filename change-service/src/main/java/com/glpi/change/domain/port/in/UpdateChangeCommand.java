package com.glpi.change.domain.port.in;

import com.glpi.change.domain.model.ChangeStatus;
import com.glpi.change.domain.model.PlanningDocuments;

/**
 * Command to update an existing change.
 * Requirements: 11.1
 */
public record UpdateChangeCommand(
        String id,
        String title,
        String content,
        ChangeStatus status,
        int urgency,
        int impact,
        int priority,
        PlanningDocuments planningDocuments
) {}
