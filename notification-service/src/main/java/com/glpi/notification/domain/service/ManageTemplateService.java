package com.glpi.notification.domain.service;

import com.glpi.notification.domain.model.NotificationTemplate;
import com.glpi.notification.domain.model.TemplateNotFoundException;
import com.glpi.notification.domain.port.in.CreateTemplateCommand;
import com.glpi.notification.domain.port.in.ManageTemplateUseCase;
import com.glpi.notification.domain.port.in.UpdateTemplateCommand;
import com.glpi.notification.domain.port.out.NotificationTemplateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing template CRUD operations.
 * Requirements: 16.4
 */
@Service
public class ManageTemplateService implements ManageTemplateUseCase {

    private final NotificationTemplateRepository templateRepository;

    public ManageTemplateService(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public NotificationTemplate createTemplate(CreateTemplateCommand command) {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(UUID.randomUUID().toString());
        template.setEventName(command.eventName());
        template.setSubjectTemplate(command.subjectTemplate());
        template.setBodyTemplate(command.bodyTemplate());
        template.setLanguage(command.language());
        template.setCreatedAt(Instant.now());
        template.setUpdatedAt(Instant.now());
        return templateRepository.save(template);
    }

    @Override
    public NotificationTemplate updateTemplate(String id, UpdateTemplateCommand command) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException(id));
        if (command.subjectTemplate() != null) {
            template.setSubjectTemplate(command.subjectTemplate());
        }
        if (command.bodyTemplate() != null) {
            template.setBodyTemplate(command.bodyTemplate());
        }
        if (command.language() != null) {
            template.setLanguage(command.language());
        }
        template.setUpdatedAt(Instant.now());
        return templateRepository.save(template);
    }
}
