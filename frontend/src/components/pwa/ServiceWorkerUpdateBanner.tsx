import { useEffect, useState } from 'react';
import { useRegisterSW } from 'virtual:pwa-register/react';

export default function ServiceWorkerUpdateBanner() {
  const [showBanner, setShowBanner] = useState(false);

  const {
    needRefresh: [needRefresh],
    updateServiceWorker,
  } = useRegisterSW({
    onRegisteredSW(_swUrl, _registration) {
      // SW registered
    },
    onRegisterError(_error) {
      // SW registration error
    },
  });

  useEffect(() => {
    setShowBanner(needRefresh);
  }, [needRefresh]);

  if (!showBanner) return null;

  return (
    <div
      role="alert"
      aria-live="polite"
      style={{
        position: 'fixed',
        bottom: 16,
        left: '50%',
        transform: 'translateX(-50%)',
        zIndex: 9999,
        backgroundColor: '#2f3f64',
        color: '#f4f6fa',
        padding: '12px 24px',
        borderRadius: '8px',
        display: 'flex',
        alignItems: 'center',
        gap: '16px',
        boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
        fontSize: '14px',
      }}
    >
      <span>A new version is available.</span>
      <button
        onClick={() => updateServiceWorker(true)}
        style={{
          backgroundColor: '#fec95c',
          color: '#1e293b',
          border: 'none',
          borderRadius: '4px',
          padding: '6px 16px',
          fontWeight: 600,
          cursor: 'pointer',
          fontSize: '14px',
        }}
      >
        Update
      </button>
      <button
        onClick={() => setShowBanner(false)}
        aria-label="Dismiss update notification"
        style={{
          background: 'none',
          border: 'none',
          color: '#f4f6fa',
          cursor: 'pointer',
          fontSize: '18px',
          padding: '0 4px',
        }}
      >
        ×
      </button>
    </div>
  );
}
