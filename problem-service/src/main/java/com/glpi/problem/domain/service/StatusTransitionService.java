package com.glpi.problem.domain.service;

import com.glpi.problem.domain.model.InvalidStatusTransitionException;
import com.glpi.problem.domain.model.ProblemStatus;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Enforces allowed status transitions for problems.
 * CLOSED can only reopen to INCOMING or ACCEPTED.
 * Requirements: 26.4
 */
@Service
public class StatusTransitionService {

    private static final Map<ProblemStatus, Set<ProblemStatus>> ALLOWED = Map.of(
            ProblemStatus.INCOMING,  EnumSet.of(ProblemStatus.ACCEPTED, ProblemStatus.ASSIGNED, ProblemStatus.WAITING, ProblemStatus.SOLVED, ProblemStatus.CLOSED),
            ProblemStatus.ACCEPTED,  EnumSet.of(ProblemStatus.ASSIGNED, ProblemStatus.PLANNED, ProblemStatus.WAITING, ProblemStatus.SOLVED, ProblemStatus.CLOSED),
            ProblemStatus.ASSIGNED,  EnumSet.of(ProblemStatus.PLANNED, ProblemStatus.WAITING, ProblemStatus.SOLVED, ProblemStatus.CLOSED, ProblemStatus.OBSERVED),
            ProblemStatus.PLANNED,   EnumSet.of(ProblemStatus.ASSIGNED, ProblemStatus.WAITING, ProblemStatus.SOLVED, ProblemStatus.CLOSED),
            ProblemStatus.WAITING,   EnumSet.of(ProblemStatus.INCOMING, ProblemStatus.ASSIGNED, ProblemStatus.PLANNED, ProblemStatus.SOLVED),
            ProblemStatus.SOLVED,    EnumSet.of(ProblemStatus.CLOSED, ProblemStatus.OBSERVED, ProblemStatus.INCOMING),
            ProblemStatus.OBSERVED,  EnumSet.of(ProblemStatus.CLOSED, ProblemStatus.ASSIGNED),
            ProblemStatus.CLOSED,    EnumSet.of(ProblemStatus.INCOMING, ProblemStatus.ACCEPTED)
    );

    public void validate(ProblemStatus from, ProblemStatus to) {
        if (from == to) return;
        Set<ProblemStatus> allowed = ALLOWED.getOrDefault(from, EnumSet.noneOf(ProblemStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
