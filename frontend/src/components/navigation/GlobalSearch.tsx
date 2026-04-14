import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUiStore } from '../../stores/uiStore';
import { truncateSearchResults } from '../../utils/pagination';
import { api } from '../../api/client';
import { SEARCH } from '../../api/endpoints';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface SearchResult {
  id: string;
  type: string;
  title: string;
  status?: number;
}

interface SearchApiResponse {
  tickets?: SearchResult[];
  problems?: SearchResult[];
  changes?: SearchResult[];
  assets?: SearchResult[];
  knowledge?: SearchResult[];
}

// Category display config
const CATEGORY_CONFIG: Record<string, { label: string; icon: string; basePath: string }> = {
  tickets: { label: 'Tickets', icon: '🎫', basePath: '/tickets' },
  problems: { label: 'Problems', icon: '⚠️', basePath: '/problems' },
  changes: { label: 'Changes', icon: '🔄', basePath: '/changes' },
  assets: { label: 'Assets', icon: '💻', basePath: '/assets' },
  knowledge: { label: 'Knowledge', icon: '📖', basePath: '/knowledge' },
};

const MAX_PER_CATEGORY = 5;
const DEBOUNCE_MS = 300;

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const wrapperStyle: React.CSSProperties = {
  position: 'relative',
  flex: '0 1 24rem',
};

const inputStyle: React.CSSProperties = {
  width: '100%',
  padding: '0.5rem 0.75rem 0.5rem 2rem',
  border: '1px solid var(--tblr-border-color, #e6e7e9)',
  borderRadius: '0.375rem',
  fontSize: '0.875rem',
  background: 'var(--tblr-bg-surface-secondary, #fafbfc)',
  color: 'var(--tblr-body-color, #1e293b)',
  outline: 'none',
};

const searchIconStyle: React.CSSProperties = {
  position: 'absolute',
  left: '0.625rem',
  top: '50%',
  transform: 'translateY(-50%)',
  fontSize: '0.875rem',
  color: 'var(--tblr-secondary)',
  pointerEvents: 'none',
};

const dropdownStyle: React.CSSProperties = {
  position: 'absolute',
  top: '100%',
  left: 0,
  right: 0,
  marginTop: '0.25rem',
  background: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #e6e7e9)',
  borderRadius: '0.375rem',
  boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
  zIndex: 1060,
  maxHeight: '24rem',
  overflowY: 'auto',
};

const categoryHeaderStyle: React.CSSProperties = {
  padding: '0.5rem 0.75rem 0.25rem',
  fontSize: '0.6875rem',
  fontWeight: 600,
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
  color: 'var(--tblr-secondary)',
  display: 'flex',
  alignItems: 'center',
  gap: '0.375rem',
};

const resultItemStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  padding: '0.5rem 0.75rem',
  cursor: 'pointer',
  fontSize: '0.8125rem',
  color: 'var(--tblr-body-color, #1e293b)',
  border: 'none',
  background: 'transparent',
  width: '100%',
  textAlign: 'left',
};

const resultIdStyle: React.CSSProperties = {
  color: 'var(--tblr-secondary)',
  fontSize: '0.75rem',
  flexShrink: 0,
};

const resultTitleStyle: React.CSSProperties = {
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  flex: 1,
};

const viewAllStyle: React.CSSProperties = {
  display: 'block',
  padding: '0.375rem 0.75rem',
  fontSize: '0.75rem',
  color: 'var(--tblr-link-color)',
  textDecoration: 'none',
  cursor: 'pointer',
  border: 'none',
  background: 'transparent',
  textAlign: 'left',
};

