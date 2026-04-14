import { useState, useMemo } from 'react';
import type { CSSProperties } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  useKnowledgeList,
  useKnowledgeSearch,
  type KnowbaseItem,
  type KnowledgeListParams,
} from '@/hooks/useKnowledge';
import { useAuthStore } from '@/stores/authStore';
import { formatDate } from '@/utils/formatters';

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const pageContainer: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  height: '100%',
  minHeight: 0,
};

const pageHeader: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  marginBottom: '0.75rem',
};

const pageTitle: CSSProperties = {
  fontSize: '1.25rem',
  fontWeight: 700,
  margin: 0,
  color: 'var(--tblr-body-color, #1e293b)',
};

const contentLayout: CSSProperties = {
  display: 'flex',
  gap: '1rem',
  flex: 1,
  minHeight: 0,
};

const categoryPanel: CSSProperties = {
  flex: '0 0 240px',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '6px',
  padding: '0.75rem',
  overflowY: 'auto',
};

const categoryTitle: CSSProperties = {
  fontSize: '0.8125rem',
  fontWeight: 700,
  color: 'var(--tblr-secondary, #606f91)',
  textTransform: 'uppercase',
  letterSpacing: '0.03em',
  marginBottom: '0.5rem',
};

function categoryBtnStyle(active: boolean): CSSProperties {
  return {
    display: 'block',
    width: '100%',
    textAlign: 'left',
    padding: '0.35rem 0.5rem',
    border: 'none',
    borderRadius: '4px',
    backgroundColor: active
      ? 'var(--tblr-primary, rgb(254,201,92))'
      : 'transparent',
    color: active
      ? 'var(--tblr-primary-fg, #1e293b)'
      : 'var(--tblr-body-color, #1e293b)',
    fontWeight: active ? 600 : 400,
    fontSize: '0.875rem',
    cursor: 'pointer',
    transition: 'background-color 0.15s',
  };
}

const articlePanel: CSSProperties = {
  flex: 1,
  display: 'flex',
  flexDirection: 'column',
  minWidth: 0,
};

const searchInput: CSSProperties = {
  width: '100%',
  padding: '0.5rem 0.75rem',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '6px',
  fontSize: '0.875rem',
  marginBottom: '0.75rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  outline: 'none',
};

const articleList: CSSProperties = {
  flex: 1,
  overflowY: 'auto',
};

const articleRow: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0.65rem 0.75rem',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  cursor: 'pointer',
  transition: 'background-color 0.1s',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
};

const articleTitleStyle: CSSProperties = {
  fontWeight: 500,
  fontSize: '0.9375rem',
  color: 'var(--tblr-body-color, #1e293b)',
  flex: 1,
  minWidth: 0,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
};

const faqBadge: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  padding: '0.1rem 0.45rem',
  borderRadius: '4px',
  backgroundColor: 'var(--tblr-primary, rgb(254,201,92))',
  color: 'var(--tblr-primary-fg, #1e293b)',
  fontSize: '0.6875rem',
  fontWeight: 700,
  marginInlineStart: '0.5rem',
  flexShrink: 0,
};

const articleMeta: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  flexShrink: 0,
  marginInlineStart: '1rem',
  fontSize: '0.8125rem',
  color: 'var(--tblr-secondary, #606f91)',
};

const viewCountStyle: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.2rem',
};

const paginationBar: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0.5rem 0.75rem',
  borderTop: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  fontSize: '0.8125rem',
  color: 'var(--tblr-secondary, #606f91)',
  flexShrink: 0,
};

const paginationBtn: CSSProperties = {
  padding: '0.3rem 0.6rem',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '4px',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  cursor: 'pointer',
  fontSize: '0.8125rem',
};

const loadingState: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '3rem',
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.9375rem',
};

const emptyState: CSSProperties = {
  padding: '2rem',
  textAlign: 'center',
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.875rem',
};

// ---------------------------------------------------------------------------
// Placeholder category tree — in production this would come from the API
// ---------------------------------------------------------------------------

interface Category {
  id: string;
  name: string;
  children?: Category[];
}

const PLACEHOLDER_CATEGORIES: Category[] = [
  { id: '', name: 'All Categories' },
  { id: '1', name: 'General' },
  { id: '2', name: 'Hardware' },
  { id: '3', name: 'Software' },
  { id: '4', name: 'Network' },
  { id: '5', name: 'Security' },
];

// ---------------------------------------------------------------------------
// CategoryTree component
// ---------------------------------------------------------------------------

function CategoryTree({
  categories,
  selectedId,
  onSelect,
  depth = 0,
}: {
  categories: Category[];
  selectedId: string;
  onSelect: (id: string) => void;
  depth?: number;
}) {
  return (
    <ul
      style={{ listStyle: 'none', margin: 0, padding: 0, paddingInlineStart: depth > 0 ? '0.75rem' : 0 }}
      role="tree"
      aria-label={depth === 0 ? 'Knowledge base categories' : undefined}
    >
      {categories.map((cat) => (
        <li key={cat.id} role="treeitem" aria-selected={selectedId === cat.id}>
          <button
            type="button"
            style={categoryBtnStyle(selectedId === cat.id)}
            onClick={() => onSelect(cat.id)}
            aria-label={`Category: ${cat.name}`}
          >
            {cat.name}
          </button>
          {cat.children && cat.children.length > 0 && (
            <CategoryTree
              categories={cat.children}
              selectedId={selectedId}
              onSelect={onSelect}
              depth={depth + 1}
            />
          )}
        </li>
      ))}
    </ul>
  );
}

