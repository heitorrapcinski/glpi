package com.glpi.change.domain.service;

import com.glpi.change.domain.model.Change;
import com.glpi.change.domain.model.ChangeNotFoundException;
import com.glpi.change.domain.model.ChangeStatus;
import com.glpi.change.domain.port.in.CloseChangeUseCase;
import com.glpi.change.domain.port.out.ChangeRepository;
import com.glpi.change.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing CloseChangeUseCase.
 * Requirements: 11.9
 */
@Service
public class CloseChangeService implements CloseChangeUseCase {

    private final ChangeRepository changeRepository;
    private final EventPublisherPort eventPublisher;
    private final StatusTransitionService statusTransitionService;

    public CloseChangeService(ChangeRepository changeRepository,
                              EventPublisherPort eventPublisher,
                              StatusTransitionService statusTransitionService) {
        this.changeRepository = changeRepository;
        this.eventPublisher = eventPublisher;
        this.statusTransitionService = statusTransitionService;
    }

    @Override
    public Change closeChange(String changeId) {
        Instant now = Instant.now();
        Change change = changeRepository.findById(changeId)
                .orElseThrow(() -> new ChangeNotFoundException(changeId));

        statusTransitionService.validate(change.getStatus(), ChangeStatus.CLOSED);
        change.setStatus(ChangeStatus.CLOSED);
        change.setClosedAt(now);
        change.setUpdatedAt(now);

        Change saved = changeRepository.save(change);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ChangeClosed",
                saved.getId(),
                "Change",
                now,
                1,
                saved
        ));

        return saved;
    }
}
