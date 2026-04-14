import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { usePreferencesStore } from '../../stores/preferencesStore';

// ---------------------------------------------------------------------------
// Menu structure — matches legacy GLPI sidebar sections
// ---------------------------------------------------------------------------

interface MenuItem {
  label: string;
  icon: string;
  path: string;
}

interface MenuSection {
  label: string;
  icon: string;
  items: MenuItem[];
  comingSoon?: boolean;
}

const MENU_SECTIONS: MenuSection[] = [
  {
    label: 'Assets',
    icon: '💻',
    items: [
      { label: 'Computers', icon: '🖥️', path: '/assets?type=Computer' },
      { label: 'Monitors', icon: '🖵', path: '/assets?type=Monitor' },
      { label: 'Software', icon: '📦', path: '/assets/software' },
      { label: 'Network Equipment', icon: '🌐', path: '/assets?type=NetworkEquipment' },
      { label: 'Printers', icon: '🖨️', path: '/assets?type=Printer' },
      { label: 'Phones', icon: '📱', path: '/assets?type=Phone' },
      { label: 'Peripherals', icon: '🔌', path: '/assets?type=Peripheral' },
    ],
  },
  {
    label: 'Assistance',
    icon: '🎫',
    items: [
      { label: 'Tickets', icon: '🎫', path: '/tickets' },
      { label: 'Problems', icon: '⚠️', path: '/problems' },
      { label: 'Changes', icon: '🔄', path: '/changes' },
    ],
  },
  {
    label: 'Management',
    icon: '📋',
    items: [
      { label: 'Licenses', icon: '📄', path: '/assets/licenses' },
      { label: 'Contacts', icon: '👤', path: '#' },
      { label: 'Suppliers', icon: '🏢', path: '#' },
      { label: 'Contracts', icon: '📝', path: '#' },
      { label: 'Documents', icon: '📁', path: '#' },
      { label: 'Lines', icon: '📞', path: '#' },
      { label: 'Certificates', icon: '🔒', path: '#' },
      { label: 'Data centers', icon: '🏗️', path: '#' },
    ],
  },
  {
    label: 'Tools',
    icon: '🔧',
    items: [
      { label: 'Knowledge Base', icon: '📖', path: '/knowledge' },
      { label: 'Notes', icon: '📝', path: '#' },
      { label: 'RSS Feeds', icon: '📡', path: '#' },
    ],
  },
  {
    label: 'Administration',
    icon: '⚙️',
    items: [],
    comingSoon: true,
  },
];

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const EXPANDED_WIDTH = '15rem';
const COLLAPSED_WIDTH = '70px';

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export { EXPANDED_WIDTH, COLLAPSED_WIDTH };

export default function Sidebar() {
  const collapsed = usePreferencesStore((s) => s.sidebarCollapsed);
  const toggleSidebar = usePreferencesStore((s) => s.toggleSidebar);
  const location = useLocation();
  const [expandedSection, setExpandedSection] = useState<string | null>(null);

  const sidebarWidth = collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;

  const sidebarStyle: React.CSSProperties = {
    position: 'fixed',
    top: 0,
    left: 0,
    bottom: 0,
    width: sidebarWidth,
    background: 'var(--glpi-mainmenu-bg)',
    color: 'var(--glpi-mainmenu-fg)',
    display: 'flex',
    flexDirection: 'column',
    zIndex: 1040,
    transition: 'width 0.2s ease',
    overflowX: 'hidden',
    overflowY: 'auto',
  };

  const logoAreaStyle: React.CSSProperties = {
    padding: collapsed ? '1rem 0' : '1rem',
    display: 'flex',
    alignItems: 'center',
    justifyContent: collapsed ? 'center' : 'flex-start',
    gap: '0.5rem',
    borderBottom: '1px solid rgba(255,255,255,0.1)',
    flexShrink: 0,
  };

  const logoTextStyle: React.CSSProperties = {
    fontSize: '1.25rem',
    fontWeight: 700,
    letterSpacing: '0.05em',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
  };

  const menuStyle: React.CSSProperties = {
    flex: 1,
    padding: '0.5rem 0',
    overflowY: 'auto',
  };

  const toggleAreaStyle: React.CSSProperties = {
    padding: '0.75rem',
    borderTop: '1px solid rgba(255,255,255,0.1)',
    flexShrink: 0,
  };

  const toggleBtnStyle: React.CSSProperties = {
    width: '100%',
    padding: '0.5rem',
    border: 'none',
    background: 'rgba(255,255,255,0.08)',
    color: 'var(--glpi-mainmenu-fg)',
    borderRadius: '0.375rem',
    cursor: 'pointer',
    fontSize: '0.75rem',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.375rem',
  };

  const handleSectionClick = (label: string, comingSoon?: boolean) => {
    if (comingSoon) return;
    setExpandedSection((prev) => (prev === label ? null : label));
  };

  const isActive = (path: string) => {
    if (path === '#') return false;
    const cleanPath = path.split('?')[0];
    return location.pathname === cleanPath || location.pathname.startsWith(cleanPath + '/');
  };

  return (
    <aside style={sidebarStyle} aria-label="Main navigation">
      {/* Logo */}
      <div style={logoAreaStyle}>
        <Link
          to="/dashboard"
          style={{ textDecoration: 'none', color: 'inherit', display: 'flex', alignItems: 'center', gap: '0.5rem' }}
          aria-label="GLPI Home"
        >
          <span style={{ fontSize: '1.5rem' }} aria-hidden="true">🔧</span>
          {!collapsed && <span style={logoTextStyle}>GLPI</span>}
        </Link>
      </div>

      {/* Menu sections */}
      <nav style={menuStyle}>
        {MENU_SECTIONS.map((section) => (
          <SidebarSection
            key={section.label}
            section={section}
            collapsed={collapsed}
            expanded={expandedSection === section.label}
            onToggle={() => handleSectionClick(section.label, section.comingSoon)}
            isActive={isActive}
          />
        ))}
      </nav>

      {/* Collapse toggle */}
      <div style={toggleAreaStyle}>
        <button
          type="button"
          style={toggleBtnStyle}
          onClick={toggleSidebar}
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          title={collapsed ? 'Expand menu' : 'Collapse menu'}
        >
          <span aria-hidden="true">{collapsed ? '»' : '«'}</span>
          {!collapsed && <span>Collapse menu</span>}
        </button>
      </div>
    </aside>
  );
}