// ---------------------------------------------------------------------------
// KnowledgeListPage
// ---------------------------------------------------------------------------

export default function KnowledgeListPage() {
  const navigate = useNavigate();
  const profileInterface = useAuthStore((s) => s.user?.profileInterface);
  const isHelpdesk = profileInterface === 'helpdesk';

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [page, setPage] = useState(1);
  const pageSize = 25;

  // Build list params — helpdesk users only see FAQ articles
  const listParams = useMemo<KnowledgeListParams>(() => {
    const params: KnowledgeListParams = {
      page,
      pageSize,
      sort: 'updatedAt',
      order: 'desc',
    };
    if (selectedCategory) params.categoryId = selectedCategory;
    if (isHelpdesk) params.isFaq = true;
    return params;
  }, [page, pageSize, selectedCategory, isHelpdesk]);

  // Use search hook when there's a query, otherwise use list hook
  const isSearching = searchQuery.trim().length > 0;
  const searchParams = useMemo(
    () => ({ ...listParams }),
    [listParams],
  );

  const listQuery = useKnowledgeList(listParams);
  const searchQueryHook = useKnowledgeSearch(searchQuery.trim(), searchParams);

  const activeQuery = isSearching ? searchQueryHook : listQuery;
  const articles: KnowbaseItem[] = activeQuery.data?.data ?? [];

  return (
    <main style={pageContainer}>
      <div style={pageHeader}>
        <h1 style={pageTitle}>Knowledge Base</h1>
      </div>

      <div style={contentLayout}>
        {/* Category tree — left panel */}
        {!isHelpdesk && (
          <aside style={categoryPanel} aria-label="Category navigation">
            <div style={categoryTitle}>Categories</div>
            <CategoryTree
              categories={PLACEHOLDER_CATEGORIES}
              selectedId={selectedCategory}
              onSelect={(id) => {
                setSelectedCategory(id);
                setPage(1);
              }}
            />
          </aside>
        )}

        {/* Article list — right panel */}
        <section style={articlePanel}>
          {/* Search input */}
          <input
            type="search"
            style={searchInput}
            placeholder="Search knowledge base articles…"
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setPage(1);
            }}
            aria-label="Search knowledge base articles"
          />

          {/* Loading */}
          {activeQuery.isLoading && (
            <div style={loadingState} aria-live="polite" aria-busy="true">
              Loading articles…
            </div>
          )}

          {/* Error */}
          {activeQuery.isError && (
            <div style={{ ...emptyState, color: 'var(--tblr-danger, #ef4444)' }} role="alert">
              Failed to load articles.
            </div>
          )}

          {/* Empty */}
          {!activeQuery.isLoading && !activeQuery.isError && articles.length === 0 && (
            <div style={emptyState}>
              {isSearching
                ? 'No articles match your search.'
                : 'No knowledge base articles found.'}
            </div>
          )}

          {/* Article list */}
          {!activeQuery.isLoading && articles.length > 0 && (
            <>
              <div style={articleList} role="list" aria-label="Knowledge base articles">
                {articles.map((article) => (
                  <div
                    key={article.id}
                    style={articleRow}
                    role="listitem"
                    tabIndex={0}
                    onClick={() => navigate(`/knowledge/${article.id}`)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        navigate(`/knowledge/${article.id}`);
                      }
                    }}
                    aria-label={`Article: ${article.title}`}
                  >
                    <span style={articleTitleStyle}>
                      {article.title}
                      {article.isFaq && (
                        <span style={faqBadge} aria-label="FAQ article">
                          FAQ
                        </span>
                      )}
                    </span>
                    <span style={articleMeta}>
                      <span style={viewCountStyle} aria-label={`${article.viewCount} views`}>
                        👁 {article.viewCount}
                      </span>
                      <span>{formatDate(article.updatedAt)}</span>
                    </span>
                  </div>
                ))}
              </div>

              {/* Pagination */}
              <div style={paginationBar}>
                <span>Page {page}</span>
                <span style={{ display: 'flex', gap: '0.5rem' }}>
                  <button
                    type="button"
                    style={paginationBtn}
                    disabled={page <= 1}
                    onClick={() => setPage((p) => Math.max(1, p - 1))}
                    aria-label="Previous page"
                  >
                    ← Prev
                  </button>
                  <button
                    type="button"
                    style={paginationBtn}
                    disabled={articles.length < pageSize}
                    onClick={() => setPage((p) => p + 1)}
                    aria-label="Next page"
                  >
                    Next →
                  </button>
                </span>
              </div>
            </>
          )}
        </section>
      </div>
    </main>
  );
}
