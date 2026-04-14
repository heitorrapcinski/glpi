import { useState, useCallback, useMemo, type CSSProperties, type ReactNode } from 'react';
import { useQuery, keepPreviousData } from '@tanstack/react-query';
import api from '../../api/client';
import Pagination from './Pagination';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface ColumnDef<T> {
  key: keyof T;
  label: string;
  sortable?: boolean;
  render?: (value: unknown, item: T) => ReactNode;
  width?: string;
}

export interface FilterDef {
  key: string;
  label: string;
  type: 'select' | 'multiselect' | 'text' | 'date-range';
  options?: { value: string; label: string }[];
}

export interface BulkActionDef {
  key: string;
  label: string;
  onAction: (selectedIds: string[]) => void;
  variant?: 'default' | 'danger';
}

interface PaginatedResponse<T> {
  data: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface SearchEngineProps<T> {
  endpoint: string;
  columns: ColumnDef<T>[];
  defaultSort?: { field: string; order: 'asc' | 'desc' };
  filters?: FilterDef[];
  bulkActions?: BulkActionDef[];
  onRowClick?: (item: T) => void;
  pageSize?: number;
  /** Key extractor — defaults to (item as any).id */
  getRowId?: (item: T) => string;
}

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const wrapper: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  gap: '0.75rem',
};

const filterBar: CSSProperties = {
  display: 'flex',
  flexWrap: 'wrap',
  gap: '0.5rem',
  alignItems: 'flex-end',
};

const filterGroup: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  gap: '0.25rem',
  minWidth: '140px',
};

const filterLabel: CSSProperties = {
  fontSize: '0.75rem',
  fontWeight: 600,
  color: 'var(--tblr-secondary, #606f91)',
};

const filterInput: CSSProperties = {
  padding: '0.35rem 0.5rem',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '4px',
  fontSize: '0.8125rem',
  background: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  minHeight: '36px',
};

const scrollContainer: CSSProperties = {
  overflowX: 'auto',
  WebkitOverflowScrolling: 'touch',
};

const table: CSSProperties = {
  width: '100%',
  borderCollapse: 'collapse',
  fontSize: '0.8125rem',
};

const thStyle: CSSProperties = {
  padding: '0.5rem 0.75rem',
  textAlign: 'left',
  fontWeight: 600,
  borderBottom: '2px solid var(--tblr-border-color, #d9dbde)',
  background: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  whiteSpace: 'nowrap',
  userSelect: 'none',
};

const thSortable: CSSProperties = {
  cursor: 'pointer',
};

const tdStyle: CSSProperties = {
  padding: '0.5rem 0.75rem',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  verticalAlign: 'middle',
};

const rowHover: CSSProperties = {
  cursor: 'pointer',
};

const checkboxCell: CSSProperties = {
  width: '44px',
  minWidth: '44px',
  textAlign: 'center',
};

const bulkBar: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  padding: '0.5rem 0.75rem',
  background: 'var(--tblr-primary, rgb(254,201,92))',
  borderRadius: '4px',
  fontSize: '0.8125rem',
  fontWeight: 500,
};

const bulkBtn: CSSProperties = {
  padding: '0.3rem 0.75rem',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '4px',
  background: 'var(--tblr-bg-surface, #fff)',
  cursor: 'pointer',
  fontSize: '0.8125rem',
  minHeight: '36px',
};

const emptyState: CSSProperties = {
  padding: '2rem',
  textAlign: 'center',
  color: 'var(--tblr-secondary, #606f91)',
};

const loadingOverlay: CSSProperties = {
  padding: '2rem',
  textAlign: 'center',
  color: 'var(--tblr-secondary, #606f91)',
};

const errorState: CSSProperties = {
  padding: '2rem',
  textAlign: 'center',
  color: '#ef4444',
};

// ---------------------------------------------------------------------------
// Sort indicator
// ---------------------------------------------------------------------------

function SortIndicator({ active, order }: { active: boolean; order: 'asc' | 'desc' }) {
  if (!active) return <span style={{ opacity: 0.3, marginInlineStart: '0.25rem' }}>↕</span>;
  return (
    <span style={{ marginInlineStart: '0.25rem' }}>
      {order === 'asc' ? '↑' : '↓'}
    </span>
  );
}

// ---------------------------------------------------------------------------
// Filter controls renderer
// ---------------------------------------------------------------------------

