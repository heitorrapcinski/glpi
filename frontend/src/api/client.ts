import axios, {
  AxiosError,
  AxiosInstance,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from 'axios';
import { AUTH } from './endpoints';
import type { ApiResponse, PaginationMeta } from './types';

// ---------------------------------------------------------------------------
// Lazy auth-store accessor — the store module is created in task 3.2.
// We import lazily to avoid circular dependencies and allow the store to be
// registered after the HTTP client is initialised.
// ---------------------------------------------------------------------------
type AuthStoreApi = {
  getState: () => {
    accessToken: string | null;
    refreshToken: string | null;
    logout: () => Promise<void>;
  };
  setState: (partial: { accessToken?: string | null }) => void;
};

let _authStore: AuthStoreApi | null = null;

/**
 * Register the auth Zustand store so the HTTP client can read / mutate tokens.
 * Called once from the auth store module after it is created.
 */
export function registerAuthStore(store: AuthStoreApi): void {
  _authStore = store;
}

// ---------------------------------------------------------------------------
// Toast helper — thin abstraction so the client doesn't depend on a UI lib.
// Consumers can replace this with their own implementation.
// ---------------------------------------------------------------------------
type ToastFn = (message: string, type: 'error' | 'warning' | 'info') => void;

let _showToast: ToastFn = (msg, type) => {
  // Default: log to console until a UI toast is wired up
  // eslint-disable-next-line no-console
  console[type === 'error' ? 'error' : 'warn'](`[api] ${type}: ${msg}`);
};

/**
 * Register a toast callback so the HTTP client can surface user-facing messages.
 */
export function registerToast(fn: ToastFn): void {
  _showToast = fn;
}

// ---------------------------------------------------------------------------
// Axios instance
// ---------------------------------------------------------------------------
const apiClient: AxiosInstance = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// ---------------------------------------------------------------------------
// Request interceptor — attach JWT Bearer header
// ---------------------------------------------------------------------------
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = _authStore?.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// ---------------------------------------------------------------------------
// 401 refresh queue — serialise concurrent refresh attempts
// ---------------------------------------------------------------------------
let isRefreshing = false;
let refreshSubscribers: Array<(token: string) => void> = [];

function subscribeTokenRefresh(cb: (token: string) => void): void {
  refreshSubscribers.push(cb);
}

function onTokenRefreshed(newToken: string): void {
  refreshSubscribers.forEach((cb) => cb(newToken));
  refreshSubscribers = [];
}

function onRefreshFailed(): void {
  refreshSubscribers.forEach(() => {
    /* subscribers will reject via the catch path */
  });
  refreshSubscribers = [];
}

// ---------------------------------------------------------------------------
// Response interceptor — 401 refresh, 429 rate-limit, network errors
// ---------------------------------------------------------------------------
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
      _retryCount?: number;
    };

    // --- Network error (no response at all) --------------------------------
    if (!error.response) {
      _showToast(
        'Network error — please check your connection and try again.',
        'error',
      );
      return Promise.reject(error);
    }

    const { status } = error.response;

    // --- 401 Unauthorized — attempt token refresh --------------------------
    if (status === 401 && !originalRequest._retry) {
      // Don't try to refresh the refresh endpoint itself
      if (originalRequest.url === AUTH.REFRESH) {
        await _authStore?.getState().logout();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Another refresh is in-flight — queue this request
        return new Promise<AxiosResponse>((resolve, reject) => {
          subscribeTokenRefresh((newToken: string) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            apiClient(originalRequest).then(resolve).catch(reject);
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = _authStore?.getState().refreshToken;
        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        const { data } = await axios.post<{ accessToken: string }>(
          `/api${AUTH.REFRESH}`,
          { refreshToken },
        );

        const newToken = data.accessToken;

        // Update the auth store so subsequent requests use the new token
        _authStore?.setState({ accessToken: newToken });

        // Retry queued requests with the new token
        onTokenRefreshed(newToken);

        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        onRefreshFailed();
        await _authStore?.getState().logout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // --- 429 Rate Limited — retry after Retry-After header -----------------
    if (status === 429) {
      const maxRetries = 2;
      const retryCount = originalRequest._retryCount ?? 0;

      if (retryCount < maxRetries) {
        const retryAfter = parseRetryAfter(error.response.headers['retry-after']);
        _showToast(
          `Rate limited — retrying in ${retryAfter} second${retryAfter !== 1 ? 's' : ''}…`,
          'warning',
        );

        originalRequest._retryCount = retryCount + 1;

        return new Promise<AxiosResponse>((resolve, reject) => {
          setTimeout(() => {
            apiClient(originalRequest).then(resolve).catch(reject);
          }, retryAfter * 1000);
        });
      }

      _showToast('Too many requests — please try again later.', 'error');
    }

    return Promise.reject(error);
  },
);

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Parse the `Retry-After` header value into seconds.
 * Supports both delta-seconds and HTTP-date formats.
 */
function parseRetryAfter(value: string | undefined): number {
  if (!value) return 1;
  const seconds = Number(value);
  if (!Number.isNaN(seconds)) return Math.max(1, Math.ceil(seconds));
  // HTTP-date format
  const date = new Date(value);
  if (!Number.isNaN(date.getTime())) {
    return Math.max(1, Math.ceil((date.getTime() - Date.now()) / 1000));
  }
  return 1;
}

/**
 * Extract pagination metadata from an API response body.
 */
function extractPagination(data: unknown): PaginationMeta | undefined {
  if (
    data &&
    typeof data === 'object' &&
    'totalElements' in data &&
    'totalPages' in data &&
    'currentPage' in data &&
    'pageSize' in data
  ) {
    const d = data as Record<string, unknown>;
    return {
      totalElements: Number(d.totalElements),
      totalPages: Number(d.totalPages),
      currentPage: Number(d.currentPage),
      pageSize: Number(d.pageSize),
    };
  }
  return undefined;
}

// ---------------------------------------------------------------------------
// Public API — typed convenience wrappers
// ---------------------------------------------------------------------------

/**
 * Perform a GET request and return a typed ApiResponse.
 */
async function get<T>(
  url: string,
  params?: Record<string, unknown>,
): Promise<ApiResponse<T>> {
  const response = await apiClient.get<T>(url, { params });
  return {
    data: response.data,
    pagination: extractPagination(response.data),
  };
}

/**
 * Perform a POST request and return a typed ApiResponse.
 */
async function post<T>(
  url: string,
  data?: unknown,
): Promise<ApiResponse<T>> {
  const response = await apiClient.post<T>(url, data);
  return {
    data: response.data,
    pagination: extractPagination(response.data),
  };
}

/**
 * Perform a PATCH request and return a typed ApiResponse.
 */
async function patch<T>(
  url: string,
  data?: unknown,
): Promise<ApiResponse<T>> {
  const response = await apiClient.patch<T>(url, data);
  return {
    data: response.data,
    pagination: extractPagination(response.data),
  };
}

/**
 * Perform a DELETE request.
 */
async function del(url: string): Promise<void> {
  await apiClient.delete(url);
}

// ---------------------------------------------------------------------------
// Exports
// ---------------------------------------------------------------------------

export { apiClient };
export const api = { get, post, patch, delete: del } as const;
export default api;
