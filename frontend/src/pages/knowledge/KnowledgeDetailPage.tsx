import { useEffect, useRef } from 'react';
import type { CSSProperties } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  useKnowledgeDetail,
  useIncrementKnowledgeView,
  type LinkedItem,
} from '@/hooks/useKnowledge';
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

const headerSection: CSSProperties = {
  padding: '1rem 1.25rem 0.75rem',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  flexShrink: 0,
};

const backBtn: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.3rem',
  padding: '0.3rem 0.6rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  fontSize: '0.8125rem',
  cursor: 'pointer',
  fontWeight: 500,
  marginBottom: '0.5rem',
};

const headerMeta: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  marginBottom: '0.25rem',
  flexWrap: 'wrap',
};

const idBadge: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  padding: '0.15rem 0.5rem',
  borderRadius: '4px',
  backgroundColor: 'var(--tblr-bg-surface-secondary, #f5f7fb)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  fontSize: '0.75rem',
  fontWeight: 600,
  color: 'var(--tblr-secondary, #606f91)',
  fontFamily: 'monospace',
};

const faqBadge: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  padding: '0.15rem 0.5rem',
  borderRadius: '4px',
  backgroundColor: 'var(--tblr-primary, rgb(254,201,92))',
  color: 'var(--tblr-primary-fg, #1e293b)',
  fontSize: '0.75rem',
  fontWeight: 700,
};

const articleTitle: CSSProperties = {
  margin: 0,
  fontSize: '1.125rem',
  fontWeight: 700,
  color: 'var(--tblr-body-color, #1e293b)',
  lineHeight: 1.3,
};

const contentArea: CSSProperties = {
  flex: 1,
  overflowY: 'auto',
  padding: '1.25rem',
  display: 'flex',
  gap: '1.25rem',
};

const mainContent: CSSProperties = {
  flex: 1,
  minWidth: 0,
};

const sidePanel: CSSProperties = {
  flex: '0 0 260px',
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
};

const card: CSSProperties = {
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '6px',
  padding: '1rem',
};

const cardTitle: CSSProperties = {
  fontSize: '0.8125rem',
  fontWeight: 700,
  color: 'var(--tblr-secondary, #606f91)',
  textTransform: 'uppercase',
  letterSpacing: '0.03em',
  marginBottom: '0.5rem',
};

const fieldRow: CSSProperties = {
  display: 'flex',
  justifyContent: 'space-between',
  padding: '0.3rem 0',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  fontSize: '0.875rem',
};

const fieldLabel: CSSProperties = {
  fontWeight: 600,
  color: 'var(--tblr-secondary, #606f91)',
};

const fieldValue: CSSProperties = {
  color: 'var(--tblr-body-color, #1e293b)',
  textAlign: 'right',
};

const richContent: CSSProperties = {
  ...card,
  lineHeight: 1.6,
  fontSize: '0.9375rem',
  color: 'var(--tblr-body-color, #1e293b)',
  wordBreak: 'break-word',
};

const linkedItemLink: CSSProperties = {
  display: 'block',
  padding: '0.3rem 0',
  color: 'var(--tblr-link-color, #3a5693)',
  textDecoration: 'none',
  fontSize: '0.875rem',
};

const loadingState: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '3rem',
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.9375rem',
};

const errorState: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '3rem',
  gap: '1rem',
  color: 'var(--tblr-danger, #ef4444)',
};

const retryBtn: CSSProperties = {
  padding: '0.4rem 0.9rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-danger, #ef4444)',
  backgroundColor: 'transparent',
  color: 'var(--tblr-danger, #ef4444)',
  cursor: 'pointer',
  fontWeight: 600,
  fontSize: '0.875rem',
};

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function getLinkedItemRoute(item: LinkedItem): string {
  const typeMap: Record<string, string> = {
    Ticket: '/tickets',
    Problem: '/problems',
    Change: '/changes',
  };
  const base = typeMap[item.itemType];
  if (base) return `${base}/${item.itemId}`;
  return '#';
}

function getLinkedItemLabel(item: LinkedItem): string {
  return `${item.itemType} #${item.itemId}`;
}

// ---------------------------------------------------------------------------
// Responsive style injection
// ---------------------------------------------------------------------------

const RESPONSIVE_STYLE_ID = 'knowledge-detail-responsive';

function ensureResponsiveStyles() {
  if (typeof document === 'undefined') return;
  if (document.getElementById(RESPONSIVE_STYLE_ID)) return;
  const style = document.createElement('style');
  style.id = RESPONSIVE_STYLE_ID;
  style.textContent = `
    @media (max-width: 767px) {
      .kb-detail-content {
        flex-direction: column !important;
      }
      .kb-detail-side {
        flex: 1 1 auto !important;
      }
    }
  `;
  document.head.appendChild(style);
}

// ---------------------------------------------------------------------------
// KnowledgeDetailPage
// ---------------------------------------------------------------------------

