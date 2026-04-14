import React from 'react';
import { Outlet } from 'react-router-dom';

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const pageStyle: React.CSSProperties = {
  minHeight: '100vh',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  background: 'var(--tblr-body-bg, #f5f7fb)',
  padding: '1rem',
};

const cardStyle: React.CSSProperties = {
  width: '100%',
  maxWidth: '24rem',
  background: 'var(--tblr-bg-surface, #fff)',
  borderRadius: '0.5rem',
  boxShadow: '0 1px 3px rgba(0,0,0,0.08), 0 4px 12px rgba(0,0,0,0.04)',
  padding: '2rem',
};

const logoAreaStyle: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: '0.5rem',
  marginBottom: '1.5rem',
};

const logoIconStyle: React.CSSProperties = {
  fontSize: '2.5rem',
};

const logoTextStyle: React.CSSProperties = {
  fontSize: '1.5rem',
  fontWeight: 700,
  letterSpacing: '0.05em',
  color: 'var(--glpi-mainmenu-bg, #2f3f64)',
};

// ---------------------------------------------------------------------------
// Component — matches legacy `page-anonymous` centered login card
// ---------------------------------------------------------------------------

export default function AuthLayout() {
  return (
    <div style={pageStyle} className="page-anonymous">
      <div style={cardStyle}>
        <div style={logoAreaStyle}>
          <span style={logoIconStyle} aria-hidden="true">🔧</span>
          <span style={logoTextStyle}>GLPI</span>
        </div>
        <Outlet />
      </div>
    </div>
  );
}
