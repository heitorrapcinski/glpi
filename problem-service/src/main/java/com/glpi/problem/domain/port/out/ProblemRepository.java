package com.glpi.problem.domain.port.out;

import com.glpi.problem.domain.model.Problem;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for Problem aggregate.
 * Requirements: 22.4
 */
public interface ProblemRepository {

    Optional<Problem> findById(String id);

    Problem save(Problem problem);

    void delete(String id);

    List<Problem> findAll(int page, int size);

    long countAll();
}