export default function KnowledgeDetailPage() {
  ensureResponsiveStyles();

  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const articleId = id ?? '';

  const { data: articleResponse, isLoading, isError, refetch } = useKnowledgeDetail(articleId);
  const article = articleResponse?.data;
  const incrementView = useIncrementKnowledgeView();

  // Increment view counter once on mount when article ID is available
  const viewIncrementedRef = useRef<string | null>(null);
  useEffect(() => {
    if (articleId && viewIncrementedRef.current !== articleId) {
      viewIncrementedRef.current = articleId;
      incrementView.mutate(articleId);
    }
  }, [articleId, incrementView]);

  // ---- Loading state ----
  if (isLoading) {
    return (
      <div style={pageContainer} role="main" aria-label="Knowledge article detail">
        <div style={loadingState} aria-live="polite" aria-busy="true">
          Loading article…
        </div>
      </div>
    );
  }

  // ---- Error state ----
  if (isError || !article) {
    return (
      <div style={pageContainer} role="main" aria-label="Knowledge article detail">
        <div style={errorState} role="alert">
          <span>Failed to load article.</span>
          <button type="button" style={retryBtn} onClick={() => refetch()}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  const linkedTickets = article.linkedItems.filter((i) => i.itemType === 'Ticket');
  const linkedProblems = article.linkedItems.filter((i) => i.itemType === 'Problem');
  const linkedChanges = article.linkedItems.filter((i) => i.itemType === 'Change');
  const hasLinkedItems = linkedTickets.length > 0 || linkedProblems.length > 0 || linkedChanges.length > 0;

  return (
    <div style={pageContainer} role="main" aria-label={`Knowledge article #${article.id}: ${article.title}`}>
      {/* Header */}
      <header style={headerSection}>
        <button
          type="button"
          style={backBtn}
          aria-label="Back to knowledge base"
          onClick={() => navigate('/knowledge')}
        >
          ← Back
        </button>
        <div style={headerMeta}>
          <span style={idBadge} aria-label={`Article ID ${article.id}`}>
            #{article.id}
          </span>
          {article.isFaq && (
            <span style={faqBadge} aria-label="FAQ article">
              FAQ
            </span>
          )}
        </div>
        <h1 style={articleTitle}>{article.title}</h1>
      </header>

      {/* Content area */}
      <div style={contentArea} className="kb-detail-content">
        {/* Main — rendered rich text content */}
        <article style={mainContent}>
          <div
            style={richContent}
            dangerouslySetInnerHTML={{ __html: article.answer }}
            aria-label="Article content"
          />
        </article>

        {/* Side panel — metadata and linked items */}
        <aside style={sidePanel} className="kb-detail-side">
          {/* Article info */}
          <div style={card}>
            <div style={cardTitle}>Article Info</div>
            <div style={fieldRow}>
              <span style={fieldLabel}>Author</span>
              <span style={fieldValue}>{article.authorName ?? article.authorId}</span>
            </div>
            <div style={fieldRow}>
              <span style={fieldLabel}>Created</span>
              <span style={fieldValue}>{formatDate(article.createdAt)}</span>
            </div>
            <div style={fieldRow}>
              <span style={fieldLabel}>Updated</span>
              <span style={fieldValue}>{formatDate(article.updatedAt)}</span>
            </div>
            <div style={fieldRow}>
              <span style={fieldLabel}>Views</span>
              <span style={fieldValue}>{article.viewCount}</span>
            </div>
            {article.beginDate && (
              <div style={fieldRow}>
                <span style={fieldLabel}>Visible From</span>
                <span style={fieldValue}>{formatDate(article.beginDate)}</span>
              </div>
            )}
            {article.endDate && (
              <div style={{ ...fieldRow, borderBottom: 'none' }}>
                <span style={fieldLabel}>Visible Until</span>
                <span style={fieldValue}>{formatDate(article.endDate)}</span>
              </div>
            )}
          </div>

          {/* Linked items */}
          {hasLinkedItems && (
            <div style={card}>
              <div style={cardTitle}>Linked Items</div>

              {linkedTickets.length > 0 && (
                <div style={{ marginBottom: '0.5rem' }}>
                  <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--tblr-body-color, #1e293b)', marginBottom: '0.25rem' }}>
                    Tickets
                  </div>
                  {linkedTickets.map((item) => (
                    <Link
                      key={`${item.itemType}-${item.itemId}`}
                      to={getLinkedItemRoute(item)}
                      style={linkedItemLink}
                      aria-label={getLinkedItemLabel(item)}
                    >
                      {getLinkedItemLabel(item)}
                    </Link>
                  ))}
                </div>
              )}

              {linkedProblems.length > 0 && (
                <div style={{ marginBottom: '0.5rem' }}>
                  <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--tblr-body-color, #1e293b)', marginBottom: '0.25rem' }}>
                    Problems
                  </div>
                  {linkedProblems.map((item) => (
                    <Link
                      key={`${item.itemType}-${item.itemId}`}
                      to={getLinkedItemRoute(item)}
                      style={linkedItemLink}
                      aria-label={getLinkedItemLabel(item)}
                    >
                      {getLinkedItemLabel(item)}
                    </Link>
                  ))}
                </div>
              )}

              {linkedChanges.length > 0 && (
                <div>
                  <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--tblr-body-color, #1e293b)', marginBottom: '0.25rem' }}>
                    Changes
                  </div>
                  {linkedChanges.map((item) => (
                    <Link
                      key={`${item.itemType}-${item.itemId}`}
                      to={getLinkedItemRoute(item)}
                      style={linkedItemLink}
                      aria-label={getLinkedItemLabel(item)}
                    >
                      {getLinkedItemLabel(item)}
                    </Link>
                  ))}
                </div>
              )}
            </div>
          )}
        </aside>
      </div>
    </div>
  );
}
