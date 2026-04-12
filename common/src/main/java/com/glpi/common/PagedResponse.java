package com.glpi.common;

import java.util.List;

/**
 * Generic paginated response wrapper used by all collection endpoints across microservices.
 *
 * <p>Field semantics:
 * <ul>
 *   <li>{@code content}       – The page of items for the current request</li>
 *   <li>{@code totalElements} – Total number of items across all pages</li>
 *   <li>{@code totalPages}    – Total number of pages: {@code ceil(totalElements / pageSize)}</li>
 *   <li>{@code currentPage}   – Zero-based index of the current page</li>
 *   <li>{@code pageSize}      – Maximum number of items per page</li>
 * </ul>
 *
 * <p>Invariant: {@code totalPages == ceil(totalElements / pageSize)} must always hold.
 *
 * <p>Validates: Requirements 19.6, 19.7 — Property 35: Pagination metadata correctness
 *
 * @param <T> the type of items in the page
 */
public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {
    /**
     * Factory method that computes {@code totalPages} automatically from
     * {@code totalElements} and {@code pageSize}, ensuring the invariant holds.
     *
     * @param content       items on the current page
     * @param totalElements total number of items across all pages
     * @param currentPage   zero-based current page index
     * @param pageSize      maximum items per page
     * @param <T>           item type
     * @return a correctly constructed {@code PagedResponse}
     */
    public static <T> PagedResponse<T> of(List<T> content, long totalElements,
                                          int currentPage, int pageSize) {
        int totalPages = (pageSize > 0)
                ? (int) Math.ceil((double) totalElements / pageSize)
                : 0;
        return new PagedResponse<>(content, totalElements, totalPages, currentPage, pageSize);
    }
}
