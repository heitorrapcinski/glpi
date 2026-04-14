import type { CSSProperties } from 'react';
import { useNavigate } from 'react-router-dom';

// ---------------------------------------------------------------------------
// CounterWidget — Clickable counter card for dashboard summary metrics
// ---------------------------------------------------------------------------

export interface CounterWidgetProps {
  /** Display label (e.g. "Open Tickets"). */
  label: string;
  /** Numeric count to display. */
  value: number;
  /** Accent color for the left border and value text. */
  color: string;
  /** Route to navigate to when clicked (e.g. "/tickets?status=overdue"). */
  href: string;
  /** Optional icon character or emoji. */
  icon?: string;
}

const card: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '1rem',
  padding: '1.25rem 1.5rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  borderRadius: '8px',
  boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
  cursor: 'pointer',
  transition: 'box-shadow 0.15s ease, transform 0.15s ease',
  borderLeft: '4px solid transparent',
  minWidth: 0,
};

const labelStyle: CSSProperties = {
  fontSize: '0.8125rem',
  color: 'var(--tblr-secondary, #606f91)',
  fontWeight: 500,
  margin: 0,
  lineHeight: 1.3,
};

const valueStyle: CSSProperties = {
  fontSize: '1.75rem',
  fontWeight: 700,
  lineHeight: 1,
  margin: 0,
};

export default function CounterWidget({ label, value, color, href, icon }: CounterWidgetProps) {
  const navigate = useNavigate();

  return (
    <button
      type="button"
      aria-label={`${label}: ${value}. Click to view details.`}
      style={{ ...card, borderLeftColor: color }}
      onClick={() => navigate(href)}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLElement).style.boxShadow = '0 4px 12px rgba(0,0,0,0.12)';
        (e.currentTarget as HTMLElement).style.transform = 'translateY(-1px)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.boxShadow = '0 1px 3px rgba(0,0,0,0.08)';
        (e.currentTarget as HTMLElement).style.transform = 'none';
      }}
    >
      {icon && (
        <span aria-hidden="true" style={{ fontSize: '1.5rem', flexShrink: 0 }}>
          {icon}
        </span>
      )}
      <div style={{ minWidth: 0 }}>
        <p style={labelStyle}>{label}</p>
        <p style={{ ...valueStyle, color }}>{value}</p>
      </div>
    </button>
  );
}
