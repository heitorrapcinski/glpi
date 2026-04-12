package com.glpi.identity.domain.service;

import com.glpi.identity.domain.model.Entity;
import com.glpi.identity.domain.model.EntityConfig;
import com.glpi.identity.domain.model.EntityNotFoundException;
import com.glpi.identity.domain.port.in.ResolveEntityConfigUseCase;
import com.glpi.identity.domain.port.out.EntityRepository;
import org.springframework.stereotype.Service;

/**
 * Domain service implementing ResolveEntityConfigUseCase.
 * Traverses the entity tree upward for CONFIG_PARENT (-2) fields
 * until a non-inherited value is found.
 */
@Service
public class ResolveEntityConfigService implements ResolveEntityConfigUseCase {

    private final EntityRepository entityRepository;

    public ResolveEntityConfigService(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    @Override
    public EntityConfig resolveConfig(String entityId) {
        Entity entity = entityRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException(entityId));

        EntityConfig resolved = new EntityConfig();
        resolved.setDefaultTicketType(resolveIntField(entity, "defaultTicketType"));
        resolved.setAutoAssignMode(resolveIntField(entity, "autoAssignMode"));
        resolved.setAutoCloseDelay(resolveIntField(entity, "autoCloseDelay"));
        resolved.setCalendarId(resolveIntField(entity, "calendarId"));
        resolved.setSatisfactionSurveyEnabled(resolveIntField(entity, "satisfactionSurveyEnabled"));
        resolved.setNotificationSenderEmail(resolveStringField(entity, "notificationSenderEmail"));

        return resolved;
    }

    private int resolveIntField(Entity startEntity, String fieldName) {
        Entity current = startEntity;
        while (current != null) {
            int value = getIntField(current.getConfig(), fieldName);
            if (value != EntityConfig.CONFIG_PARENT) {
                return value;
            }
            if (current.getParentId() == null) {
                return EntityConfig.CONFIG_PARENT;
            }
            current = entityRepository.findById(current.getParentId()).orElse(null);
        }
        return EntityConfig.CONFIG_PARENT;
    }

    private String resolveStringField(Entity startEntity, String fieldName) {
        Entity current = startEntity;
        while (current != null) {
            String value = getStringField(current.getConfig(), fieldName);
            if (value != null) {
                return value;
            }
            if (current.getParentId() == null) {
                return null;
            }
            current = entityRepository.findById(current.getParentId()).orElse(null);
        }
        return null;
    }

    private int getIntField(EntityConfig config, String fieldName) {
        return switch (fieldName) {
            case "defaultTicketType" -> config.getDefaultTicketType();
            case "autoAssignMode" -> config.getAutoAssignMode();
            case "autoCloseDelay" -> config.getAutoCloseDelay();
            case "calendarId" -> config.getCalendarId();
            case "satisfactionSurveyEnabled" -> config.getSatisfactionSurveyEnabled();
            default -> EntityConfig.CONFIG_PARENT;
        };
    }

    private String getStringField(EntityConfig config, String fieldName) {
        return switch (fieldName) {
            case "notificationSenderEmail" -> config.getNotificationSenderEmail();
            default -> null;
        };
    }
}
