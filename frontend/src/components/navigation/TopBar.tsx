import React from 'react';
import Breadcrumbs from './Breadcrumbs';
import GlobalSearch from './GlobalSearch';
import UserMenu from './UserMenu';

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const topBarStyle: React.CSSProperties = {
  height: 'var(--glpi-topbar-height, 79px)',
  background: 'var(--tblr-bg-surface, #fff)',
  borderBottom: '1px solid var(--tblr-border-color, #e6e7e9)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0 1.5rem',
  gap: '1rem',
  position: 'sticky',
  top: 0,
  zIndex: 1030,
};

const leftStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  minWidth: 0,
  flex: '0 1 auto',
};

const rightStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  flexShrink: 0,
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function TopBar() {
  return (
    <header style={topBarStyle} role="banner">
      <div style={leftStyle} className="breadcrumbs-container">
        <Breadcrumbs />
      </div>
      <div style={rightStyle}>
        <GlobalSearch />
        <UserMenu />
      </div>
    </header>
  );
}
