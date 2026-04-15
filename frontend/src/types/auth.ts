/** Request payload for POST /auth/login */
export interface LoginRequest {
  username: string;
  password: string;
  totpCode?: number;
}

/** Response from POST /auth/login and POST /auth/refresh */
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number; // seconds until access token expiry
}

/** Refresh request payload for POST /auth/refresh */
export interface RefreshRequest {
  refreshToken: string;
}

/** Error response from API Gateway */
export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
}
