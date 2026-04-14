import React from 'react';
import { Link, Outlet } from 'react-router-dom';
import UserMenu from '../components/navigation/UserMenu';

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const wrapperStyle: React.CSSProperties = {
  minHeight: '100vh',
  background: 'var(--tblr-body-bg, #f5f7fb)',
  display: 'flex',
  flexDirection: 'column',
};

const navBarStyle: React.CSSProperties = {
  height: 'var(--glpi-topbar-height, 79px)',
  background: 'var(--tblr-bg-surface, #fff)',
  borderBottom: '1px solid var(--tblr-border-color, #e6e7e9)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0 1.5rem',
  position: 'sticky',
  top: 0,
  zIndex: 1030,
};

const navLeftStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '1.5rem',
};

const logoLinkStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  textDecoration: 'none',
  color: 'var(--glpi-mainmenu-bg, #2f3f64)',
};

const logoTextStyle: React.CSSProperties = {
  fontSize: '1.25rem',
  fontWeight: 700,
  letterSpacing: '0.05em',
};

const navLinksStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '1rem',
};

const navLinkStyle: React.CSSProperties = {
  textDecoration: 'none',
  color: 'var(--tblr-body-color, #1e293b)',
  fontSize: '0.875rem',
  fontWeight: 500,
  padding: '0.375rem 0.75rem',
  borderRadius: '0.375rem',
};

const contentStyle: React.CSSProperties = {
  flex: 1,
  padding: 'var(--glpi-content-margin, 24px)',
};

// ---------------------------------------------------------------------------
// Component — horizontal top nav, no sidebar
// ---------------------------------------------------------------------------

export default function HelpdeskLayout() {
  return (
    <div style={wrapperStyle}>
      <nav style={navBarStyle} aria-label="Helpdesk navigation">
        <div style={navLeftStyle}>
          <Link to="/helpdesk" style={logoLinkStyle} aria-label="GLPI Home">
            <span style={{ fontSize: '1.5rem' }} aria-hidden="true">🔧</span>
            <span style={logoTextStyle}>GLPI</span>
          </Link>
          <div style={navLinksStyle}>
            <Link to="/helpdesk" style={navLinkStyle}>Home</Link>
            <Link to="/tickets/new" style={navLinkStyle}>Create Ticket</Link>
            <Link to="/tickets" style={navLinkStyle}>My Tickets</Link>
            <Link to="/knowledge" style={navLinkStyle}>FAQ</Link>
          </div>
        </div>
        <UserMenu />
      </nav>

      <main style={contentStyle}>
        <Outlet />
      </main>
    </div>
  );
}
