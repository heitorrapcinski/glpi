import type { CSSProperties } from 'react';
import type { TimelineEntry as TEntry } from '@/hooks/useTickets';
import { getTimelineEntryColors } from '@/utils/status';

// ---------------------------------------------------------------------------
// TimelineEntry — Single timeline item with colored left border
// ---------------------------------------------------------------------------

export interface TimelineEntryProps {
  entry: TEntry;
  canApprove?: boolean;
  onApproveSolution?: (solutionId: string) => void;
  onRejectSolution?: (solutionId: string) => void;
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function getAuthorName(entry: TEntry): string {
  switch (entry.type) {
    case 'followup':
      return entry.data.authorName ?? 'Unknown';
    case 'task':
      return entry.data.assignedUserName ?? 'Unknown';
    case 'solution':
      return entry.data.authorName ?? 'Unknown';
    case 'document':
      return entry.data.authorName ?? 'Unknown';
    case 'validation':
      return entry.data.validatorName ?? 'Unknown';
    case 'log':
      return '';
  }
}

function getContent(entry: TEntry): string {
  switch (entry.type) {
    case 'followup':
    case 'task':
    case 'solution':
      return entry.data.content;
    case 'document':
      return entry.data.filename;
    case 'validation':
      return entry.data.comment ?? '';
    case 'log':
      return entry.data.message;
  }
}

function getCreatedAt(entry: TEntry): string {
  return entry.data.createdAt;
}

function getTypeLabel(type: TEntry['type']): string {
  const labels: Record<TEntry['type'], string> = {
    followup: 'Followup',
    task: 'Task',
    solution: 'Solution',
    document: 'Document',
    validation: 'Validation',
    log: 'Log',
  };
  return labels[type];
}

function isPendingSolution(entry: TEntry): boolean {
  return entry.type === 'solution' && entry.data.status === 'pending';
}

function formatTimestamp(iso: string): string {
  try {
    return new Intl.DateTimeFormat('en-US', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const card: CSSProperties = {
  borderRadius: '6px',
  padding: '0.75rem 1rem',
  marginBottom: '0.75rem',
  borderLeftWidth: '4px',
  borderLeftStyle: 'solid',
};

const header: CSSProperties = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  marginBottom: '0.5rem',
  fontSize: '0.8125rem',
};

const badge: CSSProperties = {
  display: 'inline-block',
  padding: '0.15em 0.5em',
  borderRadius: '4px',
  fontSize: '0.75rem',
  fontWeight: 600,
  backgroundColor: 'var(--glpi-timeline-badge-bg, rgb(97 97 97 / 15%))',
  color: 'var(--glpi-timeline-badge-fg, rgb(43 43 43 / 80%))',
};

const authorStyle: CSSProperties = {
  fontWeight: 600,
  marginLeft: '0.5rem',
};

const timestampStyle: CSSProperties = {
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.75rem',
};

const contentStyle: CSSProperties = {
  fontSize: '0.875rem',
  lineHeight: 1.6,
};

const approvalBar: CSSProperties = {
  display: 'flex',
  gap: '0.5rem',
  marginTop: '0.75rem',
  paddingTop: '0.5rem',
  borderTop: '1px solid var(--tblr-border-color, #d9dbde)',
};

const btnBase: CSSProperties = {
  padding: '0.35rem 0.75rem',
  borderRadius: '4px',
  border: 'none',
  fontWeight: 600,
  fontSize: '0.8125rem',
  cursor: 'pointer',
};

const approveBtn: CSSProperties = {
  ...btnBase,
  backgroundColor: '#22c55e',
  color: '#fff',
};

const rejectBtn: CSSProperties = {
  ...btnBase,
  backgroundColor: '#ef4444',
  color: '#fff',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function TimelineEntry({
  entry,
  canApprove = false,
  onApproveSolution,
  onRejectSolution,
}: TimelineEntryProps) {
  const colors = getTimelineEntryColors(entry.type);
  const author = getAuthorName(entry);
  const content = getContent(entry);
  const createdAt = getCreatedAt(entry);
  const label = getTypeLabel(entry.type);

  const cardStyle: CSSProperties = {
    ...card,
    backgroundColor: colors.backgroundColor,
    borderLeftColor: colors.borderColor,
    color: colors.foregroundColor,
  };

  const showApproval = canApprove && isPendingSolution(entry);

  return (
    <article style={cardStyle} aria-label={`${label} entry`}>
      <div style={header}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <span style={badge}>{label}</span>
          {author && <span style={authorStyle}>{author}</span>}
        </div>
        <time dateTime={createdAt} style={timestampStyle}>
          {formatTimestamp(createdAt)}
        </time>
      </div>

      {entry.type === 'document' ? (
        <div style={contentStyle}>{content}</div>
      ) : (
        <div
          style={contentStyle}
          dangerouslySetInnerHTML={{ __html: content }}
        />
      )}

      {showApproval && (
        <div style={approvalBar}>
          <button
            type="button"
            style={approveBtn}
            aria-label="Approve solution"
            onClick={() => onApproveSolution?.(entry.data.id)}
          >
            Approve
          </button>
          <button
            type="button"
            style={rejectBtn}
            aria-label="Reject solution"
            onClick={() => onRejectSolution?.(entry.data.id)}
          >
            Reject
          </button>
        </div>
      )}
    </article>
  );
}
