import type { CSSProperties } from 'react';

// ---------------------------------------------------------------------------
// LoadingSkeleton — Suspense fallback for lazy-loaded routes
// ---------------------------------------------------------------------------

const shimmer: CSSProperties = {
  display: 'block',
  borderRadius: '4px',
  background:
    'linear-gradient(90deg, #e9ecef 25%, #f1f3f5 50%, #e9ecef 75%)',
  backgroundSize: '200% 100%',
  animation: 'shimmer 1.5s ease-in-out infinite',
};

const container: CSSProperties = {
  padding: '1.5rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
};

/**
 * Lightweight skeleton placeholder displayed while a lazy-loaded page chunk
 * is being fetched.  Renders a few pulsing bars that approximate a typical
 * page layout (header + content rows).
 */
export default function LoadingSkeleton() {
  return (
    <>
      {/* Inline keyframes — avoids requiring a separate CSS file */}
      <style>{`@keyframes shimmer{0%{background-position:200% 0}100%{background-position:-200% 0}}`}</style>

      <div style={container} role="status" aria-label="Loading page">
        {/* Title bar */}
        <span style={{ ...shimmer, width: '40%', height: '1.5rem' }} />

        {/* Content rows */}
        <span style={{ ...shimmer, width: '100%', height: '1rem' }} />
        <span style={{ ...shimmer, width: '90%', height: '1rem' }} />
        <span style={{ ...shimmer, width: '75%', height: '1rem' }} />

        {/* Card placeholder */}
        <span style={{ ...shimmer, width: '100%', height: '8rem', marginTop: '0.5rem' }} />
      </div>
    </>
  );
}
