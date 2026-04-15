import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import type { ReactNode } from "react";
import * as authService from "../services/authService";
import {
  setAccessTokenGetter,
  setTokenRefresher,
  setSessionClearer,
} from "../services/api";

// ---------------------------------------------------------------------------
// localStorage keys
// ---------------------------------------------------------------------------

const LS_ACCESS_TOKEN = "glpi_access_token";
const LS_REFRESH_TOKEN = "glpi_refresh_token";
const LS_EXPIRES_AT = "glpi_expires_at";

// ---------------------------------------------------------------------------
// Interfaces
// ---------------------------------------------------------------------------

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  expiresAt: number | null; // Unix timestamp (ms)
  isAuthenticated: boolean;
  isLoading: boolean; // True during initial session restore
}

interface AuthContextType extends AuthState {
  login: (
    username: string,
    password: string,
    totpCode?: number,
  ) => Promise<void>;
  logout: () => Promise<void>;
  getAccessToken: () => string | null;
}

// ---------------------------------------------------------------------------
// Context
// ---------------------------------------------------------------------------

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Persist tokens to localStorage. */
function persistTokens(
  accessToken: string,
  refreshToken: string,
  expiresAt: number,
): void {
  localStorage.setItem(LS_ACCESS_TOKEN, accessToken);
  localStorage.setItem(LS_REFRESH_TOKEN, refreshToken);
  localStorage.setItem(LS_EXPIRES_AT, String(expiresAt));
}

/** Remove all token entries from localStorage. */
function clearPersistedTokens(): void {
  localStorage.removeItem(LS_ACCESS_TOKEN);
  localStorage.removeItem(LS_REFRESH_TOKEN);
  localStorage.removeItem(LS_EXPIRES_AT);
}

