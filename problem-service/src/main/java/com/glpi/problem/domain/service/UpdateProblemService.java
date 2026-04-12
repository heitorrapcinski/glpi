package com.glpi.problem.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.problem.domain.model.Problem;
import com.glpi.problem.domain.model.ProblemNotFoundException;
import com.glpi.problem.domain.port.in.UpdateProblemCommand;
import com.glpi.problem.domain.port.in.UpdateProblemUseCase;
import com.glpi.problem.domain.port.out.EventPublisherPort;
import com.glpi.problem.domain.port.out.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing UpdateProblemUseCase.
 */
@Service
public class UpdateProblemService implements UpdateProblemUseCase {

    private final ProblemRepository problemRepository;
    private final EventPublisherPort eventPublisher;
    private final StatusTransitionService statusTransitionService;

    public UpdateProblemService(ProblemRepository problemRepository,
                                EventPublisherPort eventPublisher,
                                StatusTransitionService statusTransitionService) {
        this.problemRepository = problemRepository;
        this.eventPublisher = eventPublisher;
        this.statusTransitionService = statusTransitionService;
    }

    @Override
    public Problem updateProblem(UpdateProblemCommand cmd) {
        Problem problem = problemRepository.findById(cmd.id())
                .orElseThrow(() -> new ProblemNotFoundException(cmd.id()));

        if (cmd.status() != null && cmd.status() != problem.getStatus()) {
            statusTransitionService.validate(problem.getStatus(), cmd.status());
            problem.setStatus(cmd.status());
        }
        if (cmd.title() != null) problem.setTitle(cmd.title());
        if (cmd.content() != null) problem.setContent(cmd.content());
        if (cmd.urgency() > 0) problem.setUrgency(cmd.urgency());
        if (cmd.impact() > 0) problem.setImpact(cmd.impact());
        if (cmd.priority() > 0) problem.setPriority(cmd.priority());
        if (cmd.impactContent() != null) problem.setImpactContent(cmd.impactContent());
        if (cmd.causeContent() != null) problem.setCauseContent(cmd.causeContent());
        if (cmd.symptomContent() != null) problem.setSymptomContent(cmd.symptomContent());
        problem.setUpdatedAt(Instant.now());

        Problem saved = problemRepository.save(problem);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ProblemUpdated",
                saved.getId(),
                "Problem",
                Instant.now(),
                1,
                saved
        ));

        return saved;
    }
}
