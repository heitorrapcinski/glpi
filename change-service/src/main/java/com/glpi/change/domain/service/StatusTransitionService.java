package com.glpi.change.domain.service;

import com.glpi.change.domain.model.ChangeStatus;
import com.glpi.change.domain.model.InvalidStatusTransitionException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Enforces allowed status transitions for changes.
 * INCOMING cannot go directly to SOLVED — must pass through EVALUATION or APPROVAL.
 * Requirements: 11.1, 26.3
 */
@Service
public class StatusTransitionService {

    private static final Map<ChangeStatus, Set<ChangeStatus>> ALLOWED = Map.ofEntries(
            Map.entry(ChangeStatus.INCOMING,     EnumSet.of(ChangeStatus.EVALUATION, ChangeStatus.APPROVAL, ChangeStatus.WAITING, ChangeStatus.CANCELED, ChangeStatus.REFUSED)),
            Map.entry(ChangeStatus.EVALUATION,   EnumSet.of(ChangeStatus.APPROVAL, ChangeStatus.ACCEPTED, ChangeStatus.WAITING, ChangeStatus.REFUSED, ChangeStatus.CANCELED)),
            Map.entry(ChangeStatus.APPROVAL,     EnumSet.of(ChangeStatus.ACCEPTED, ChangeStatus.REFUSED, ChangeStatus.WAITING, ChangeStatus.CANCELED)),
            Map.entry(ChangeStatus.ACCEPTED,     EnumSet.of(ChangeStatus.WAITING, ChangeStatus.TEST, ChangeStatus.QUALIFICATION, ChangeStatus.SOLVED, ChangeStatus.CANCELED)),
            Map.entry(ChangeStatus.WAITING,      EnumSet.of(ChangeStatus.INCOMING, ChangeStatus.EVALUATION, ChangeStatus.APPROVAL, ChangeStatus.ACCEPTED, ChangeStatus.TEST)),
            Map.entry(ChangeStatus.TEST,         EnumSet.of(ChangeStatus.QUALIFICATION, ChangeStatus.ACCEPTED, ChangeStatus.WAITING, ChangeStatus.CANCELED)),
            Map.entry(ChangeStatus.QUALIFICATION,EnumSet.of(ChangeStatus.SOLVED, ChangeStatus.TEST, ChangeStatus.WAITING, ChangeStatus.CANCELED)),
            Map.entry(ChangeStatus.SOLVED,       EnumSet.of(ChangeStatus.CLOSED, ChangeStatus.OBSERVED)),
            Map.entry(ChangeStatus.OBSERVED,     EnumSet.of(ChangeStatus.CLOSED, ChangeStatus.ACCEPTED)),
            Map.entry(ChangeStatus.CLOSED,       EnumSet.of(ChangeStatus.INCOMING)),
            Map.entry(ChangeStatus.CANCELED,     EnumSet.of(ChangeStatus.INCOMING)),
            Map.entry(ChangeStatus.REFUSED,      EnumSet.of(ChangeStatus.INCOMING, ChangeStatus.EVALUATION))
    );

    public void validate(ChangeStatus from, ChangeStatus to) {
        if (from == to) return;
        Set<ChangeStatus> allowed = ALLOWED.getOrDefault(from, EnumSet.noneOf(ChangeStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
