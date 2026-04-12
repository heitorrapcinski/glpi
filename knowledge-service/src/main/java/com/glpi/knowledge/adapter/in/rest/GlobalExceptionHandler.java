package com.glpi.knowledge.adapter.in.rest;

import com.glpi.common.ErrorResponse;
import com.glpi.knowledge.domain.model.ArticleNotFoundException;
import com.glpi.knowledge.domain.model.CategoryNotFoundException;
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

    @ExceptionHandler(ArticleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleArticleNotFound(ArticleNotFoundException ex, WebRequest request) {
        return ErrorResponse.of(404, "ARTICLE_NOT_FOUND", ex.getMessage(),
                request.getHeader("X-Request-Id"));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCategoryNotFound(CategoryNotFoundException ex, WebRequest request) {
        return ErrorResponse.of(404, "CATEGORY_NOT_FOUND", ex.getMessage(),
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
