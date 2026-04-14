import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import type { ApiError } from '../api/types';
import type { AxiosError } from 'axios';

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const formGroupStyle: React.CSSProperties = {
  marginBottom: '1rem',
};

const labelStyle: React.CSSProperties = {
  display: 'block',
  marginBottom: '0.375rem',
  fontSize: '0.875rem',
  fontWeight: 500,
  color: 'var(--tblr-body-color, #1e293b)',
};

const inputStyle: React.CSSProperties = {
  display: 'block',
  width: '100%',
  padding: '0.5rem 0.75rem',
  fontSize: '0.875rem',
  lineHeight: 1.5,
  color: 'var(--tblr-body-color, #1e293b)',
  background: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #d0d7de)',
  borderRadius: '0.375rem',
  boxSizing: 'border-box',
  outline: 'none',
  transition: 'border-color 0.15s ease',
};

const inputErrorStyle: React.CSSProperties = {
  ...inputStyle,
  borderColor: '#ef4444',
};

const checkboxRowStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  marginBottom: '1.25rem',
};

const checkboxLabelStyle: React.CSSProperties = {
  fontSize: '0.875rem',
  color: 'var(--tblr-body-color, #1e293b)',
  cursor: 'pointer',
  userSelect: 'none',
};

const buttonStyle: React.CSSProperties = {
  display: 'block',
  width: '100%',
  padding: '0.625rem 1rem',
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: 'var(--tblr-primary-fg, #1e293b)',
  background: 'var(--tblr-primary, rgb(254, 201, 92))',
  border: 'none',
  borderRadius: '0.375rem',
  cursor: 'pointer',
  transition: 'opacity 0.15s ease',
};

const buttonDisabledStyle: React.CSSProperties = {
  ...buttonStyle,
  opacity: 0.65,
  cursor: 'not-allowed',
};

const alertStyle: React.CSSProperties = {
  padding: '0.75rem 1rem',
  marginBottom: '1rem',
  fontSize: '0.875rem',
  color: '#991b1b',
  background: '#fef2f2',
  border: '1px solid #fecaca',
  borderRadius: '0.375rem',
};

const dividerStyle: React.CSSProperties = {
  borderTop: '1px solid var(--tblr-border-color, #d0d7de)',
  margin: '1.25rem 0',
};

const sectionTitleStyle: React.CSSProperties = {
  fontSize: '0.8125rem',
  fontWeight: 600,
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
  color: 'var(--tblr-muted, #6b7280)',
  marginBottom: '0.75rem',
};

// ---------------------------------------------------------------------------
// Error extraction helpers
// ---------------------------------------------------------------------------

function extractApiError(error: unknown): { status: number; code: string; message: string } | null {
  const axiosErr = error as AxiosError<{ error?: ApiError; code?: string; message?: string }>;
  if (!axiosErr.response) return null;

  const status = axiosErr.response.status;
  const body = axiosErr.response.data;

  // Try nested error object first
  if (body?.error) {
    return { status, code: body.error.code ?? '', message: body.error.message ?? '' };
  }

  // Flat body
  return { status, code: body?.code ?? '', message: body?.message ?? '' };
}

// ---------------------------------------------------------------------------
// LoginPage
// ---------------------------------------------------------------------------