function FilterControls({
  filters,
  values,
  onChange,
}: {
  filters: FilterDef[];
  values: Record<string, string>;
  onChange: (key: string, value: string) => void;
}) {
  return (
    <div style={filterBar} role="search" aria-label="Table filters">
      {filters.map((f) => {
        switch (f.type) {
          case 'select':
            return (
              <div key={f.key} style={filterGroup}>
                <label style={filterLabel} htmlFor={`filter-${f.key}`}>
                  {f.label}
                </label>
                <select
                  id={`filter-${f.key}`}
                  style={filterInput}
                  value={values[f.key] ?? ''}
                  onChange={(e) => onChange(f.key, e.target.value)}
                  aria-label={f.label}
                >
                  <option value="">All</option>
                  {f.options?.map((o) => (
                    <option key={o.value} value={o.value}>
                      {o.label}
                    </option>
                  ))}
                </select>
              </div>
            );

          case 'multiselect':
            return (
              <div key={f.key} style={filterGroup}>
                <label style={filterLabel} htmlFor={`filter-${f.key}`}>
                  {f.label}
                </label>
                <select
                  id={`filter-${f.key}`}
                  style={filterInput}
                  multiple
                  value={values[f.key] ? values[f.key].split(',') : []}
                  onChange={(e) => {
                    const selected = Array.from(e.target.selectedOptions, (o) => o.value);
                    onChange(f.key, selected.join(','));
                  }}
                  aria-label={f.label}
                >
                  {f.options?.map((o) => (
                    <option key={o.value} value={o.value}>
                      {o.label}
                    </option>
                  ))}
                </select>
              </div>
            );

          case 'text':
            return (
              <div key={f.key} style={filterGroup}>
                <label style={filterLabel} htmlFor={`filter-${f.key}`}>
                  {f.label}
                </label>
                <input
                  id={`filter-${f.key}`}
                  type="text"
                  style={filterInput}
                  placeholder={f.label}
                  value={values[f.key] ?? ''}
                  onChange={(e) => onChange(f.key, e.target.value)}
                  aria-label={f.label}
                />
              </div>
            );

          case 'date-range':
            return (
              <div key={f.key} style={{ ...filterGroup, flexDirection: 'row', gap: '0.25rem', alignItems: 'flex-end' }}>
                <div style={filterGroup}>
                  <label style={filterLabel} htmlFor={`filter-${f.key}-from`}>
                    {f.label} from
                  </label>
                  <input
                    id={`filter-${f.key}-from`}
                    type="date"
                    style={filterInput}
                    value={values[`${f.key}_from`] ?? ''}
                    onChange={(e) => onChange(`${f.key}_from`, e.target.value)}
                    aria-label={`${f.label} from`}
                  />
                </div>
                <div style={filterGroup}>
                  <label style={filterLabel} htmlFor={`filter-${f.key}-to`}>
                    {f.label} to
                  </label>
                  <input
                    id={`filter-${f.key}-to`}
                    type="date"
                    style={filterInput}
                    value={values[`${f.key}_to`] ?? ''}
                    onChange={(e) => onChange(`${f.key}_to`, e.target.value)}
                    aria-label={`${f.label} to`}
                  />
                </div>
              </div>
            );

          default:
            return null;
        }
      })}
    </div>
  );
}

// ---------------------------------------------------------------------------
// SearchEngine component
// ---------------------------------------------------------------------------

