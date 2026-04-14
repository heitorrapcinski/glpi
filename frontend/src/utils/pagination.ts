import type { PaginationMeta } from '@/api/types';

/**
 * Computes pagination metadata from total elements, page size, and requested page.
 *
 * - totalPages = Math.ceil(totalElements / pageSize)
 * - currentPage is clamped to [1..totalPages] (or 1 when totalElements is 0)
 */
export function computePaginationMeta(
  totalElements: number,
  pageSize: number,
  currentPage: number,
): PaginationMeta {
  const totalPages = totalElements === 0 ? 1 : Math.ceil(totalElements / pageSize);
  const clampedPage = Math.max(1, Math.min(currentPage, totalPages));

  return {
    totalElements,
    pageSize,
    totalPages,
    currentPage: clampedPage,
  };
}

/**
 * Result of truncating a single category of search results.
 */
export interface TruncatedCategory<T> {
  items: T[];
  hasMore: boolean;
}

/**
 * Truncates grouped search results so each category contains at most
 * `maxPerCategory` items (default 5).
 *
 * Returns a record where each key maps to `{ items, hasMore }`.
 * `hasMore` is true when the original array length exceeds maxPerCategory.
 */
export function truncateSearchResults<T>(
  groupedResults: Record<string, T[]>,
  maxPerCategory: number = 5,
): Record<string, TruncatedCategory<T>> {
  const result: Record<string, TruncatedCategory<T>> = {};

  for (const [category, items] of Object.entries(groupedResults)) {
    result[category] = {
      items: items.slice(0, maxPerCategory),
      hasMore: items.length > maxPerCategory,
    };
  }

  return result;
}