export default function LoginPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const returnTo = searchParams.get('returnTo');

  const login = useAuthStore((s) => s.login);
  const loginWith2FA = useAuthStore((s) => s.loginWith2FA);
  const isLoading = useAuthStore((s) => s.isLoading);

  // Login form state
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);

  // 2FA state
  const [show2FA, setShow2FA] = useState(false);
  const [totpCode, setTotpCode] = useState('');

  // Error state
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // ---------------------------------------------------------------------------
  // Post-login redirect
  // ---------------------------------------------------------------------------

  function redirectAfterLogin(profileInterface: 'central' | 'helpdesk') {
    if (returnTo) {
      navigate(decodeURIComponent(returnTo), { replace: true });
      return;
    }
    if (profileInterface === 'helpdesk') {
      navigate('/helpdesk', { replace: true });
    } else {
      navigate('/dashboard', { replace: true });
    }
  }

  // ---------------------------------------------------------------------------
  // Handle login form submission
  // ---------------------------------------------------------------------------

  async function handleLoginSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErrorMessage(null);

    try {
      await login(username, password, rememberMe);
      // After login, user is set in the store — read it directly
      const profileInterface = useAuthStore.getState().user?.profileInterface ?? 'central';
      redirectAfterLogin(profileInterface);
    } catch (error: unknown) {
      const apiError = extractApiError(error);

      if (!apiError) {
        setErrorMessage('An unexpected error occurred. Please try again.');
        return;
      }

      if (apiError.status === 403 && apiError.code === '2FA_REQUIRED') {
        // Transition to 2FA step
        setShow2FA(true);
        setErrorMessage(null);
        return;
      }

      if (apiError.status === 401) {
        if (apiError.code === 'account_locked') {
          setErrorMessage('Your account is temporarily locked.');
        } else {
          setErrorMessage('Invalid username or password.');
        }
        return;
      }

      setErrorMessage(apiError.message || 'An unexpected error occurred. Please try again.');
    }
  }

  // ---------------------------------------------------------------------------
  // Handle 2FA form submission
  // ---------------------------------------------------------------------------

  async function handle2FASubmit(e: React.FormEvent) {
    e.preventDefault();
    setErrorMessage(null);

    try {
      await loginWith2FA(totpCode);
      const profileInterface = useAuthStore.getState().user?.profileInterface ?? 'central';
      redirectAfterLogin(profileInterface);
    } catch (error: unknown) {
      const apiError = extractApiError(error);

      if (!apiError) {
        setErrorMessage('An unexpected error occurred. Please try again.');
        return;
      }

      if (apiError.status === 401) {
        setErrorMessage('Invalid verification code. Please try again.');
      } else {
        setErrorMessage(apiError.message || 'An unexpected error occurred. Please try again.');
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Render — 2FA step
  // ---------------------------------------------------------------------------

  if (show2FA) {
    return (
      <form onSubmit={handle2FASubmit} noValidate aria-label="Two-factor authentication">
        <p style={sectionTitleStyle}>Two-Factor Authentication</p>
        <p style={{ fontSize: '0.875rem', color: 'var(--tblr-muted, #6b7280)', marginBottom: '1rem' }}>
          Enter the 6-digit code from your authenticator app.
        </p>

        {errorMessage && (
          <div role="alert" style={alertStyle}>
            {errorMessage}
          </div>
        )}

        <div style={formGroupStyle}>
          <label htmlFor="totp-code" style={labelStyle}>
            Verification code
          </label>
          <input
            id="totp-code"
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            pattern="[0-9]{6}"
            maxLength={6}
            placeholder="000000"
            value={totpCode}
            onChange={(e) => setTotpCode(e.target.value.replace(/\D/g, ''))}
            style={errorMessage ? inputErrorStyle : inputStyle}
            autoFocus
            required
            aria-describedby={errorMessage ? 'totp-error' : undefined}
          />
        </div>

        <button
          type="submit"
          style={isLoading ? buttonDisabledStyle : buttonStyle}
          disabled={isLoading || totpCode.length !== 6}
          aria-busy={isLoading}
        >
          {isLoading ? 'Verifying…' : 'Verify'}
        </button>

        <div style={dividerStyle} />

        <button
          type="button"
          onClick={() => { setShow2FA(false); setErrorMessage(null); setTotpCode(''); }}
          style={{
            background: 'none',
            border: 'none',
            color: 'var(--tblr-link-color, #3a5693)',
            cursor: 'pointer',
            fontSize: '0.875rem',
            padding: 0,
          }}
        >
          ← Back to login
        </button>
      </form>
    );
  }

  // ---------------------------------------------------------------------------
  // Render — login step
  // ---------------------------------------------------------------------------

  return (
    <form onSubmit={handleLoginSubmit} noValidate aria-label="Sign in">
      {errorMessage && (
        <div role="alert" style={alertStyle}>
          {errorMessage}
        </div>
      )}

      <div style={formGroupStyle}>
        <label htmlFor="login-username" style={labelStyle}>
          Username
        </label>
        <input
          id="login-username"
          type="text"
          autoComplete="username"
          placeholder="Enter your username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          style={inputStyle}
          autoFocus
          required
          aria-required="true"
        />
      </div>

      <div style={formGroupStyle}>
        <label htmlFor="login-password" style={labelStyle}>
          Password
        </label>
        <input
          id="login-password"
          type="password"
          autoComplete="current-password"
          placeholder="Enter your password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          style={inputStyle}
          required
          aria-required="true"
        />
      </div>

      <div style={checkboxRowStyle}>
        <input
          id="remember-me"
          type="checkbox"
          checked={rememberMe}
          onChange={(e) => setRememberMe(e.target.checked)}
          style={{ width: '1rem', height: '1rem', cursor: 'pointer' }}
        />
        <label htmlFor="remember-me" style={checkboxLabelStyle}>
          Remember me
        </label>
      </div>

      <button
        type="submit"
        style={isLoading ? buttonDisabledStyle : buttonStyle}
        disabled={isLoading}
        aria-busy={isLoading}
      >
        {isLoading ? 'Signing in…' : 'Sign in'}
      </button>
    </form>
  );
}
