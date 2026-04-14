import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const wrapperStyle: React.CSSProperties = {
  position: 'relative',
};

const triggerStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  padding: '0.375rem 0.75rem',
  border: 'none',
  background: 'transparent',
  cursor: 'pointer',
  borderRadius: '0.375rem',
  color: 'var(--tblr-body-color, #1e293b)',
  fontSize: '0.875rem',
  lineHeight: 1.4,
};

const avatarStyle: React.CSSProperties = {
  width: 32,
  height: 32,
  borderRadius: '50%',
  background: 'var(--tblr-primary)',
  color: 'var(--tblr-primary-fg)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontWeight: 600,
  fontSize: '0.75rem',
  flexShrink: 0,
};

const userInfoStyle: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'flex-end',
  lineHeight: 1.3,
};

const usernameStyle: React.CSSProperties = {
  fontWeight: 600,
  fontSize: '0.8125rem',
};

const entityStyle: React.CSSProperties = {
  fontSize: '0.75rem',
  color: 'var(--tblr-secondary)',
  maxWidth: '10rem',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
};

const dropdownStyle: React.CSSProperties = {
  position: 'absolute',
  top: '100%',
  right: 0,
  marginTop: '0.25rem',
  minWidth: '14rem',
  background: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #e6e7e9)',
  borderRadius: '0.375rem',
  boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
  zIndex: 1050,
  padding: '0.25rem 0',
};

const sectionLabelStyle: React.CSSProperties = {
  padding: '0.5rem 0.75rem 0.25rem',
  fontSize: '0.6875rem',
  fontWeight: 600,
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
  color: 'var(--tblr-secondary)',
};

const menuItemStyle: React.CSSProperties = {
  display: 'block',
  width: '100%',
  padding: '0.5rem 0.75rem',
  border: 'none',
  background: 'transparent',
  textAlign: 'left',
  cursor: 'pointer',
  fontSize: '0.8125rem',
  color: 'var(--tblr-body-color, #1e293b)',
};

const dividerStyle: React.CSSProperties = {
  height: 1,
  background: 'var(--tblr-border-color, #e6e7e9)',
  margin: '0.25rem 0',
};

const activeItemStyle: React.CSSProperties = {
  ...menuItemStyle,
  fontWeight: 600,
  color: 'var(--tblr-primary)',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function UserMenu() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  const initials = user
    ? user.username.slice(0, 2).toUpperCase()
    : '??';

  // Close on outside click
  useEffect(() => {
    if (!open) return;
    function handleClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, [open]);

  // Close on Escape
  useEffect(() => {
    if (!open) return;
    function handleKey(e: KeyboardEvent) {
      if (e.key === 'Escape') setOpen(false);
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [open]);

  const handleLogout = useCallback(async () => {
    setOpen(false);
    await logout();
    navigate('/login');
  }, [logout, navigate]);

  const handlePreferences = useCallback(() => {
    setOpen(false);
    navigate('/preferences');
  }, [navigate]);

  return (
    <div ref={ref} style={wrapperStyle}>
      <button
        type="button"
        style={triggerStyle}
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="true"
        aria-expanded={open}
        aria-label="User menu"
      >
        <div style={userInfoStyle}>
          <span style={usernameStyle}>{user?.username ?? 'User'}</span>
          <span style={entityStyle} title={user?.entityName}>
            {user?.entityName ?? ''}
          </span>
        </div>
        <div style={avatarStyle} aria-hidden="true">
          {initials}
        </div>
      </button>

      {open && (
        <div style={dropdownStyle} role="menu" aria-label="User menu options">
          {/* Profile info */}
          <div style={sectionLabelStyle}>Profile</div>
          <div
            style={activeItemStyle}
            role="menuitem"
          >
            {user?.profileName ?? 'Unknown'}
          </div>

          <div style={dividerStyle} />

          {/* Entity info */}
          <div style={sectionLabelStyle}>Entity</div>
          <div
            style={activeItemStyle}
            role="menuitem"
          >
            {user?.entityName ?? 'Unknown'}
          </div>

          <div style={dividerStyle} />

          {/* Actions */}
          <button
            type="button"
            style={menuItemStyle}
            role="menuitem"
            onClick={handlePreferences}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'var(--glpi-hover-bg)';
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'transparent';
            }}
          >
            Preferences
          </button>
          <button
            type="button"
            style={{
              ...menuItemStyle,
              color: '#ef4444',
            }}
            role="menuitem"
            onClick={handleLogout}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'var(--glpi-hover-bg)';
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'transparent';
            }}
          >
            Logout
          </button>
        </div>
      )}
    </div>
  );
}
