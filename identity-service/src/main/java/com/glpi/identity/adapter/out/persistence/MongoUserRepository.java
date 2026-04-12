package com.glpi.identity.adapter.out.persistence;

import com.glpi.identity.domain.model.AuthType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for UserDocument.
 */
public interface MongoUserRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByUsername(String username);

    boolean existsByUsernameAndAuthType(String username, AuthType authType);
}
