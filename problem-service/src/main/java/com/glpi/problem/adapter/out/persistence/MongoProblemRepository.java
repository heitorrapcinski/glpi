package com.glpi.problem.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for ProblemDocument.
 * Requirements: 22.4
 */
public interface MongoProblemRepository extends MongoRepository<ProblemDocument, String> {

    List<ProblemDocument> findAllBy(Pageable pageable);
}
