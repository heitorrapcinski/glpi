import React, { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar, { EXPANDED_WIDTH, COLLAPSED_WIDTH } from '../components/navigation/Sidebar';
import TopBar from '../components/navigation/TopBar';
import { usePreferencesStore } from '../stores/preferencesStore';
import { useUiStore } from '../stores/uiStore';

// ---------------------------------------------------------------------------
// Breakpoint
// ---------------------------------------------------------------------------

const MOBILE_BREAKPOINT = 992;

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function CentralLayout() {
  const collapsed = usePreferencesStore((s) => s.sidebarCollapsed);
  const mobileSidebarOpen = useUiStore((s) => s.mobileSidebarOpen);
  const toggleMobileSidebar = useUiStore((s) => s.toggleMobileSidebar);
  const closeMobileSidebar = useUiStore((s) => s.closeMobileSidebar);

  const [isMobile, setIsMobile] = React.useState(
    typeof window !== 'undefined' ? window.innerWidth < MOBILE_BREAKPOINT : false,
  );

  useEffect(() => {
    const mql = window.matchMedia(`(max-width: ${MOBILE_BREAKPOINT - 1}px)`);
    const handler = (e: MediaQueryListEvent) => {
      setIsMobile(e.matches);
      if (!e.matches) closeMobileSidebar();
    };
    setIsMobile(mql.matches);
    mql.addEventListener('change', handler);
    return () => mql.removeEventListener('change', handler);
  }, [closeMobileSidebar]);

  const sidebarWidth = collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;

  // --- Styles ---------------------------------------------------------------

  const wrapperStyle: React.CSSProperties = {
    minHeight: '100vh',
    background: 'var(--tblr-body-bg, #f5f7fb)',
  };

  const contentWrapperStyle: React.CSSProperties = {
    marginInlineStart: isMobile ? 0 : sidebarWidth,
    transition: 'margin-inline-start 0.2s ease',
    display: 'flex',
    flexDirection: 'column',
    minHeight: '100vh',
  };

  const mainStyle: React.CSSProperties = {
    flex: 1,
    padding: 'var(--glpi-content-margin, 24px)',
  };

  const overlayStyle: React.CSSProperties = {
    position: 'fixed',
    inset: 0,
    background: 'rgba(0,0,0,0.4)',
    zIndex: 1035,
  };

  const hamburgerStyle: React.CSSProperties = {
    position: 'fixed',
    top: '1rem',
    left: '1rem',
    zIndex: 1050,
    width: '44px',
    height: '44px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    border: 'none',
    borderRadius: '0.375rem',
    background: 'var(--glpi-mainmenu-bg, #2f3f64)',
    color: 'var(--glpi-mainmenu-fg, #f4f6fa)',
    fontSize: '1.25rem',
    cursor: 'pointer',
  };

  const mobileSidebarWrapperStyle: React.CSSProperties = {
    position: 'fixed',
    top: 0,
    left: 0,
    bottom: 0,
    zIndex: 1040,
    transform: mobileSidebarOpen ? 'translateX(0)' : 'translateX(-100%)',
    transition: 'transform 0.2s ease',
  };

  // --- Render ---------------------------------------------------------------

  return (
    <div style={wrapperStyle}>
      {/* Desktop sidebar */}
      {!isMobile && <Sidebar />}

      {/* Mobile hamburger + overlay sidebar */}
      {isMobile && (
        <>
          <button
            type="button"
            style={hamburgerStyle}
            onClick={toggleMobileSidebar}
            aria-label="Open navigation menu"
          >
            <span aria-hidden="true">☰</span>
          </button>

          {mobileSidebarOpen && (
            <div
              style={overlayStyle}
              onClick={closeMobileSidebar}
              aria-hidden="true"
            />
          )}

          <div style={mobileSidebarWrapperStyle}>
            <Sidebar />
          </div>
        </>
      )}

      {/* Content area */}
      <div style={contentWrapperStyle}>
        <TopBar />
        <main style={mainStyle}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
