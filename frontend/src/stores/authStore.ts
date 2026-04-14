import { create, type StoreApi } from 'zustand';
import { api, registerAuthStore } from '../api/client';
import { AUTH, IDENTITY } from '../api/endpoints';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface UserContext {
  userId: string;
  username: string;
  entityId: string;
  entityName: string;
  profileId: string;
  profileName: string;
  profileInterface: 'central' | 'helpdesk';
  rights: Record<string, number>;
  language: string;
}

export interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserContext | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  rememberMe: boolean;

  login: (username: string, password: string, rememberMe: boolean) => Promise<void>;
  loginWith2FA: (totpCode: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshAccessToken: () => Promise<void>;
  switchProfile: (profileId: string) => Promise<void>;
  switchEntity: (entityId: string) => Promise<void>;
}

// ---------------------------------------------------------------------------
// localStorage key for persisted refresh token
// ---------------------------------------------------------------------------
const REFRESH_TOKEN_KEY = 'glpi_refresh_token';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function persistRefreshToken(token: string | null, rememberMe: boolean): void {
  if (rememberMe && token) {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}

function getPersistedRefreshToken(): string | null {
  try {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  } catch {
    return null;
  }
}

function clearTokenStorage(): void {
  try {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  } catch {
    // Ignore storage errors (e.g. private browsing)
  }
}

// ---------------------------------------------------------------------------
// API response shapes (login / refresh / switch)
// ---------------------------------------------------------------------------

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: UserContext;
}

interface RefreshResponse {
  accessToken: string;
}

// ---------------------------------------------------------------------------
// Store creation
// ---------------------------------------------------------------------------

export const useAuthStore = create<AuthState>()((set, get) => ({
  accessToken: null,
  refreshToken: null,
  user: null,
  isAuthenticated: false,
  isLoading: false,
  rememberMe: false,

  // -------------------------------------------------------------------------
  // login — POST /auth/login
  // -------------------------------------------------------------------------
  async login(username: string, password: string, rememberMe: boolean) {
    set({ isLoading: true });
    try {
      const { data } = await api.post<LoginResponse>(AUTH.LOGIN, {
        username,
        password,
      });

      persistRefreshToken(data.refreshToken, rememberMe);

      set({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        user: data.user,
        isAuthenticated: true,
        isLoading: false,
        rememberMe,
      });
    } catch (error: unknown) {
      set({ isLoading: false });
      throw error;
    }
  },

  // -------------------------------------------------------------------------
  // loginWith2FA — POST /auth/2fa
  // -------------------------------------------------------------------------
  async loginWith2FA(totpCode: string) {
    set({ isLoading: true });
    try {
      const { data } = await api.post<LoginResponse>(AUTH.TWO_FA, {
        totpCode,
      });

      const { rememberMe } = get();
      persistRefreshToken(data.refreshToken, rememberMe);

      set({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        user: data.user,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error: unknown) {
      set({ isLoading: false });
      throw error;
    }
  },

  // -------------------------------------------------------------------------
  // logout — POST /auth/logout then clear state
  // -------------------------------------------------------------------------
  async logout() {
    try {
      const { refreshToken } = get();
      if (refreshToken) {
        await api.post(AUTH.LOGOUT, { refreshToken }).catch(() => {
          // Best-effort — don't block logout on network failure
        });
      }
    } finally {
      clearTokenStorage();
      set({
        accessToken: null,
        refreshToken: null,
        user: null,
        isAuthenticated: false,
        isLoading: false,
        rememberMe: false,
      });
    }
  },

  // -------------------------------------------------------------------------
  // refreshAccessToken — POST /auth/refresh
  // -------------------------------------------------------------------------
  async refreshAccessToken() {
    const { refreshToken, rememberMe } = get();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    try {
      const { data } = await api.post<RefreshResponse>(AUTH.REFRESH, {
        refreshToken,
      });

      set({ accessToken: data.accessToken });
    } catch (error: unknown) {
      // Refresh failed — clear session
      clearTokenStorage();
      set({
        accessToken: null,
        refreshToken: null,
        user: null,
        isAuthenticated: false,
        isLoading: false,
        rememberMe: false,
      });
      throw error;
    }
  },

  // -------------------------------------------------------------------------
  // switchProfile — POST /identity/profiles with new profileId
  // -------------------------------------------------------------------------
  async switchProfile(profileId: string) {
    set({ isLoading: true });
    try {
      const { data } = await api.post<LoginResponse>(
        `${IDENTITY.PROFILES}/${profileId}/switch`,
      );

      const { rememberMe } = get();
      persistRefreshToken(data.refreshToken, rememberMe);

      set({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        user: data.user,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error: unknown) {
      set({ isLoading: false });
      throw error;
    }
  },

  // -------------------------------------------------------------------------
  // switchEntity — POST /identity/entities with new entityId
  // -------------------------------------------------------------------------
  async switchEntity(entityId: string) {
    set({ isLoading: true });
    try {
      const { data } = await api.post<LoginResponse>(
        `${IDENTITY.ENTITIES}/${entityId}/switch`,
      );

      const { rememberMe } = get();
      persistRefreshToken(data.refreshToken, rememberMe);

      set({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        user: data.user,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error: unknown) {
      set({ isLoading: false });
      throw error;
    }
  },
}));

// ---------------------------------------------------------------------------
// Wire up the HTTP client interceptor so it can read tokens from this store
// ---------------------------------------------------------------------------
registerAuthStore(useAuthStore as unknown as StoreApi<AuthState>);

// ---------------------------------------------------------------------------
// Session restoration — check localStorage for a persisted refresh token
// and attempt a silent refresh on app load.
// ---------------------------------------------------------------------------
export async function restoreSession(): Promise<boolean> {
  const persistedToken = getPersistedRefreshToken();
  if (!persistedToken) return false;

  // Seed the store with the persisted refresh token so the refresh call works
  useAuthStore.setState({ refreshToken: persistedToken, rememberMe: true });

  try {
    await useAuthStore.getState().refreshAccessToken();
    return useAuthStore.getState().isAuthenticated;
  } catch {
    clearTokenStorage();
    useAuthStore.setState({
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
      rememberMe: false,
    });
    return false;
  }
}
