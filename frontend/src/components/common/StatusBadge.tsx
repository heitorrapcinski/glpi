import type { CSSProperties } from 'react';
import { getStatusConfig } from '@/utils/status';

// ---------------------------------------------------------------------------
// StatusBadge — Colored badge for ITIL object status codes
// ---------------------------------------------------------------------------

export interface StatusBadgeProps {
  /** ITIL status code (1–6). */
  code: number;
  /** Optional size variant. */
  size?: 'sm' | 'md';
}

const base: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.35em',
  fontWeight: 600,
  borderRadius: '4px',
  whiteSpace: 'nowrap',
  lineHeight: 1,
};

const sizes: Record<string, CSSProperties> = {
  sm: { fontSize: '0.75rem', padding: '0.2em 0.5em' },
  md: { fontSize: '0.8125rem', padding: '0.25em 0.6em' },
};

export default function StatusBadge({ code, size = 'md' }: StatusBadgeProps) {
  const config = getStatusConfig(code);

  const style: CSSProperties = {
    ...base,
    ...sizes[size],
    backgroundColor: `${config.color}1a`, // 10 % opacity tint
    color: config.color,
    border: `1px solid ${config.color}40`,
  };

  return (
    <span role="status" aria-label={`Status: ${config.label}`} style={style}>
      <span aria-hidden="true" style={{ fontSize: '0.7em' }}>●</span>
      {config.label}
    </span>
  );
}
