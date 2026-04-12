package com.glpi.identity.adapter.in.rest;

import com.glpi.common.ErrorResponse;
import com.glpi.identity.domain.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

/**
 * Global exception handler mapping domain exceptions to standardized HTTP error responses.
 * Uses {@link ErrorResponse} from the common module.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 409 Conflict ---

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(DuplicateUsernameException ex) {
        return conflict("DUPLICATE_USERNAME", ex.getMessage());
    }

    @ExceptionHandler(DuplicateEntityNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntityName(DuplicateEntityNameException ex) {
        return conflict("DUPLICATE_ENTITY_NAME", ex.getMessage());
    }

    @ExceptionHandler(EntityHasChildrenException.class)
    public ResponseEntity<ErrorResponse> handleEntityHasChildren(EntityHasChildrenException ex) {
        return conflict("ENTITY_HAS_CHILDREN", ex.getMessage());
    }

    @ExceptionHandler(LastProfileProtectedException.class)
    public ResponseEntity<ErrorResponse> handleLastProfileProtected(LastProfileProtectedException ex) {
        return conflict("LAST_PROFILE_PROTECTED", ex.getMessage());
    }

    // --- 422 Unprocessable Entity ---

    @ExceptionHandler(PasswordComplexityException.class)
    public ResponseEntity<ErrorResponse> handlePasswordComplexity(PasswordComplexityException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.withDetails(
                        422, "PASSWORD_COMPLEXITY_VIOLATION",
                        ex.getMessage(),
                        ex.getViolations(),
                        traceId()));
    }

    @ExceptionHandler(PasswordHistoryException.class)
    public ResponseEntity<ErrorResponse> handlePasswordHistory(PasswordHistoryException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(422, "PASSWORD_HISTORY_VIOLATION", ex.getMessage(), traceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.withDetails(422, "VALIDATION_ERROR",
                        "Request validation failed", details, traceId()));
    }

    // --- 404 Not Found ---

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return notFound("USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return notFound("ENTITY_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProfileNotFound(ProfileNotFoundException ex) {
        return notFound("PROFILE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGroupNotFound(GroupNotFoundException ex) {
        return notFound("GROUP_NOT_FOUND", ex.getMessage());
    }

    // --- 401 Unauthorized ---

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountInactive(AccountInactiveException ex) {
        return unauthorized("ACCOUNT_INACTIVE", ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException ex) {
        return unauthorized("ACCOUNT_LOCKED", ex.getMessage());
    }

    @ExceptionHandler(TotpRequiredException.class)
    public ResponseEntity<ErrorResponse> handleTotpRequired(TotpRequiredException ex) {
        return unauthorized("TOTP_REQUIRED", ex.getMessage());
    }

    @ExceptionHandler(TotpInvalidException.class)
    public ResponseEntity<ErrorResponse> handleTotpInvalid(TotpInvalidException ex) {
        return unauthorized("TOTP_INVALID", ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenReuseException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenReuse(RefreshTokenReuseException ex) {
        return unauthorized("REFRESH_TOKEN_REUSE", ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(TokenExpiredException ex) {
        return unauthorized("TOKEN_EXPIRED", ex.getMessage());
    }

    // --- 403 Forbidden ---

    @ExceptionHandler(InsufficientRightsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientRights(InsufficientRightsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, "INSUFFICIENT_RIGHTS", ex.getMessage(), traceId()));
    }

    // --- 500 Internal Server Error ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // Never expose stack trace or internal details
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "INTERNAL_ERROR",
                        "An unexpected error occurred", traceId()));
    }

    // --- Helpers ---

    private ResponseEntity<ErrorResponse> conflict(String code, String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, code, message, traceId()));
    }

    private ResponseEntity<ErrorResponse> notFound(String code, String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, code, message, traceId()));
    }

    private ResponseEntity<ErrorResponse> unauthorized(String code, String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, code, message, traceId()));
    }

    private String traceId() {
        return UUID.randomUUID().toString();
    }
}
