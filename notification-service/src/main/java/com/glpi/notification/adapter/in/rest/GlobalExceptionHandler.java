package com.glpi.notification.adapter.in.rest;

import com.glpi.common.ErrorResponse;
import com.glpi.notification.domain.model.TemplateNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler mapping domain exceptions to HTTP responses.
 * Requirements: 19.3, 19.4, 19.5
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TemplateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTemplateNotFound(TemplateNotFoundException ex, WebRequest request) {
        return ErrorResponse.of(404, "TEMPLATE_NOT_FOUND", ex.getMessage(),
                request.getHeader("X-Request-Id"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return ErrorResponse.of(400, "BAD_REQUEST", ex.getMessage(),
                request.getHeader("X-Request-Id"));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex, WebRequest request) {
        return ErrorResponse.of(500, "INTERNAL_ERROR", "An unexpected error occurred",
                request.getHeader("X-Request-Id"));
    }
}
