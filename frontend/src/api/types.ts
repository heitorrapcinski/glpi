/**
 * Generic API response wrapper with optional pagination metadata.
 */
export interface ApiResponse<T> {
  data: T;
  pagination?: PaginationMeta;
}

/**
 * Pagination metadata returned by paginated API endpoints.
 */
export interface PaginationMeta {
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

/**
 * Structured API error returned by the API Gateway.
 */
export interface ApiError {
  status: number;
  code: string;
  message: string;
  details?: Record<string, string[]>;
}
