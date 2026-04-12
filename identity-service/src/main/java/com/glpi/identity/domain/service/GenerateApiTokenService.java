package com.glpi.identity.domain.service;

import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.model.UserNotFoundException;
import com.glpi.identity.domain.port.in.GenerateApiTokenUseCase;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * Domain service implementing GenerateApiTokenUseCase.
 * Generates a UUID-based token and encrypts it with AES-256/GCM/NoPadding.
 */
@Service
public class GenerateApiTokenService implements GenerateApiTokenUseCase {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final UserRepository userRepository;
    private final SecretKey aesSecretKey;

    public GenerateApiTokenService(UserRepository userRepository, SecretKey aesSecretKey) {
        this.userRepository = userRepository;
        this.aesSecretKey = aesSecretKey;
    }

    @Override
    public String generateApiToken(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String plainToken = UUID.randomUUID().toString();
        String encryptedToken = encrypt(plainToken);

        user.setPersonalToken(encryptedToken);
        userRepository.save(user);

        return plainToken;
    }

    String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesSecretKey, parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            // Prepend IV to ciphertext for storage
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token", e);
        }
    }

    String decrypt(String encryptedBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] ciphertext = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesSecretKey, parameterSpec);

            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt token", e);
        }
    }
}
