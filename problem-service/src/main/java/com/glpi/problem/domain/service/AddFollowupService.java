package com.glpi.problem.domain.service;

import com.glpi.problem.domain.model.Followup;
import com.glpi.problem.domain.model.Problem;
import com.glpi.problem.domain.model.ProblemNotFoundException;
import com.glpi.problem.domain.port.in.AddFollowupCommand;
import com.glpi.problem.domain.port.in.AddFollowupUseCase;
import com.glpi.problem.domain.port.out.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing AddFollowupUseCase.
 */
@Service
public class AddFollowupService implements AddFollowupUseCase {

    private final ProblemRepository problemRepository;

    public AddFollowupService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Override
    public Problem addFollowup(AddFollowupCommand cmd) {
        Instant now = Instant.now();
        Problem problem = problemRepository.findById(cmd.problemId())
                .orElseThrow(() -> new ProblemNotFoundException(cmd.problemId()));

        Followup followup = new Followup(
                UUID.randomUUID().toString(),
                cmd.content(),
                cmd.authorId(),
                cmd.isPrivate(),
                cmd.source(),
                now
        );
        problem.getFollowups().add(followup);
        problem.setUpdatedAt(now);

        return problemRepository.save(problem);
    }
}
