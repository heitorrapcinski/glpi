package com.glpi.problem.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.problem.domain.model.Problem;
import com.glpi.problem.domain.model.ProblemNotFoundException;
import com.glpi.problem.domain.port.in.LinkTicketToProblemCommand;
import com.glpi.problem.domain.port.in.LinkTicketToProblemUseCase;
import com.glpi.problem.domain.port.out.EventPublisherPort;
import com.glpi.problem.domain.port.out.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing LinkTicketToProblemUseCase.
 * Requirements: 10.3, 10.4
 */
@Service
public class LinkTicketToProblemService implements LinkTicketToProblemUseCase {

    private final ProblemRepository problemRepository;
    private final EventPublisherPort eventPublisher;

    public LinkTicketToProblemService(ProblemRepository problemRepository, EventPublisherPort eventPublisher) {
        this.problemRepository = problemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Problem linkTicket(LinkTicketToProblemCommand cmd) {
        Problem problem = problemRepository.findById(cmd.problemId())
                .orElseThrow(() -> new ProblemNotFoundException(cmd.problemId()));

        if (!problem.getLinkedTicketIds().contains(cmd.ticketId())) {
            problem.getLinkedTicketIds().add(cmd.ticketId());
            problem.setUpdatedAt(Instant.now());
            problem = problemRepository.save(problem);

            eventPublisher.publish(new DomainEventEnvelope(
                    UUID.randomUUID().toString(),
                    "ProblemTicketLinked",
                    problem.getId(),
                    "Problem",
                    Instant.now(),
                    1,
                    problem
            ));
        }

        return problem;
    }
}
