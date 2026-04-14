import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from 'react';
import type { CSSProperties, ReactNode } from 'react';

// ---------------------------------------------------------------------------
// Toast notification system
// ---------------------------------------------------------------------------

export type ToastVariant = 'success' | 'error' | 'warning' | 'info';

export interface ToastMessage {
  id: string;
  variant: ToastVariant;
  message: string;
}

export interface ToastContextValue {
  addToast: (variant: ToastVariant, message: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

/** Hook to show toast notifications from any component. */
export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within a ToastProvider');
  return ctx;
}

// ---------------------------------------------------------------------------
// Variant styles
// ---------------------------------------------------------------------------

const VARIANT_STYLES: Record<ToastVariant, { bg: string; fg: string; icon: string }> = {
  success: { bg: '#dcfce7', fg: '#166534', icon: '✓' },
  error:   { bg: '#fee2e2', fg: '#991b1b', icon: '✕' },
  warning: { bg: '#fef3c7', fg: '#92400e', icon: '⚠' },
  info:    { bg: '#dbeafe', fg: '#1e40af', icon: 'ℹ' },
};

const AUTO_DISMISS_MS = 5000;

let nextId = 0;

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const addToast = useCallback((variant: ToastVariant, message: string) => {
    const id = String(++nextId);
    setToasts((prev) => [...prev, { id, variant, message }]);
  }, []);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ addToast }}>
      {children}
      <ToastContainer toasts={toasts} onDismiss={removeToast} />
    </ToastContext.Provider>
  );
}

// ---------------------------------------------------------------------------
// Container (top-right, ARIA live region)
// ---------------------------------------------------------------------------

const containerStyle: CSSProperties = {
  position: 'fixed',
  top: '1rem',
  right: '1rem',
  zIndex: 9999,
  display: 'flex',
  flexDirection: 'column',
  gap: '0.5rem',
  maxWidth: '24rem',
  pointerEvents: 'none',
};

function ToastContainer({
  toasts,
  onDismiss,
}: {
  toasts: ToastMessage[];
  onDismiss: (id: string) => void;
}) {
  return (
    <div
      aria-live="polite"
      aria-atomic="false"
      role="region"
      aria-label="Notifications"
      style={containerStyle}
    >
      {toasts.map((t) => (
        <ToastItem key={t.id} toast={t} onDismiss={onDismiss} />
      ))}
    </div>
  );
}

// ---------------------------------------------------------------------------
// Single toast item
// ---------------------------------------------------------------------------

const itemBase: CSSProperties = {
  display: 'flex',
  alignItems: 'flex-start',
  gap: '0.5rem',
  padding: '0.75rem 1rem',
  borderRadius: '6px',
  boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
  fontSize: '0.875rem',
  lineHeight: 1.4,
  pointerEvents: 'auto',
  animation: 'toast-slide-in 0.25s ease-out',
};

const dismissBtn: CSSProperties = {
  background: 'none',
  border: 'none',
  cursor: 'pointer',
  fontSize: '1rem',
  lineHeight: 1,
  padding: '0 0 0 0.5rem',
  marginInlineStart: 'auto',
  opacity: 0.6,
  minWidth: '44px',
  minHeight: '44px',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
};

function ToastItem({
  toast,
  onDismiss,
}: {
  toast: ToastMessage;
  onDismiss: (id: string) => void;
}) {
  const timerRef = useRef<ReturnType<typeof setTimeout>>();
  const vs = VARIANT_STYLES[toast.variant];

  useEffect(() => {
    timerRef.current = setTimeout(() => onDismiss(toast.id), AUTO_DISMISS_MS);
    return () => clearTimeout(timerRef.current);
  }, [toast.id, onDismiss]);

  return (
    <>
      <style>{`@keyframes toast-slide-in{from{opacity:0;transform:translateX(1rem)}to{opacity:1;transform:translateX(0)}}`}</style>
      <div
        role="alert"
        style={{ ...itemBase, backgroundColor: vs.bg, color: vs.fg }}
      >
        <span aria-hidden="true" style={{ fontWeight: 700 }}>{vs.icon}</span>
        <span>{toast.message}</span>
        <button
          type="button"
          style={{ ...dismissBtn, color: vs.fg }}
          onClick={() => onDismiss(toast.id)}
          aria-label="Dismiss notification"
        >
          ×
        </button>
      </div>
    </>
  );
}
