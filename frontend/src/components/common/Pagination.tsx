import type { CSSProperties } from 'react';

// ---------------------------------------------------------------------------
// Pagination — Page controls with configurable page size
// ---------------------------------------------------------------------------

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  onPageChange: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  pageSizeOptions?: number[];
}

const PAGE_SIZE_DEFAULTS = [15, 25, 50, 100];

const nav: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  flexWrap: 'wrap',
  gap: '0.75rem',
  padding: '0.5rem 0',
  fontSize: '0.8125rem',
};

const btnGroup: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.25rem',
};

const pageBtn: CSSProperties = {
  minWidth: '44px',
  minHeight: '44px',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '0.25rem 0.5rem',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '4px',
  background: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  cursor: 'pointer',
  fontSize: '0.8125rem',
  fontWeight: 500,
};

const activeBtnExtra: CSSProperties = {
  backgroundColor: 'var(--tblr-primary, rgb(254,201,92))',
  color: 'var(--tblr-primary-fg, #1e293b)',
  borderColor: 'var(--tblr-primary-darken, #e5b44e)',
  fontWeight: 700,
};

const disabledExtra: CSSProperties = {
  opacity: 0.4,
  cursor: 'default',
  pointerEvents: 'none',
};

const selectStyle: CSSProperties = {
  padding: '0.25rem 0.4rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  fontSize: '0.8125rem',
  minHeight: '44px',
};

/** Build a compact page range with ellipsis. */
function getPageRange(current: number, total: number): (number | '...')[] {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);

  const pages: (number | '...')[] = [1];

  const start = Math.max(2, current - 1);
  const end = Math.min(total - 1, current + 1);

  if (start > 2) pages.push('...');
  for (let i = start; i <= end; i++) pages.push(i);
  if (end < total - 1) pages.push('...');

  pages.push(total);
  return pages;
}

export default function Pagination({
  currentPage,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
  onPageSizeChange,
  pageSizeOptions = PAGE_SIZE_DEFAULTS,
}: PaginationProps) {
  const pages = getPageRange(currentPage, totalPages);

  return (
    <nav aria-label="Pagination" style={nav}>
      {/* Info */}
      <span style={{ color: 'var(--tblr-secondary, #606f91)' }}>
        {totalElements === 0
          ? 'No results'
          : `${(currentPage - 1) * pageSize + 1}–${Math.min(
              currentPage * pageSize,
              totalElements,
            )} of ${totalElements}`}
      </span>

      {/* Page buttons */}
      <div style={btnGroup}>
        <button
          type="button"
          style={{ ...pageBtn, ...(currentPage <= 1 ? disabledExtra : {}) }}
          disabled={currentPage <= 1}
          onClick={() => onPageChange(currentPage - 1)}
          aria-label="Previous page"
        >
          ‹
        </button>

        {pages.map((p, i) =>
          p === '...' ? (
            <span key={`e${i}`} style={{ padding: '0 0.25rem' }}>
              …
            </span>
          ) : (
            <button
              key={p}
              type="button"
              style={{
                ...pageBtn,
                ...(p === currentPage ? activeBtnExtra : {}),
              }}
              aria-current={p === currentPage ? 'page' : undefined}
              onClick={() => onPageChange(p)}
            >
              {p}
            </button>
          ),
        )}

        <button
          type="button"
          style={{
            ...pageBtn,
            ...(currentPage >= totalPages ? disabledExtra : {}),
          }}
          disabled={currentPage >= totalPages}
          onClick={() => onPageChange(currentPage + 1)}
          aria-label="Next page"
        >
          ›
        </button>
      </div>

      {/* Page size selector */}
      {onPageSizeChange && (
        <label style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
          <span>Rows</span>
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange(Number(e.target.value))}
            style={selectStyle}
            aria-label="Rows per page"
          >
            {pageSizeOptions.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </label>
      )}
    </nav>
  );
}
