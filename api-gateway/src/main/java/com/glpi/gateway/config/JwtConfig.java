package com.glpi.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Loads the RS256 public key from application.yml (base64-encoded PEM content).
 * Falls back to a generated key pair for development when no key is configured.
 */
@Configuration
public class JwtConfig {

    @Value("${gateway.jwt.public-key:}")
    private String publicKeyBase64;

    @Bean
    public PublicKey jwtPublicKey() throws Exception {
        if (publicKeyBase64 != null && !publicKeyBase64.isBlank()) {
            byte[] keyBytes = Base64.getDecoder().decode(stripPemHeaders(publicKeyBase64));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
        // Generate ephemeral key pair for development
        return DevKeyPairHolder.INSTANCE.getPublic();
    }

    private String stripPemHeaders(String pem) {
        return pem.replaceAll("-----[A-Z ]+-----", "")
                  .replaceAll("\\s+", "");
    }

    /** Singleton holder for the ephemeral dev key pair. */
    private static final class DevKeyPairHolder {
        static final java.security.KeyPair INSTANCE;

        static {
            try {
                java.security.KeyPairGenerator gen = java.security.KeyPairGenerator.getInstance("RSA");
                gen.initialize(2048);
                INSTANCE = gen.generateKeyPair();
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        static java.security.PublicKey getPublic() { return INSTANCE.getPublic(); }
    }
}
