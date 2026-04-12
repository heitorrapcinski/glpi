package com.glpi.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Loads the RS256 key pair from application.yml (base64-encoded PEM content).
 * Falls back to a generated key pair for development when no keys are configured.
 */
@Configuration
public class JwtConfig {

    @Value("${identity.jwt.private-key:}")
    private String privateKeyBase64;

    @Value("${identity.jwt.public-key:}")
    private String publicKeyBase64;

    @Bean
    public PrivateKey jwtPrivateKey() throws Exception {
        if (privateKeyBase64 != null && !privateKeyBase64.isBlank()) {
            byte[] keyBytes = Base64.getDecoder().decode(stripPemHeaders(privateKeyBase64));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }
        // Generate ephemeral key pair for development
        return DevKeyPairHolder.INSTANCE.getPrivate();
    }

    @Bean
    public PublicKey jwtPublicKey() throws Exception {
        if (publicKeyBase64 != null && !publicKeyBase64.isBlank()) {
            byte[] keyBytes = Base64.getDecoder().decode(stripPemHeaders(publicKeyBase64));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
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

        static java.security.PrivateKey getPrivate() { return INSTANCE.getPrivate(); }
        static java.security.PublicKey getPublic() { return INSTANCE.getPublic(); }
    }
}
