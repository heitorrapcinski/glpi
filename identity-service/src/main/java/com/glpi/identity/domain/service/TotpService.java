package com.glpi.identity.domain.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * TOTP (RFC 6238) service for Two-Factor Authentication.
 * Uses HMAC-SHA1 with a 30-second time step and ±1 step tolerance.
 */
@Service
public class TotpService {

    private static final int TIME_STEP_SECONDS = 30;
    private static final int DIGITS = 6;
    private static final int WINDOW = 1; // ±1 step tolerance
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    /** Base32 alphabet (RFC 4648). */
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * Generates a 20-byte random TOTP secret encoded as Base32.
     */
    public String generateSecret() {
        byte[] secret = new byte[20];
        new SecureRandom().nextBytes(secret);
        return base32Encode(secret);
    }

    /**
     * Verifies a TOTP code against the given Base32-encoded secret.
     * Accepts codes within ±1 time step of the current time.
     *
     * @param secret Base32-encoded TOTP secret
     * @param code   6-digit TOTP code provided by the user
     * @return true if the code is valid
     */
    public boolean verifyCode(String secret, int code) {
        byte[] keyBytes = base32Decode(secret);
        long currentStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;

        for (int delta = -WINDOW; delta <= WINDOW; delta++) {
            int expected = computeTotp(keyBytes, currentStep + delta);
            if (expected == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes the TOTP value for a given time step counter.
     */
    int computeTotp(byte[] key, long counter) {
        try {
            byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(counterBytes);

            // Dynamic truncation (RFC 4226 §5.4)
            int offset = hash[hash.length - 1] & 0x0F;
            int truncated = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int mod = (int) Math.pow(10, DIGITS);
            return truncated % mod;
        } catch (Exception e) {
            throw new RuntimeException("TOTP computation failed", e);
        }
    }

    // --- Base32 encoding/decoding ---

    static String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                sb.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32_CHARS.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return sb.toString();
    }

    static byte[] base32Decode(String encoded) {
        String upper = encoded.toUpperCase().replaceAll("[^A-Z2-7]", "");
        int outputLength = upper.length() * 5 / 8;
        byte[] result = new byte[outputLength];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;

        for (char c : upper.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                result[index++] = (byte) ((buffer >> bitsLeft) & 0xFF);
            }
        }
        return result;
    }
}
