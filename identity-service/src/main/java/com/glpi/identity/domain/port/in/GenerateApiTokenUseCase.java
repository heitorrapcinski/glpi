package com.glpi.identity.domain.port.in;

/**
 * Driving port: use case for generating a personal API token for a user.
 */
public interface GenerateApiTokenUseCase {

    /**
     * Generates a UUID-based API token, encrypts it with AES-256, and stores it.
     *
     * @param userId the ID of the user
     * @return the plaintext token (returned once to the caller, not stored in plaintext)
     */
    String generateApiToken(String userId);
}
