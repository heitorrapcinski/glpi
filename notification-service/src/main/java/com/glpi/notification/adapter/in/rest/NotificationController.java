package com.glpi.notification.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.notification.domain.model.NotificationTemplate;
import com.glpi.notification.domain.model.QueuedNotification;
import com.glpi.notification.domain.model.TemplateNotFoundException;
import com.glpi.notification.domain.port.in.CreateTemplateCommand;
import com.glpi.notification.domain.port.in.ManageTemplateUseCase;
import com.glpi.notification.domain.port.in.UpdateTemplateCommand;
import com.glpi.notification.domain.port.out.NotificationTemplateRepository;
import com.glpi.notification.domain.port.out.QueuedNotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for notification templates and queued notifications.
 * Requirements: 16.4, 16.7, 19.1
 */
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Notification template and queue management")
public class NotificationController {

    private final ManageTemplateUseCase manageTemplateUseCase;
    private final NotificationTemplateRepository templateRepository;
    private final QueuedNotificationRepository queuedNotificationRepository;

    public NotificationController(ManageTemplateUseCase manageTemplateUseCase,
                                  NotificationTemplateRepository templateRepository,
                                  QueuedNotificationRepository queuedNotificationRepository) {
        this.manageTemplateUseCase = manageTemplateUseCase;
        this.templateRepository = templateRepository;
        this.queuedNotificationRepository = queuedNotificationRepository;
    }

    // ---- Templates ----

    @GetMapping("/templates")
    @Operation(summary = "List all notification templates (paginated)")
    public PagedResponse<NotificationTemplate> listTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<NotificationTemplate> templates = templateRepository.findAll(page, size);
        long total = templateRepository.countAll();
        return PagedResponse.of(templates, total, page, size);
    }

    @PostMapping("/templates")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a notification template")
    public NotificationTemplate createTemplate(@RequestBody CreateTemplateCommand command) {
        return manageTemplateUseCase.createTemplate(command);
    }

    @GetMapping("/templates/{id}")
    @Operation(summary = "Get a notification template by ID")
    public ResponseEntity<NotificationTemplate> getTemplate(@PathVariable String id) {
        return templateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/templates/{id}")
    @Operation(summary = "Update a notification template")
    public NotificationTemplate updateTemplate(@PathVariable String id,
                                               @RequestBody UpdateTemplateCommand command) {
        return manageTemplateUseCase.updateTemplate(id, command);
    }

    // ---- Queue ----

    @GetMapping("/queue")
    @Operation(summary = "List queued notifications (paginated)")
    public PagedResponse<QueuedNotification> listQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<QueuedNotification> notifications = queuedNotificationRepository.findAll(page, size);
        long total = queuedNotificationRepository.countAll();
        return PagedResponse.of(notifications, total, page, size);
    }
}
