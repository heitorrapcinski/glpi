import { type CSSProperties, useCallback } from 'react';
import { usePreferencesStore } from '@/stores/preferencesStore';
import { applyTheme, SUPPORTED_PALETTES, DARK_PALETTES } from '@/theme/themeEngine';

// ---------------------------------------------------------------------------
// PreferencesPage — User preferences for language, theme, layout, etc.
// Requirements: 16.1, 16.2, 16.3, 16.4, 16.5
// ---------------------------------------------------------------------------

/** Available display languages (MVP: en-US only). */
const LANGUAGES = [
  { value: 'en-US', label: 'English (US)' },
  { value: 'fr-FR', label: 'Français' },
  { value: 'pt-BR', label: 'Português (Brasil)' },
  { value: 'es-ES', label: 'Español' },
  { value: 'de-DE', label: 'Deutsch' },
] as const;

const ITEMS_PER_PAGE_OPTIONS = [15, 25, 50, 100, 200] as const;

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const page: CSSProperties = {
  padding: '1.5rem',
  maxWidth: '860px',
  margin: '0 auto',
};

const heading: CSSProperties = {
  fontSize: '1.25rem',
  fontWeight: 700,
  color: 'var(--tblr-body-color, #1e293b)',
  margin: '0 0 1.5rem 0',
};

const card: CSSProperties = {
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  borderRadius: '8px',
  boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
  padding: '1.25rem 1.5rem',
  marginBottom: '1.25rem',
};

const sectionTitle: CSSProperties = {
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: 'var(--tblr-body-color, #1e293b)',
  margin: '0 0 1rem 0',
};

const fieldRow: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0.625rem 0',
  borderBottom: '1px solid #e9ecef',
  gap: '1rem',
  flexWrap: 'wrap',
};

const fieldLabel: CSSProperties = {
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: 'var(--tblr-body-color, #1e293b)',
  margin: 0,
  minWidth: '140px',
};

const fieldDescription: CSSProperties = {
  fontSize: '0.75rem',
  color: 'var(--tblr-secondary, #606f91)',
  margin: '0.125rem 0 0 0',
};

const selectStyle: CSSProperties = {
  padding: '0.375rem 0.75rem',
  fontSize: '0.8125rem',
  borderRadius: '6px',
  border: '1px solid #d1d5db',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  minWidth: '180px',
  cursor: 'pointer',
};

const paletteGrid: CSSProperties = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))',
  gap: '0.625rem',
  padding: '0.5rem 0',
};

const paletteBtn = (isActive: boolean): CSSProperties => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: '0.375rem',
  padding: '0.625rem 0.5rem',
  borderRadius: '8px',
  border: isActive ? '2px solid rgb(var(--tblr-primary-rgb, 254, 201, 92))' : '2px solid transparent',
  backgroundColor: isActive ? 'rgba(var(--tblr-primary-rgb, 254, 201, 92), 0.08)' : 'var(--tblr-bg-surface, #fff)',
  cursor: 'pointer',
  transition: 'border-color 0.15s, background-color 0.15s',
  fontSize: '0.75rem',
  fontWeight: isActive ? 600 : 400,
  color: 'var(--tblr-body-color, #1e293b)',
  textTransform: 'capitalize',
});

const paletteSwatch: CSSProperties = {
  width: '100%',
  height: '28px',
  borderRadius: '4px',
  border: '1px solid rgba(0,0,0,0.1)',
};

const toggleRow: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
};

const toggleSwitch = (active: boolean): CSSProperties => ({
  position: 'relative',
  width: '40px',
  height: '22px',
  borderRadius: '11px',
  backgroundColor: active ? 'rgb(var(--tblr-primary-rgb, 254, 201, 92))' : '#d1d5db',
  cursor: 'pointer',
  transition: 'background-color 0.2s',
  border: 'none',
  padding: 0,
  flexShrink: 0,
});

const toggleKnob = (active: boolean): CSSProperties => ({
  position: 'absolute',
  top: '2px',
  left: active ? '20px' : '2px',
  width: '18px',
  height: '18px',
  borderRadius: '50%',
  backgroundColor: '#fff',
  boxShadow: '0 1px 2px rgba(0,0,0,0.2)',
  transition: 'left 0.2s',
});

// ---------------------------------------------------------------------------
// Palette preview colors (representative sidebar bg for each palette)
// ---------------------------------------------------------------------------

