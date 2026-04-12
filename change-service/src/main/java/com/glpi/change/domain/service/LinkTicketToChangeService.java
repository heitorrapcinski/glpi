package com.glpi.change.domain.service;

import com.glpi.change.domain.model.Change;
import com.glpi.change.domain.model.ChangeNotFoundException;
import com.glpi.change.domain.port.in.LinkTicketToChangeCommand;
import com.glpi.change.domain.port.in.LinkTicketToChangeUseCase;
import com.glpi.change.domain.port.out.ChangeRepository;
import com.glpi.change.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing LinkTicketToChangeUseCase.
 * Requirements: 11.6, 11.7
 */
@Service
public class LinkTicketToChangeService implements LinkTicketToChangeUseCase {

    private final ChangeRepository changeRepository;
    private final EventPublisherPort eventPublisher;

    public LinkTicketToChangeService(ChangeRepository changeRepository, EventPublisherPort eventPublisher) {
        this.changeRepository = changeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Change linkTicket(LinkTicketToChangeCommand cmd) {
        Change change = changeRepository.findById(cmd.changeId())
                .orElseThrow(() -> new ChangeNotFoundException(cmd.changeId()));

        if (!change.getLinkedTicketIds().contains(cmd.ticketId())) {
            change.getLinkedTicketIds().add(cmd.ticketId());
            change.setUpdatedAt(Instant.now());
            change = changeRepository.save(change);

            eventPublisher.publish(new DomainEventEnvelope(
                    UUID.randomUUID().toString(),
                    "ChangeTicketLinked",
                    change.getId(),
                    "Change",
                    Instant.now(),
                    1,
                    change
            ));
        }

        return change;
    }
}
