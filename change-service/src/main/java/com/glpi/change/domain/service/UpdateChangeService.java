package com.glpi.change.domain.service;

import com.glpi.change.domain.model.Change;
import com.glpi.change.domain.model.ChangeNotFoundException;
import com.glpi.change.domain.port.in.UpdateChangeCommand;
import com.glpi.change.domain.port.in.UpdateChangeUseCase;
import com.glpi.change.domain.port.out.ChangeRepository;
import com.glpi.change.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing UpdateChangeUseCase.
 * Requirements: 11.1
 */
@Service
public class UpdateChangeService implements UpdateChangeUseCase {

    private final ChangeRepository changeRepository;
    private final EventPublisherPort eventPublisher;
    private final StatusTransitionService statusTransitionService;

    public UpdateChangeService(ChangeRepository changeRepository,
                               EventPublisherPort eventPublisher,
                               StatusTransitionService statusTransitionService) {
        this.changeRepository = changeRepository;
        this.eventPublisher = eventPublisher;
        this.statusTransitionService = statusTransitionService;
    }

    @Override
    public Change updateChange(UpdateChangeCommand cmd) {
        Instant now = Instant.now();
        Change change = changeRepository.findById(cmd.id())
                .orElseThrow(() -> new ChangeNotFoundException(cmd.id()));

        if (cmd.status() != null && cmd.status() != change.getStatus()) {
            statusTransitionService.validate(change.getStatus(), cmd.status());
            change.setStatus(cmd.status());
        }
        if (cmd.title() != null) change.setTitle(cmd.title());
        if (cmd.content() != null) change.setContent(cmd.content());
        if (cmd.urgency() > 0) change.setUrgency(cmd.urgency());
        if (cmd.impact() > 0) change.setImpact(cmd.impact());
        if (cmd.priority() > 0) change.setPriority(cmd.priority());
        if (cmd.planningDocuments() != null) change.setPlanningDocuments(cmd.planningDocuments());
        change.setUpdatedAt(now);

        Change saved = changeRepository.save(change);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ChangeUpdated",
                saved.getId(),
                "Change",
                now,
                1,
                saved
        ));

        return saved;
    }
}
