package com.glpi.change.domain.service;

import com.glpi.change.domain.model.Change;
import com.glpi.change.domain.model.ChangeNotFoundException;
import com.glpi.change.domain.model.ChangeStatus;
import com.glpi.change.domain.model.Solution;
import com.glpi.change.domain.port.out.ChangeRepository;
import com.glpi.change.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service for adding a solution to a change (transitions to SOLVED).
 * Requirements: 11.1
 */
@Service
public class AddSolutionService {

    private final ChangeRepository changeRepository;
    private final EventPublisherPort eventPublisher;
    private final StatusTransitionService statusTransitionService;

    public AddSolutionService(ChangeRepository changeRepository,
                              EventPublisherPort eventPublisher,
                              StatusTransitionService statusTransitionService) {
        this.changeRepository = changeRepository;
        this.eventPublisher = eventPublisher;
        this.statusTransitionService = statusTransitionService;
    }

    public Change addSolution(String changeId, String content, String solutionType, String authorId) {
        Instant now = Instant.now();
        Change change = changeRepository.findById(changeId)
                .orElseThrow(() -> new ChangeNotFoundException(changeId));

        statusTransitionService.validate(change.getStatus(), ChangeStatus.SOLVED);
        change.setSolution(new Solution(content, solutionType, authorId, now));
        change.setStatus(ChangeStatus.SOLVED);
        change.setUpdatedAt(now);

        Change saved = changeRepository.save(change);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ChangeSolved",
                saved.getId(),
                "Change",
                now,
                1,
                saved
        ));

        return saved;
    }
}
