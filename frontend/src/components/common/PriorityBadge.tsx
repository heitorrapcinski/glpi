import type { CSSProperties } from 'react';
import { getPriorityConfig } from '@/utils/priority';

// ---------------------------------------------------------------------------
// PriorityBadge — Colored indicator for ITIL priority codes
// ---------------------------------------------------------------------------

export interface PriorityBadgeProps {
  /** ITIL priority code (1–6). */
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

export default function PriorityBadge({ code, size = 'md' }: PriorityBadgeProps) {
  const config = getPriorityConfig(code);

  const style: CSSProperties = {
    ...base,
    ...sizes[size],
    backgroundColor: `${config.color}1a`,
    color: config.color,
    border: `1px solid ${config.color}40`,
  };

  return (
    <span aria-label={`Priority: ${config.label}`} style={style}>
      <span
        aria-hidden="true"
        style={{
          display: 'inline-block',
          width: '0.6em',
          height: '0.6em',
          borderRadius: '50%',
          backgroundColor: config.color,
        }}
      />
      {config.label}
    </span>
  );
}
