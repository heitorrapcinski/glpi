import React from 'react';
import { Link, useLocation } from 'react-router-dom';

// ---------------------------------------------------------------------------
// Route label map — maps path segments to human-readable labels
// ---------------------------------------------------------------------------

const SEGMENT_LABELS: Record<string, string> = {
  dashboard: 'Dashboard',
  helpdesk: 'Helpdesk',
  tickets: 'Tickets',
  problems: 'Problems',
  changes: 'Changes',
  assets: 'Assets',
  software: 'Software',
  licenses: 'Licenses',
  knowledge: 'Knowledge Base',
  preferences: 'Preferences',
  new: 'New',
};

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const navStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.25rem',
  fontSize: '0.875rem',
  color: 'var(--tblr-secondary)',
  minWidth: 0,
  overflow: 'hidden',
};

const linkStyle: React.CSSProperties = {
  color: 'var(--tblr-link-color)',
  textDecoration: 'none',
  whiteSpace: 'nowrap',
};

const separatorStyle: React.CSSProperties = {
  color: 'var(--tblr-secondary)',
  userSelect: 'none',
};

const currentStyle: React.CSSProperties = {
  color: 'var(--tblr-secondary)',
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function Breadcrumbs() {
  const location = useLocation();

  const segments = location.pathname
    .split('/')
    .filter(Boolean);

  if (segments.length === 0) return null;

  const crumbs = segments.map((segment, index) => {
    const path = '/' + segments.slice(0, index + 1).join('/');
    const label = SEGMENT_LABELS[segment] ?? decodeURIComponent(segment);
    const isLast = index === segments.length - 1;

    return { path, label, isLast };
  });

  return (
    <nav aria-label="Breadcrumb" style={navStyle}>
      <Link to="/" style={linkStyle} aria-label="Home">
        Home
      </Link>
      {crumbs.map((crumb) => (
        <React.Fragment key={crumb.path}>
          <span style={separatorStyle} aria-hidden="true">/</span>
          {crumb.isLast ? (
            <span style={currentStyle} aria-current="page">
              {crumb.label}
            </span>
          ) : (
            <Link to={crumb.path} style={linkStyle}>
              {crumb.label}
            </Link>
          )}
        </React.Fragment>
      ))}
    </nav>
  );
}