/** Read tokens from localStorage. Returns null values when missing. */
function readPersistedTokens(): {
  accessToken: string | null;
  refreshToken: string | null;
  expiresAt: number | null;
} {
  const accessToken = localStorage.getItem(LS_ACCESS_TOKEN);
  const refreshToken = localStorage.getItem(LS_REFRESH_TOKEN);
  const raw = localStorage.getItem(LS_EXPIRES_AT);
  const expiresAt = raw ? Number(raw) : null;
  return { accessToken, refreshToken, expiresAt };
}

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    accessToken: null,
    refreshToken: null,
    expiresAt: null,
    isAuthenticated: false,
    isLoading: true, // starts true until session restore completes
  });

  // Ref keeps the latest state accessible inside timers / callbacks without
  // needing them in the dependency array.
  const stateRef = useRef(state);
  stateRef.current = state;

  // Timer id for the automatic refresh scheduler (Task 4.2).
  const refreshTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // -------------------------------------------------------------------------
  // Token refresh timer (Task 4.2)
  // -------------------------------------------------------------------------

  /** Clear any pending refresh timer. */
  const clearRefreshTimer = useCallback(() => {
    if (refreshTimerRef.current !== null) {
      clearTimeout(refreshTimerRef.current);
      refreshTimerRef.current = null;
    }
  }, []);

  /**
   * Schedule a token refresh that fires 60 seconds before `expiresAt`.
   * If the remaining time is already ≤ 60 s the refresh fires immediately.
   */
  const scheduleRefresh = useCallback(
    (expiresAt: number) => {
      clearRefreshTimer();

      const msUntilExpiry = expiresAt - Date.now();
      // Fire 60 s before expiry, but never negative.
      const delay = Math.max(msUntilExpiry - 60_000, 0);

      refreshTimerRef.current = setTimeout(async () => {
        const currentRefreshToken = stateRef.current.refreshToken;
        if (!currentRefreshToken) return;

        try {
          const response = await authService.refresh(currentRefreshToken);
          const newExpiresAt = Date.now() + response.expiresIn * 1000;

          // Update memory state
          setState((prev) => ({
            ...prev,
            accessToken: response.accessToken,
            refreshToken: response.refreshToken,
            expiresAt: newExpiresAt,
            isAuthenticated: true,
          }));

          // Update localStorage
          persistTokens(
            response.accessToken,
            response.refreshToken,
            newExpiresAt,
          );

          // Reschedule for the next cycle
          scheduleRefresh(newExpiresAt);
        } catch {
          // On refresh failure (401), clear everything
          clearRefreshTimer();
          clearPersistedTokens();
          setState({
            accessToken: null,
            refreshToken: null,
            expiresAt: null,
            isAuthenticated: false,
            isLoading: false,
          });
          // ProtectedRoute will handle the redirect to /signin
        }
      }, delay);
    },
    [clearRefreshTimer],
  );

  // -------------------------------------------------------------------------
  // Session restore on mount (Task 4.1)
  // -------------------------------------------------------------------------

  useEffect(() => {
    const { accessToken, refreshToken, expiresAt } = readPersistedTokens();

    if (accessToken && refreshToken && expiresAt && expiresAt > Date.now()) {
      setState({
        accessToken,
        refreshToken,
        expiresAt,
        isAuthenticated: true,
        isLoading: false,
      });
      scheduleRefresh(expiresAt);
    } else {
      // No valid session — clear any stale data
      clearPersistedTokens();
      setState((prev) => ({ ...prev, isLoading: false }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Clean up timer on unmount (Task 4.2)
  useEffect(() => {
    return () => {
      clearRefreshTimer();
    };
  }, [clearRefreshTimer]);

  // -------------------------------------------------------------------------
  // Wire AuthContext into the Axios HTTP client (Task 4.3)
  // -------------------------------------------------------------------------

  useEffect(() => {
    setAccessTokenGetter(() => stateRef.current.accessToken);

    setTokenRefresher(async () => {
      const currentRefreshToken = stateRef.current.refreshToken;
      if (!currentRefreshToken) return null;

      try {
        const response = await authService.refresh(currentRefreshToken);
        const newExpiresAt = Date.now() + response.expiresIn * 1000;

        setState((prev) => ({
          ...prev,
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          expiresAt: newExpiresAt,
          isAuthenticated: true,
        }));

        persistTokens(response.accessToken, response.refreshToken, newExpiresAt);

        return response.accessToken;
      } catch {
        clearPersistedTokens();
        setState({
          accessToken: null,
          refreshToken: null,
          expiresAt: null,
          isAuthenticated: false,
          isLoading: false,
        });
        return null;
      }
    });

    setSessionClearer(() => {
      clearRefreshTimer();
      clearPersistedTokens();
      setState({
        accessToken: null,
        refreshToken: null,
        expiresAt: null,
        isAuthenticated: false,
        isLoading: false,
      });
    });
  }, [clearRefreshTimer]);

  // -------------------------------------------------------------------------
  // login (Task 4.1)
  // -------------------------------------------------------------------------

  const login = useCallback(
    async (username: string, password: string, totpCode?: number) => {
      const response = await authService.login({
        username,
        password,
        ...(totpCode !== undefined ? { totpCode } : {}),
      });

      const expiresAt = Date.now() + response.expiresIn * 1000;

      // Store in memory
      setState({
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
        expiresAt,
        isAuthenticated: true,
        isLoading: false,
      });

      // Persist to localStorage
      persistTokens(response.accessToken, response.refreshToken, expiresAt);

      // Schedule automatic refresh
      scheduleRefresh(expiresAt);
    },
    [scheduleRefresh],
  );

  // -------------------------------------------------------------------------
  // logout (Task 4.1)
  // -------------------------------------------------------------------------

  const logout = useCallback(async () => {
    const token = stateRef.current.accessToken;

    // Always clear tokens regardless of API response
    clearRefreshTimer();
    clearPersistedTokens();
    setState({
      accessToken: null,
      refreshToken: null,
      expiresAt: null,
      isAuthenticated: false,
      isLoading: false,
    });

    // Attempt server-side logout (fire-and-forget)
    if (token) {
      try {
        await authService.logout(token);
      } catch {
        // Silently ignore — tokens are already cleared
      }
    }

    // Redirect to sign-in
    window.location.href = "/signin";
  }, [clearRefreshTimer]);

  // -------------------------------------------------------------------------
  // getAccessToken (Task 4.1)
  // -------------------------------------------------------------------------

  const getAccessToken = useCallback((): string | null => {
    return stateRef.current.accessToken;
  }, []);

  // -------------------------------------------------------------------------
  // Context value
  // -------------------------------------------------------------------------

  const value: AuthContextType = {
    ...state,
    login,
    logout,
    getAccessToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// ---------------------------------------------------------------------------
// Hook
// ---------------------------------------------------------------------------

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
