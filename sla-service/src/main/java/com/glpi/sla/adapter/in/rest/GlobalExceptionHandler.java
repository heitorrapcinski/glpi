package com.glpi.sla.adapter.in.rest;

import com.glpi.common.ErrorResponse;
import com.glpi.sla.domain.model.CalendarNotFoundException;
import com.glpi.sla.domain.model.OlaNotFoundException;
import com.glpi.sla.domain.model.SlaNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * Global exception handler mapping domain exceptions to standardized HTTP error responses.
 * Requirements: 25.1, 19.3, 19.4, 19.5
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 404 Not Found ---

    @ExceptionHandler(SlaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSlaNotFound(SlaNotFoundException ex) {
        return notFound("SLA_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(OlaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOlaNotFound(OlaNotFoundException ex) {
        return notFound("OLA_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(CalendarNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCalendarNotFound(CalendarNotFoundException ex) {
        return notFound("CALENDAR_NOT_FOUND", ex.getMessage());
    }

    // --- 400 Bad Request ---

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "BAD_REQUEST", ex.getMessage(), traceId()));
    }

    // --- 500 Internal Server Error ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "INTERNAL_ERROR",
                        "An unexpected error occurred", traceId()));
    }

    // --- Helpers ---

    private ResponseEntity<ErrorResponse> notFound(String code, String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, code, message, traceId()));
    }

    private String traceId() {
        return UUID.randomUUID().toString();
    }
}
