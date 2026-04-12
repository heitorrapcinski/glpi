package com.glpi.change.domain.service;

import com.glpi.change.domain.model.Change;
import com.glpi.change.domain.model.ChangeStatus;
import com.glpi.change.domain.model.PlanningDocuments;
import com.glpi.change.domain.port.in.CreateChangeCommand;
import com.glpi.change.domain.port.in.CreateChangeUseCase;
import com.glpi.change.domain.port.out.ChangeRepository;
import com.glpi.change.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Domain service implementing CreateChangeUseCase.
 * Requirements: 11.2
 */
@Service
public class CreateChangeService implements CreateChangeUseCase {

    private final ChangeRepository changeRepository;
    private final EventPublisherPort eventPublisher;

    public CreateChangeService(ChangeRepository changeRepository, EventPublisherPort eventPublisher) {
        this.changeRepository = changeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Change createChange(CreateChangeCommand cmd) {
        Instant now = Instant.now();

        Change change = new Change();
        change.setId(UUID.randomUUID().toString());
        change.setStatus(ChangeStatus.INCOMING);
        change.setTitle(cmd.title());
        change.setContent(cmd.content());
        change.setEntityId(cmd.entityId());
        change.setUrgency(cmd.urgency());
        change.setImpact(cmd.impact());
        change.setPriority(cmd.priority());
        change.setPlanningDocuments(cmd.planningDocuments() != null ? cmd.planningDocuments() : new PlanningDocuments());
        change.setActors(cmd.actors() != null ? cmd.actors() : new ArrayList<>());
        change.setLinkedAssets(cmd.linkedAssets() != null ? cmd.linkedAssets() : new ArrayList<>());
        change.setCreatedAt(now);
        change.setUpdatedAt(now);

        Change saved = changeRepository.save(change);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ChangeCreated",
                saved.getId(),
                "Change",
                now,
                1,
                saved
        ));

        return saved;
    }
}