const PALETTE_PREVIEW: Record<string, string> = {
  default: '#2f3f64',
  classic: '#98a458',
  dark: '#161514',
  darker: '#0d0d0d',
  midnight: '#000000',
  auror: '#1b3a4b',
  auror_dark: '#0f2027',
  teclib: '#a958b9',
  aerialgreen: '#2e7d32',
  automn: '#8d6e63',
  clockworkorange: '#e65100',
  flood: '#0277bd',
  greenflat: '#388e3c',
  hipster: '#5d4037',
  icecream: '#ec407a',
  lightblue: '#0288d1',
  premiumred: '#c62828',
  purplehaze: '#6a1b9a',
  vintage: '#795548',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function PreferencesPage() {
  const {
    theme,
    locale,
    layoutMode,
    timelineOrder,
    itemsPerPage,
    sidebarCollapsed,
    setTheme,
    setLocale,
    setLayoutMode,
    setTimelineOrder,
    setItemsPerPage,
    toggleSidebar,
  } = usePreferencesStore();

  const handleThemeChange = useCallback(
    (paletteName: string) => {
      setTheme(paletteName);
      applyTheme(paletteName);
    },
    [setTheme],
  );

  return (
    <div style={page}>
      <h1 style={heading}>Preferences</h1>

      {/* Display Language */}
      <section style={card} aria-labelledby="pref-lang">
        <h2 id="pref-lang" style={sectionTitle}>Display</h2>

        <div style={fieldRow}>
          <div>
            <p style={fieldLabel}>Language</p>
            <p style={fieldDescription}>Interface display language</p>
          </div>
          <select
            style={selectStyle}
            value={locale}
            onChange={(e) => setLocale(e.target.value)}
            aria-label="Display language"
          >
            {LANGUAGES.map((lang) => (
              <option key={lang.value} value={lang.value}>
                {lang.label}
              </option>
            ))}
          </select>
        </div>

        <div style={fieldRow}>
          <div>
            <p style={fieldLabel}>Items per page</p>
            <p style={fieldDescription}>Number of items shown in list views</p>
          </div>
          <select
            style={selectStyle}
            value={itemsPerPage}
            onChange={(e) => setItemsPerPage(Number(e.target.value))}
            aria-label="Items per page"
          >
            {ITEMS_PER_PAGE_OPTIONS.map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
        </div>
      </section>

      {/* Theme / Palette */}
      <section style={card} aria-labelledby="pref-theme">
        <h2 id="pref-theme" style={sectionTitle}>Theme / Palette</h2>
        <p style={fieldDescription}>
          Select a color palette. Changes apply immediately.
        </p>

        <div style={paletteGrid} role="radiogroup" aria-label="Theme palette selector">
          {SUPPORTED_PALETTES.map((name) => {
            const active = name === theme;
            const isDark = DARK_PALETTES.includes(name);
            return (
              <button
                key={name}
                type="button"
                role="radio"
                aria-checked={active}
                aria-label={`${name} palette${isDark ? ' (dark)' : ''}`}
                style={paletteBtn(active)}
                onClick={() => handleThemeChange(name)}
              >
                <span
                  style={{
                    ...paletteSwatch,
                    backgroundColor: PALETTE_PREVIEW[name] ?? '#2f3f64',
                  }}
                />
                {name.replace(/_/g, ' ')}
                {isDark && (
                  <span style={{ fontSize: '0.625rem', color: 'var(--tblr-secondary, #606f91)' }}>
                    dark
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </section>

      {/* Layout */}
      <section style={card} aria-labelledby="pref-layout">
        <h2 id="pref-layout" style={sectionTitle}>Layout</h2>

        <div style={fieldRow}>
          <div>
            <p style={fieldLabel}>Layout mode</p>
            <p style={fieldDescription}>Vertical sidebar or horizontal top bar</p>
          </div>
          <select
            style={selectStyle}
            value={layoutMode}
            onChange={(e) => setLayoutMode(e.target.value as 'vertical' | 'horizontal')}
            aria-label="Layout mode"
          >
            <option value="vertical">Vertical sidebar</option>
            <option value="horizontal">Horizontal top bar</option>
          </select>
        </div>

        <div style={fieldRow}>
          <div>
            <p style={fieldLabel}>Timeline order</p>
            <p style={fieldDescription}>Order of entries on ticket/problem/change timelines</p>
          </div>
          <select
            style={selectStyle}
            value={timelineOrder}
            onChange={(e) => setTimelineOrder(e.target.value as 'newest' | 'oldest')}
            aria-label="Timeline order"
          >
            <option value="newest">Newest first</option>
            <option value="oldest">Oldest first</option>
          </select>
        </div>

        <div style={{ ...fieldRow, borderBottom: 'none' }}>
          <div>
            <p style={fieldLabel}>Sidebar collapsed</p>
            <p style={fieldDescription}>Show sidebar in collapsed (icons only) mode</p>
          </div>
          <div style={toggleRow}>
            <button
              type="button"
              role="switch"
              aria-checked={sidebarCollapsed}
              aria-label="Toggle sidebar collapsed state"
              style={toggleSwitch(sidebarCollapsed)}
              onClick={toggleSidebar}
            >
              <span style={toggleKnob(sidebarCollapsed)} />
            </button>
            <span style={{ fontSize: '0.75rem', color: 'var(--tblr-secondary, #606f91)' }}>
              {sidebarCollapsed ? 'Collapsed' : 'Expanded'}
            </span>
          </div>
        </div>
      </section>
    </div>
  );
}
