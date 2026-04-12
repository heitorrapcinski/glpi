package com.glpi.identity.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for refresh token documents.
 */
public interface MongoRefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {

    Optional<RefreshTokenDocument> findByTokenHash(String tokenHash);

    List<RefreshTokenDocument> findAllByFamilyId(String familyId);

    @Query(value = "{ 'familyId': ?0 }", fields = "{ '_id': 1 }")
    List<RefreshTokenDocument> findIdsByFamilyId(String familyId);
}
