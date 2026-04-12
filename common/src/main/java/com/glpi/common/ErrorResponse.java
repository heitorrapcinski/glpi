package com.glpi.common;

import java.time.Instant;
import java.util.List;

/**
 * Uniform error response body returned by all microservices on any non-2xx HTTP response.
 *
 * <p>Field semantics:
 * <ul>
 *   <li>{@code timestamp}  – UTC instant when the error occurred</li>
 *   <li>{@code status}     – HTTP status code (e.g. 400, 404, 422)</li>
 *   <li>{@code errorCode}  – Machine-readable error code from the error code registry
 *                            (e.g. "DUPLICATE_USERNAME", "INVALID_STATUS_TRANSITION")</li>
 *   <li>{@code message}    – Human-readable description of the error</li>
 *   <li>{@code details}    – Optional list of field-level validation messages</li>
 *   <li>{@code traceId}    – Distributed trace ID for correlating logs across services</li>
 * </ul>
 *
 * <p>Validates: Requirements 19.3, 19.4, 19.5
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        List<String> details,
        String traceId
) {
    /**
     * Convenience factory for simple errors without field-level details.
     */
    public static ErrorResponse of(int status, String errorCode, String message, String traceId) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, List.of(), traceId);
    }

    /**
     * Convenience factory for validation errors with field-level details.
     */
    public static ErrorResponse withDetails(int status, String errorCode, String message,
                                            List<String> details, String traceId) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, details, traceId);
    }
}
