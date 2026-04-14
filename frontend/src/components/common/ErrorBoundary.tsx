import { Component } from 'react';
import type { ErrorInfo, ReactNode, CSSProperties } from 'react';

// ---------------------------------------------------------------------------
// ErrorBoundary — Catch-all error UI with retry button
// ---------------------------------------------------------------------------

export interface ErrorBoundaryProps {
  children: ReactNode;
  /** Optional custom fallback renderer. */
  fallback?: (error: Error, reset: () => void) => ReactNode;
}

interface State {
  error: Error | null;
}

const container: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  minHeight: '50vh',
  padding: '2rem',
  textAlign: 'center',
  gap: '1rem',
};

const heading: CSSProperties = {
  fontSize: '1.25rem',
  fontWeight: 600,
  color: 'var(--tblr-dark, #101923)',
  margin: 0,
};

const message: CSSProperties = {
  fontSize: '0.875rem',
  color: 'var(--tblr-secondary, #606f91)',
  margin: 0,
  maxWidth: '32rem',
};

const button: CSSProperties = {
  padding: '0.5rem 1.25rem',
  fontSize: '0.875rem',
  fontWeight: 600,
  borderRadius: '6px',
  border: 'none',
  cursor: 'pointer',
  backgroundColor: 'var(--tblr-primary, rgb(254,201,92))',
  color: 'var(--tblr-primary-fg, #1e293b)',
  minWidth: '44px',
  minHeight: '44px',
};

export default class ErrorBoundary extends Component<ErrorBoundaryProps, State> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // eslint-disable-next-line no-console
    console.error('[ErrorBoundary]', error, info.componentStack);
  }

  private reset = () => {
    this.setState({ error: null });
  };

  render() {
    const { error } = this.state;
    if (!error) return this.props.children;

    if (this.props.fallback) {
      return this.props.fallback(error, this.reset);
    }

    return (
      <div role="alert" style={container}>
        <h2 style={heading}>Something went wrong</h2>
        <p style={message}>
          An unexpected error occurred. You can try again or contact support if
          the problem persists.
        </p>
        <button type="button" style={button} onClick={this.reset}>
          Try again
        </button>
      </div>
    );
  }
}
