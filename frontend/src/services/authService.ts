import api from './api';
import type { LoginRequest, AuthResponse } from '../types/auth';

/**
 * Send login credentials to the API Gateway.
 * POST /auth/login
 */
export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/login', request);
  return data;
}

/**
 * Refresh the access token using a valid refresh token.
 * POST /auth/refresh
 */
export async function refresh(refreshToken: string): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/refresh', {
    refreshToken,
  });
  return data;
}

/**
 * Invalidate the current session on the server.
 * POST /auth/logout
 *
 * The Bearer token is already attached by the Axios request interceptor,
 * but the token is also accepted as a parameter for explicit use.
 */
export async function logout(accessToken: string): Promise<void> {
  await api.post('/auth/logout', null, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
}
