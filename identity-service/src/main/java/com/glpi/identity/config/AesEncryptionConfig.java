package com.glpi.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Configuration for AES-256 encryption used to protect tokens at rest.
 */
@Configuration
public class AesEncryptionConfig {

    @Value("${identity.encryption.aes-key:}")
    private String aesKeyBase64;

    @Bean
    public SecretKey aesSecretKey() throws NoSuchAlgorithmException {
        if (aesKeyBase64 != null && !aesKeyBase64.isBlank()) {
            byte[] keyBytes = Base64.getDecoder().decode(aesKeyBase64);
            return new SecretKeySpec(keyBytes, "AES");
        }
        // Generate a random key for development (not suitable for production)
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }
}
