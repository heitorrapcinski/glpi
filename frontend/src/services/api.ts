import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

// ---------------------------------------------------------------------------
// Error classification
// ---------------------------------------------------------------------------

export type ErrorCategory =
  | 'AUTH_INVALID'
  | 'AUTH_EXPIRED'
  | 'RATE_LIMITED'
  | 'SERVER_ERROR'
  | 'SERVICE_UNAVAILABLE'
  | 'CLIENT_ERROR';

/**
 * Classify an Axios error into a well-known category so callers can display
 * the right feedback without inspecting raw status codes.
 */
export function classifyError(error: AxiosError): ErrorCategory {
  // Network errors (no response at all) → service unavailable
  if (!error.response) {
    return 'SERVICE_UNAVAILABLE';
  }

  const status = error.response.status;
  const url = error.config?.url ?? '';

  if (status === 401) {
    // On the login endpoint the user simply provided bad credentials
    return url.includes('/auth/') ? 'AUTH_INVALID' : 'AUTH_EXPIRED';
  }

  if (status === 429) return 'RATE_LIMITED';
  if (status === 503) return 'SERVICE_UNAVAILABLE';
  if (status >= 500 && status <= 599) return 'SERVER_ERROR';
  // Any other 4xx
  return 'CLIENT_ERROR';
}

// ---------------------------------------------------------------------------
// External hooks – set by AuthContext at runtime
// ---------------------------------------------------------------------------

let _getAccessToken: (() => string | null) | null = null;
let _refreshTokens: (() => Promise<string | null>) | null = null;
let _clearSession: (() => void) | null = null;

export function setAccessTokenGetter(fn: () => string | null): void {
  _getAccessToken = fn;
}

export function setTokenRefresher(fn: () => Promise<string | null>): void {
  _refreshTokens = fn;
}

export function setSessionClearer(fn: () => void): void {
  _clearSession = fn;
}

// ---------------------------------------------------------------------------
// Axios instance
// ---------------------------------------------------------------------------

const api = axios.create({
  baseURL: import.meta.env.VITE_API_GATEWAY_URL || '/api',
});

// ---------------------------------------------------------------------------
// Request interceptor – attach Bearer token
// ---------------------------------------------------------------------------

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = _getAccessToken?.();
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

// ---------------------------------------------------------------------------
// Response interceptor – 401 retry with refresh
// ---------------------------------------------------------------------------

/** Flag to prevent concurrent refresh attempts */
let isRefreshing = false;

/**
 * Queue of requests that arrived while a refresh was already in-flight.
 * They will be resolved/rejected once the refresh completes.
 */
let pendingQueue: {
  resolve: (token: string | null) => void;
  reject: (err: unknown) => void;
}[] = [];

function processPendingQueue(token: string | null, error?: unknown): void {
  pendingQueue.forEach((p) => {
    if (error) {
      p.reject(error);
    } else {
      p.resolve(token);
    }
  });
  pendingQueue = [];
}

api.interceptors.response.use(
  // Success – pass through
  (response) => response,

  // Error handler
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retried?: boolean;
    };

    // Only handle 401s
    if (error.response?.status !== 401) {
      return Promise.reject(error);
    }

    // Never retry auth endpoints – reject immediately
    const url = originalRequest?.url ?? '';
    if (url.includes('/auth/')) {
      return Promise.reject(error);
    }

    // Prevent infinite retry loops
    if (originalRequest._retried) {
      return Promise.reject(error);
    }

    // If no refresh function is wired, we can't recover
    if (!_refreshTokens) {
      return Promise.reject(error);
    }

    // If a refresh is already in progress, queue this request
    if (isRefreshing) {
      return new Promise<string | null>((resolve, reject) => {
        pendingQueue.push({ resolve, reject });
      }).then((newToken) => {
        if (newToken) {
          originalRequest.headers.set('Authorization', `Bearer ${newToken}`);
        }
        originalRequest._retried = true;
        return api(originalRequest);
      });
    }

    isRefreshing = true;
    originalRequest._retried = true;

    try {
      const newToken = await _refreshTokens();

      if (newToken) {
        originalRequest.headers.set('Authorization', `Bearer ${newToken}`);
      }

      processPendingQueue(newToken);
      return api(originalRequest);
    } catch (refreshError) {
      processPendingQueue(null, refreshError);

      // Refresh failed – clear session and redirect to sign-in
      _clearSession?.();
      window.location.href = '/signin';

      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

export default api;