export default function SearchEngine<T>({
  endpoint,
  columns,
  defaultSort,
  filters,
  bulkActions,
  onRowClick,
  pageSize: defaultPageSize = 50,
  getRowId = (item) => (item as Record<string, unknown>).id as string,
}: SearchEngineProps<T>) {
  // -- Local state ----------------------------------------------------------
  const [page, setPage] = useState(1);
  const [pageSizeState, setPageSizeState] = useState(defaultPageSize);
  const [sort, setSort] = useState<{ field: string; order: 'asc' | 'desc' }>(
    defaultSort ?? { field: '', order: 'asc' },
  );
  const [filterValues, setFilterValues] = useState<Record<string, string>>({});
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());

  // -- Build query params ---------------------------------------------------
  const queryParams = useMemo(() => {
    const params: Record<string, unknown> = {
      page,
      pageSize: pageSizeState,
    };
    if (sort.field) {
      params.sort = sort.field;
      params.order = sort.order;
    }
    // Append non-empty filter values
    for (const [k, v] of Object.entries(filterValues)) {
      if (v) params[k] = v;
    }
    return params;
  }, [page, pageSizeState, sort, filterValues]);

  // -- Data fetching via TanStack Query -------------------------------------
  const { data, isLoading, isError, error } = useQuery<PaginatedResponse<T>>({
    queryKey: ['searchEngine', endpoint, queryParams],
    queryFn: async () => {
      const response = await api.get<PaginatedResponse<T>>(endpoint, queryParams);
      return response.data;
    },
    placeholderData: keepPreviousData,
  });

  const rows = data?.data ?? [];
  const totalElements = data?.totalElements ?? 0;
  const totalPages = data?.totalPages ?? 0;
  const currentPage = data?.currentPage ?? page;

  // -- Handlers -------------------------------------------------------------
  const handleSort = useCallback(
    (field: string) => {
      setSort((prev) => ({
        field,
        order: prev.field === field && prev.order === 'asc' ? 'desc' : 'asc',
      }));
      setPage(1);
    },
    [],
  );

  const handleFilterChange = useCallback((key: string, value: string) => {
    setFilterValues((prev) => ({ ...prev, [key]: value }));
    setPage(1);
    setSelectedIds(new Set());
  }, []);

  const handlePageChange = useCallback((newPage: number) => {
    setPage(newPage);
    setSelectedIds(new Set());
  }, []);

  const handlePageSizeChange = useCallback((newSize: number) => {
    setPageSizeState(newSize);
    setPage(1);
    setSelectedIds(new Set());
  }, []);

  const toggleSelectAll = useCallback(() => {
    if (selectedIds.size === rows.length) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(rows.map(getRowId)));
    }
  }, [rows, selectedIds.size, getRowId]);

  const toggleSelectRow = useCallback(
    (id: string) => {
      setSelectedIds((prev) => {
        const next = new Set(prev);
        if (next.has(id)) next.delete(id);
        else next.add(id);
        return next;
      });
    },
    [],
  );

  const hasBulkActions = bulkActions && bulkActions.length > 0;
  const allSelected = rows.length > 0 && selectedIds.size === rows.length;

  // -- Render ---------------------------------------------------------------
  return (
    <div style={wrapper}>
      {/* Filters */}
      {filters && filters.length > 0 && (
        <FilterControls
          filters={filters}
          values={filterValues}
          onChange={handleFilterChange}
        />
      )}

      {/* Bulk action bar */}
      {hasBulkActions && selectedIds.size > 0 && (
        <div style={bulkBar} role="toolbar" aria-label="Bulk actions">
          <span>{selectedIds.size} selected</span>
          {bulkActions.map((action) => (
            <button
              key={action.key}
              type="button"
              style={{
                ...bulkBtn,
                ...(action.variant === 'danger'
                  ? { color: '#ef4444', borderColor: '#ef4444' }
                  : {}),
              }}
              onClick={() => action.onAction(Array.from(selectedIds))}
            >
              {action.label}
            </button>
          ))}
        </div>
      )}

      {/* Table with horizontal scroll */}
      <div style={scrollContainer} tabIndex={0} role="region" aria-label="Data table">
        <table style={table} role="grid">
          <thead>
            <tr>
              {hasBulkActions && (
                <th style={{ ...thStyle, ...checkboxCell }}>
                  <input
                    type="checkbox"
                    checked={allSelected}
                    onChange={toggleSelectAll}
                    aria-label="Select all rows"
                    style={{ width: '16px', height: '16px' }}
                  />
                </th>
              )}
              {columns.map((col) => (
                <th
                  key={String(col.key)}
                  style={{
                    ...thStyle,
                    ...(col.sortable ? thSortable : {}),
                    ...(col.width ? { width: col.width } : {}),
                  }}
                  onClick={col.sortable ? () => handleSort(String(col.key)) : undefined}
                  aria-sort={
                    sort.field === String(col.key)
                      ? sort.order === 'asc'
                        ? 'ascending'
                        : 'descending'
                      : undefined
                  }
                >
                  {col.label}
                  {col.sortable && (
                    <SortIndicator
                      active={sort.field === String(col.key)}
                      order={sort.order}
                    />
                  )}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {isLoading && (
              <tr>
                <td
                  colSpan={columns.length + (hasBulkActions ? 1 : 0)}
                  style={loadingOverlay}
                >
                  Loading…
                </td>
              </tr>
            )}
            {isError && (
              <tr>
                <td
                  colSpan={columns.length + (hasBulkActions ? 1 : 0)}
                  style={errorState}
                >
                  {(error as Error)?.message ?? 'Failed to load data'}
                </td>
              </tr>
            )}
            {!isLoading && !isError && rows.length === 0 && (
              <tr>
                <td
                  colSpan={columns.length + (hasBulkActions ? 1 : 0)}
                  style={emptyState}
                >
                  No results found
                </td>
              </tr>
            )}
            {!isLoading &&
              !isError &&
              rows.map((item) => {
                const id = getRowId(item);
                return (
                  <tr
                    key={id}
                    style={onRowClick ? rowHover : undefined}
                    onClick={onRowClick ? () => onRowClick(item) : undefined}
                  >
                    {hasBulkActions && (
                      <td style={{ ...tdStyle, ...checkboxCell }}>
                        <input
                          type="checkbox"
                          checked={selectedIds.has(id)}
                          onChange={(e) => {
                            e.stopPropagation();
                            toggleSelectRow(id);
                          }}
                          onClick={(e) => e.stopPropagation()}
                          aria-label={`Select row ${id}`}
                          style={{ width: '16px', height: '16px' }}
                        />
                      </td>
                    )}
                    {columns.map((col) => (
                      <td
                        key={String(col.key)}
                        style={{
                          ...tdStyle,
                          ...(col.width ? { width: col.width } : {}),
                        }}
                      >
                        {col.render
                          ? col.render(item[col.key] as unknown, item)
                          : String(item[col.key] ?? '')}
                      </td>
                    ))}
                  </tr>
                );
              })}
          </tbody>
        </table>
      </div>

      {/* Pagination footer */}
      {!isLoading && !isError && totalPages > 0 && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalElements={totalElements}
          pageSize={pageSizeState}
          onPageChange={handlePageChange}
          onPageSizeChange={handlePageSizeChange}
        />
      )}
    </div>
  );
}
