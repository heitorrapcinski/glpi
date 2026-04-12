package com.glpi.ticket.adapter.in.rest;

import com.glpi.common.ErrorResponse;
import com.glpi.ticket.domain.model.InsufficientRightsException;
import com.glpi.ticket.domain.model.InvalidStatusTransitionException;
import com.glpi.ticket.domain.model.TicketNotFoundException;
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

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTicketNotFound(TicketNotFoundException ex, WebRequest request) {
        return ErrorResponse.of(404, "TICKET_NOT_FOUND", ex.getMessage(),
                request.getHeader("X-Request-Id"));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleInvalidTransition(InvalidStatusTransitionException ex, WebRequest request) {
        return ErrorResponse.of(422, "INVALID_STATUS_TRANSITION", ex.getMessage(),
                request.getHeader("X-Request-Id"));
    }

    @ExceptionHandler(InsufficientRightsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleInsufficientRights(InsufficientRightsException ex, WebRequest request) {
        return ErrorResponse.of(403, "INSUFFICIENT_RIGHTS", ex.getMessage(),
                request.getHeader("X-Request-Id"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return ErrorResponse.of(400, "BAD_REQUEST", ex.getMessage(),
                request.getHeader("X-Request-Id"));
    }
}
