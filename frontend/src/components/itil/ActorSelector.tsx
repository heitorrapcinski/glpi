import { useState, useCallback, useRef, useEffect } from 'react';
import type { CSSProperties } from 'react';

// ---------------------------------------------------------------------------
// ActorSelector — Reusable user/group/supplier search and selection
// ---------------------------------------------------------------------------

export interface ActorOption {
  id: string;
  kind: 'user' | 'group' | 'supplier';
  displayName: string;
}

export interface ActorSelectorProps {
  /** Currently selected actor id. */
  value: string;
  /** Called when the user selects an actor. */
  onChange: (actorId: string) => void;
  /** Placeholder text for the search input. */
  placeholder?: string;
  /** Accessible label. */
  ariaLabel?: string;
  /** Static list of options (for MVP; can be replaced with async search). */
  options?: ActorOption[];
  /** Async search callback — if provided, options prop is ignored. */
  onSearch?: (query: string) => Promise<ActorOption[]>;
}

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const wrapper: CSSProperties = {
  position: 'relative',
  width: '100%',
};

const inputStyle: CSSProperties = {
  width: '100%',
  padding: '0.45rem 0.65rem',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '4px',
  fontSize: '0.875rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  outline: 'none',
  boxSizing: 'border-box',
};

const dropdownStyle: CSSProperties = {
  position: 'absolute',
  top: '100%',
  left: 0,
  right: 0,
  maxHeight: '200px',
  overflowY: 'auto',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderTop: 'none',
  borderRadius: '0 0 4px 4px',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  zIndex: 10,
  boxShadow: '0 4px 8px rgba(0,0,0,0.08)',
};

const optionStyle: CSSProperties = {
  padding: '0.4rem 0.65rem',
  fontSize: '0.8125rem',
  cursor: 'pointer',
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
};

const optionHover: CSSProperties = {
  ...optionStyle,
  backgroundColor: 'var(--tblr-bg-surface-secondary, #fafbfc)',
};

const kindBadge: CSSProperties = {
  fontSize: '0.6875rem',
  padding: '0.1rem 0.35rem',
  borderRadius: '3px',
  backgroundColor: 'var(--tblr-bg-surface-secondary, #fafbfc)',
  color: 'var(--tblr-secondary, #606f91)',
  textTransform: 'capitalize',
  flexShrink: 0,
};

const noResults: CSSProperties = {
  padding: '0.5rem 0.65rem',
  fontSize: '0.8125rem',
  color: 'var(--tblr-secondary, #606f91)',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function ActorSelector({
  value,
  onChange,
  placeholder = 'Search users, groups, or suppliers…',
  ariaLabel = 'Actor selector',
  options = [],
  onSearch,
}: ActorSelectorProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<ActorOption[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [hoveredIndex, setHoveredIndex] = useState(-1);
  const wrapperRef = useRef<HTMLDivElement>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout>>();

  // Derive display name for current value
  const selectedOption = [...options, ...results].find((o) => o.id === value);

  // Filter / search logic
  const performSearch = useCallback(
    async (q: string) => {
      if (onSearch) {
        const res = await onSearch(q);
        setResults(res);
      } else {
        const lower = q.toLowerCase();
        setResults(
          options.filter(
            (o) =>
              o.displayName.toLowerCase().includes(lower) ||
              o.kind.toLowerCase().includes(lower),
          ),
        );
      }
    },
    [onSearch, options],
  );

  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const q = e.target.value;
      setQuery(q);
      setIsOpen(true);
      setHoveredIndex(-1);

      if (debounceRef.current) clearTimeout(debounceRef.current);
      debounceRef.current = setTimeout(() => performSearch(q), 200);
    },
    [performSearch],
  );

  const handleSelect = useCallback(
    (option: ActorOption) => {
      onChange(option.id);
      setQuery(option.displayName);
      setIsOpen(false);
    },
    [onChange],
  );

  const handleFocus = useCallback(() => {
    setIsOpen(true);
    performSearch(query);
  }, [performSearch, query]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (!isOpen) return;
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setHoveredIndex((prev) => Math.min(prev + 1, results.length - 1));
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        setHoveredIndex((prev) => Math.max(prev - 1, 0));
      } else if (e.key === 'Enter' && hoveredIndex >= 0) {
        e.preventDefault();
        handleSelect(results[hoveredIndex]);
      } else if (e.key === 'Escape') {
        setIsOpen(false);
      }
    },
    [isOpen, results, hoveredIndex, handleSelect],
  );

  // Close on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return (
    <div ref={wrapperRef} style={wrapper}>
      <input
        type="text"
        value={query || (selectedOption?.displayName ?? '')}
        onChange={handleInputChange}
        onFocus={handleFocus}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        aria-label={ariaLabel}
        aria-expanded={isOpen}
        aria-autocomplete="list"
        role="combobox"
        style={inputStyle}
      />

      {isOpen && (
        <div style={dropdownStyle} role="listbox" aria-label="Search results">
          {results.length === 0 ? (
            <div style={noResults}>No results found</div>
          ) : (
            results.map((option, idx) => (
              <div
                key={option.id}
                role="option"
                aria-selected={option.id === value}
                style={idx === hoveredIndex ? optionHover : optionStyle}
                onMouseEnter={() => setHoveredIndex(idx)}
                onMouseDown={(e) => {
                  e.preventDefault();
                  handleSelect(option);
                }}
              >
                <span style={kindBadge}>{option.kind}</span>
                <span>{option.displayName}</span>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
