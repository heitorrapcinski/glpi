package com.glpi.identity.domain.port.out;

import com.glpi.identity.domain.model.Profile;

import java.util.List;
import java.util.Optional;

/**
 * Driven port: persistence contract for the Profile aggregate.
 */
public interface ProfileRepository {

    Optional<Profile> findById(String id);

    Profile save(Profile profile);

    void delete(String id);

    List<Profile> findAll();

    Optional<Profile> findDefault();

    long countProfilesWithUpdateRightOn(String resource);
}