const emptyMessageStyle: React.CSSProperties = {
  padding: '1rem 0.75rem',
  textAlign: 'center',
  color: 'var(--tblr-secondary)',
  fontSize: '0.8125rem',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function GlobalSearch() {
  const navigate = useNavigate();
  const searchOpen = useUiStore((s) => s.searchOpen);
  const setSearchOpen = useUiStore((s) => s.setSearchOpen);

  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Record<string, { items: SearchResult[]; hasMore: boolean }>>({});
  const [loading, setLoading] = useState(false);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const wrapperRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Debounced search
  const performSearch = useCallback(async (q: string) => {
    if (q.trim().length < 2) {
      setResults({});
      setSearchOpen(false);
      return;
    }

    setLoading(true);
    try {
      const { data } = await api.get<SearchApiResponse>(SEARCH.QUERY, {
        params: { q: q.trim() },
      });

      const grouped: Record<string, SearchResult[]> = {};
      for (const key of Object.keys(CATEGORY_CONFIG)) {
        const items = (data as Record<string, SearchResult[] | undefined>)[key];
        if (items && items.length > 0) {
          grouped[key] = items;
        }
      }

      const truncated = truncateSearchResults(grouped, MAX_PER_CATEGORY);
      setResults(truncated);
      setSearchOpen(Object.keys(truncated).length > 0);
    } catch {
      setResults({});
      setSearchOpen(false);
    } finally {
      setLoading(false);
    }
  }, [setSearchOpen]);

  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = e.target.value;
      setQuery(value);

      if (debounceRef.current) clearTimeout(debounceRef.current);
      debounceRef.current = setTimeout(() => {
        performSearch(value);
      }, DEBOUNCE_MS);
    },
    [performSearch],
  );

  // Navigate to result
  const handleSelect = useCallback(
    (category: string, result: SearchResult) => {
      setSearchOpen(false);
      setQuery('');
      const config = CATEGORY_CONFIG[category];
      if (config) {
        navigate(`${config.basePath}/${result.id}`);
      }
    },
    [navigate, setSearchOpen],
  );

  // "View all" navigation
  const handleViewAll = useCallback(
    (category: string) => {
      setSearchOpen(false);
      setQuery('');
      const config = CATEGORY_CONFIG[category];
      if (config) {
        navigate(`${config.basePath}?search=${encodeURIComponent(query)}`);
      }
    },
    [navigate, query, setSearchOpen],
  );

  // Close on outside click
  useEffect(() => {
    if (!searchOpen) return;
    function handleClick(e: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setSearchOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, [searchOpen, setSearchOpen]);

  // Close on Escape
  useEffect(() => {
    if (!searchOpen) return;
    function handleKey(e: KeyboardEvent) {
      if (e.key === 'Escape') {
        setSearchOpen(false);
        inputRef.current?.blur();
      }
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [searchOpen, setSearchOpen]);

  // Cleanup debounce on unmount
  useEffect(() => {
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, []);

  const hasResults = Object.keys(results).length > 0;

  return (
    <div ref={wrapperRef} style={wrapperStyle}>
      <span style={searchIconStyle} aria-hidden="true">🔍</span>
      <input
        ref={inputRef}
        type="search"
        placeholder="Search tickets, assets, knowledge..."
        value={query}
        onChange={handleInputChange}
        onFocus={() => {
          if (hasResults) setSearchOpen(true);
        }}
        style={inputStyle}
        aria-label="Global search"
        aria-expanded={searchOpen}
        aria-haspopup="listbox"
        role="combobox"
        aria-autocomplete="list"
      />

      {searchOpen && (
        <div style={dropdownStyle} role="listbox" aria-label="Search results">
          {loading && (
            <div style={emptyMessageStyle}>Searching...</div>
          )}

          {!loading && !hasResults && query.trim().length >= 2 && (
            <div style={emptyMessageStyle}>No results found</div>
          )}

          {!loading && Object.entries(results).map(([category, { items, hasMore }]) => {
            const config = CATEGORY_CONFIG[category];
            if (!config || items.length === 0) return null;

            return (
              <div key={category}>
                <div style={categoryHeaderStyle}>
                  <span aria-hidden="true">{config.icon}</span>
                  {config.label}
                </div>
                {items.map((item) => (
                  <button
                    key={`${category}-${item.id}`}
                    type="button"
                    style={resultItemStyle}
                    role="option"
                    onClick={() => handleSelect(category, item)}
                    onMouseEnter={(e) => {
                      (e.currentTarget as HTMLElement).style.background = 'var(--glpi-hover-bg)';
                    }}
                    onMouseLeave={(e) => {
                      (e.currentTarget as HTMLElement).style.background = 'transparent';
                    }}
                  >
                    <span style={resultIdStyle}>#{item.id}</span>
                    <span style={resultTitleStyle}>{item.title}</span>
                  </button>
                ))}
                {hasMore && (
                  <button
                    type="button"
                    style={viewAllStyle}
                    onClick={() => handleViewAll(category)}
                  >
                    View all {config.label.toLowerCase()} →
                  </button>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
