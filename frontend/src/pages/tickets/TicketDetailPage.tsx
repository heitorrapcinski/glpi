import { useState, useCallback } from 'react';
import type { CSSProperties } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  useTicketDetail,
  useUpdateTicket,
  useAddTicketFollowup,
  useAddTicketTask,
  useAddTicketSolution,
  type TimelineEntry,
  type FollowupFormData,
  type TaskFormData,
  type SolutionFormData,
} from '@/hooks/useTickets';
import { usePreferencesStore } from '@/stores/preferencesStore';
import Timeline from '@/components/itil/Timeline';
import FieldsPanel from '@/components/itil/FieldsPanel';

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
  padding: '1rem 1.25rem 0.75rem',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  flexShrink: 0,
};

const headerMeta: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  marginBottom: '0.25rem',
};

const ticketIdBadge: CSSProperties = {
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

const ticketTitle: CSSProperties = {
  margin: 0,
  fontSize: '1.125rem',
  fontWeight: 700,
  color: 'var(--tblr-body-color, #1e293b)',
  lineHeight: 1.3,
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

const twoColumnLayout: CSSProperties = {
  display: 'flex',
  flex: 1,
  minHeight: 0,
  overflow: 'hidden',
};

const timelineColumn: CSSProperties = {
  flex: '0 0 66.666%',
  maxWidth: '66.666%',
  padding: '1rem 1.25rem',
  overflowY: 'auto',
  display: 'flex',
  flexDirection: 'column',
};

const fieldsPanelColumn: CSSProperties = {
  flex: '0 0 33.333%',
  maxWidth: '33.333%',
  display: 'flex',
  flexDirection: 'column',
  minHeight: 0,
  overflowY: 'auto',
};

const fieldsPanelCollapsed: CSSProperties = {
  flex: '0 0 90px',
  maxWidth: '90px',
  display: 'flex',
  flexDirection: 'column',
  minHeight: 0,
  overflowY: 'auto',
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
// Responsive style injection (below 768px → single column)
// ---------------------------------------------------------------------------

const RESPONSIVE_STYLE_ID = 'ticket-detail-responsive';

function ensureResponsiveStyles() {
  if (typeof document === 'undefined') return;
  if (document.getElementById(RESPONSIVE_STYLE_ID)) return;
  const style = document.createElement('style');
  style.id = RESPONSIVE_STYLE_ID;
  style.textContent = `
    @media (max-width: 767px) {
      .ticket-detail-layout {
        flex-direction: column !important;
        overflow: visible !important;
      }
      .ticket-detail-timeline {
        flex: 1 1 auto !important;
        max-width: 100% !important;
        overflow-y: visible !important;
      }
      .ticket-detail-fields {
        flex: 1 1 auto !important;
        max-width: 100% !important;
        overflow-y: visible !important;
        border-left: none !important;
        border-top: 1px solid var(--tblr-border-color, #d9dbde) !important;
      }
    }
  `;
  document.head.appendChild(style);
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function buildTimelineEntries(ticket: {
  followups: import('@/hooks/useTickets').Followup[];
  tasks: import('@/hooks/useTickets').Task[];
  solution: import('@/hooks/useTickets').Solution | null;
  validations: import('@/hooks/useTickets').Validation[];
}): TimelineEntry[] {
  const entries: TimelineEntry[] = [];

  for (const f of ticket.followups) {
    entries.push({ type: 'followup', data: f });
  }
  for (const t of ticket.tasks) {
    entries.push({ type: 'task', data: t });
  }
  if (ticket.solution) {
    entries.push({ type: 'solution', data: ticket.solution });
  }
  for (const v of ticket.validations) {
    entries.push({ type: 'validation', data: v });
  }

  return entries;
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function TicketDetailPage() {
  ensureResponsiveStyles();

  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const timelineOrder = usePreferencesStore((s) => s.timelineOrder);

  const [fieldsPanelCollapsedState, setFieldsPanelCollapsedState] = useState(false);

  const ticketId = id ?? '';

  const { data: ticketResponse, isLoading, isError, refetch } = useTicketDetail(ticketId);
  const updateTicket = useUpdateTicket(ticketId);
  const addFollowup = useAddTicketFollowup(ticketId);
  const addTask = useAddTicketTask(ticketId);
  const addSolution = useAddTicketSolution(ticketId);

  const ticket = ticketResponse?.data;

  const handleFieldUpdate = useCallback(
    (field: string, value: unknown) => {
      updateTicket.mutate({ [field]: value } as Parameters<typeof updateTicket.mutate>[0]);
    },
    [updateTicket],
  );

  const handleAddFollowup = useCallback(
    (data: FollowupFormData) => {
      addFollowup.mutate(data);
    },
    [addFollowup],
  );

  const handleAddTask = useCallback(
    (data: TaskFormData) => {
      addTask.mutate(data);
    },
    [addTask],
  );

  const handleAddSolution = useCallback(
    (data: SolutionFormData) => {
      addSolution.mutate(data);
    },
    [addSolution],
  );

  const toggleFieldsPanel = useCallback(() => {
    setFieldsPanelCollapsedState((v) => !v);
  }, []);

  // ---- Loading state ----
  if (isLoading) {
    return (
      <div style={pageContainer} role="main" aria-label="Ticket detail">
        <div style={loadingState} aria-live="polite" aria-busy="true">
          Loading ticket…
        </div>
      </div>
    );
  }

  // ---- Error state ----
  if (isError || !ticket) {
    return (
      <div style={pageContainer} role="main" aria-label="Ticket detail">
        <div style={errorState} role="alert">
          <span>Failed to load ticket.</span>
          <button type="button" style={retryBtn} onClick={() => refetch()}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  const timelineEntries = buildTimelineEntries(ticket);

  return (
    <div style={pageContainer} role="main" aria-label={`Ticket #${ticket.id}: ${ticket.title}`}>
      {/* Header */}
      <header style={pageHeader}>
        <button
          type="button"
          style={backBtn}
          aria-label="Back to ticket list"
          onClick={() => navigate('/tickets')}
        >
          ← Back
        </button>
        <div style={headerMeta}>
          <span style={ticketIdBadge} aria-label={`Ticket ID ${ticket.id}`}>
            #{ticket.id}
          </span>
        </div>
        <h1 style={ticketTitle}>{ticket.title}</h1>
      </header>

      {/* Two-column layout */}
      <div
        style={twoColumnLayout}
        className="ticket-detail-layout"
      >
        {/* Timeline column (8/12 ≈ 66.6%) */}
        <section
          style={timelineColumn}
          className="ticket-detail-timeline"
          aria-label="Ticket timeline"
        >
          <Timeline
            entries={timelineEntries}
            order={timelineOrder}
            onAddFollowup={handleAddFollowup}
            onAddTask={handleAddTask}
            onAddSolution={handleAddSolution}
          />
        </section>

        {/* Fields panel column (4/12 ≈ 33.3%) */}
        <div
          style={fieldsPanelCollapsedState ? fieldsPanelCollapsed : fieldsPanelColumn}
          className="ticket-detail-fields"
        >
          <FieldsPanel
            status={ticket.status}
            priority={ticket.priority}
            urgency={ticket.urgency}
            impact={ticket.impact}
            categoryId={ticket.categoryId}
            actors={ticket.actors}
            sla={ticket.sla}
            dates={{
              createdAt: ticket.createdAt,
              updatedAt: ticket.updatedAt,
              solvedAt: ticket.solvedAt,
              closedAt: ticket.closedAt,
            }}
            onUpdate={handleFieldUpdate}
            collapsed={fieldsPanelCollapsedState}
            onToggleCollapse={toggleFieldsPanel}
          />
        </div>
      </div>
    </div>
  );
}