// ---------------------------------------------------------------------------
// SidebarSection sub-component
// ---------------------------------------------------------------------------

interface SidebarSectionProps {
  section: MenuSection;
  collapsed: boolean;
  expanded: boolean;
  onToggle: () => void;
  isActive: (path: string) => boolean;
}

function SidebarSection({ section, collapsed, expanded, onToggle, isActive }: SidebarSectionProps) {
  const sectionBtnStyle: React.CSSProperties = {
    width: '100%',
    padding: collapsed ? '0.75rem 0' : '0.75rem 1rem',
    border: 'none',
    background: 'transparent',
    color: section.comingSoon ? 'var(--glpi-mainmenu-fg-muted)' : 'var(--glpi-mainmenu-fg)',
    cursor: section.comingSoon ? 'default' : 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: collapsed ? 'center' : 'flex-start',
    gap: '0.625rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    textAlign: 'left',
    opacity: section.comingSoon ? 0.5 : 1,
  };

  const chevronStyle: React.CSSProperties = {
    marginLeft: 'auto',
    fontSize: '0.625rem',
    transition: 'transform 0.2s',
    transform: expanded ? 'rotate(90deg)' : 'rotate(0deg)',
  };

  const subMenuStyle: React.CSSProperties = {
    overflow: 'hidden',
    maxHeight: expanded ? `${section.items.length * 2.5}rem` : '0',
    transition: 'max-height 0.2s ease',
  };

  return (
    <div>
      <button
        type="button"
        style={sectionBtnStyle}
        onClick={onToggle}
        aria-expanded={expanded}
        aria-label={section.comingSoon ? `${section.label} — coming soon` : section.label}
        title={collapsed ? section.label : undefined}
      >
        <span aria-hidden="true" style={{ fontSize: '1.125rem', flexShrink: 0 }}>{section.icon}</span>
        {!collapsed && (
          <>
            <span>{section.label}</span>
            {section.comingSoon ? (
              <span style={{
                marginLeft: 'auto',
                fontSize: '0.625rem',
                background: 'rgba(255,255,255,0.15)',
                padding: '0.125rem 0.375rem',
                borderRadius: '0.25rem',
              }}>
                Soon
              </span>
            ) : (
              <span style={chevronStyle} aria-hidden="true">▸</span>
            )}
          </>
        )}
      </button>

      {/* Sub-items (only when expanded and not collapsed) */}
      {!collapsed && !section.comingSoon && (
        <div style={subMenuStyle}>
          {section.items.map((item) => {
            const active = isActive(item.path);
            const itemStyle: React.CSSProperties = {
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.5rem 1rem 0.5rem 2.75rem',
              fontSize: '0.8125rem',
              color: active ? 'var(--glpi-mainmenu-fg)' : 'var(--glpi-mainmenu-fg-muted)',
              textDecoration: 'none',
              background: active ? 'var(--glpi-mainmenu-active-bg)' : 'transparent',
              borderRadius: 0,
            };

            return (
              <Link
                key={item.label}
                to={item.path}
                style={itemStyle}
                aria-current={active ? 'page' : undefined}
              >
                <span aria-hidden="true" style={{ fontSize: '0.875rem' }}>{item.icon}</span>
                <span>{item.label}</span>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
