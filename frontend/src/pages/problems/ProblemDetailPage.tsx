import { useState, useCallback } from 'react';
import type { CSSProperties } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  useProblemDetail,
  useUpdateProblem,
  useAddProblemFollowup,
  useAddProblemTask,
  useAddProblemSolution,
} from '@/hooks/useProblems';
import type {
  FollowupFormData,
  TaskFormData,
  SolutionFormData,
  TimelineEntry,
} from '@/hooks/useTickets';
import { usePreferencesStore } from '@/stores/preferencesStore';
import Timeline from '@/components/itil/Timeline';
import FieldsPanel from '@/components/itil/FieldsPanel';
import RichTextEditor from '@/components/common/RichTextEditor';

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

const problemIdBadge: CSSProperties = {
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

const problemTitle: CSSProperties = {
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

const linkedTicketsSection: CSSProperties = {
  marginBottom: '1.25rem',
  padding: '0.75rem 1rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '6px',
};

const linkedTicketsSectionTitle: CSSProperties = {
  fontSize: '0.8125rem',
  fontWeight: 700,
  color: 'var(--tblr-body-color, #1e293b)',
  marginBottom: '0.5rem',
};

const linkedTicketLink: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.3rem',
  padding: '0.2rem 0.5rem',
  borderRadius: '4px',
  backgroundColor: 'var(--tblr-bg-surface-secondary, #f5f7fb)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  fontSize: '0.8125rem',
  color: 'var(--tblr-link-color, #3a5693)',
  textDecoration: 'none',
  fontWeight: 500,
  marginRight: '0.4rem',
  marginBottom: '0.4rem',
};

const richTextSectionWrapper: CSSProperties = {
  fontSize: '0.8125rem',
};

// ---------------------------------------------------------------------------
// Responsive style injection
// ---------------------------------------------------------------------------

const RESPONSIVE_STYLE_ID = 'problem-detail-responsive';

function ensureResponsiveStyles() {
  if (typeof document === 'undefined') return;
  if (document.getElementById(RESPONSIVE_STYLE_ID)) return;
  const style = document.createElement('style');
  style.id = RESPONSIVE_STYLE_ID;
  style.textContent = `
    @media (max-width: 767px) {
      .problem-detail-layout {
        flex-direction: column !important;
        overflow: visible !important;
      }
      .problem-detail-timeline {
        flex: 1 1 auto !important;
        max-width: 100% !important;
        overflow-y: visible !important;
      }
      .problem-detail-fields {
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

function buildTimelineEntries(problem: {
  followups: import('@/hooks/useTickets').Followup[];
  tasks: import('@/hooks/useTickets').Task[];
  solution: import('@/hooks/useTickets').Solution | null;
}): TimelineEntry[] {
  const entries: TimelineEntry[] = [];
  for (const f of problem.followups) {
    entries.push({ type: 'followup', data: f });
  }
  for (const t of problem.tasks) {
    entries.push({ type: 'task', data: t });
  }
  if (problem.solution) {
    entries.push({ type: 'solution', data: problem.solution });
  }
  return entries;
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function ProblemDetailPage() {
  ensureResponsiveStyles();

  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const timelineOrder = usePreferencesStore((s) => s.timelineOrder);

  const [fieldsPanelCollapsedState, setFieldsPanelCollapsedState] = useState(false);

  const problemId = id ?? '';

  const { data: problemResponse, isLoading, isError, refetch } = useProblemDetail(problemId);
  const updateProblem = useUpdateProblem(problemId);
  const addFollowup = useAddProblemFollowup(problemId);
  const addTask = useAddProblemTask(problemId);
  const addSolution = useAddProblemSolution(problemId);

  const problem = problemResponse?.data;

  const handleFieldUpdate = useCallback(
    (field: string, value: unknown) => {
      updateProblem.mutate({ [field]: value } as Parameters<typeof updateProblem.mutate>[0]);
    },
    [updateProblem],
  );

  const handleImpactChange = useCallback(
    (html: string) => { updateProblem.mutate({ impactContent: html }); },
    [updateProblem],
  );

  const handleCauseChange = useCallback(
    (html: string) => { updateProblem.mutate({ causeContent: html }); },
    [updateProblem],
  );

  const handleSymptomChange = useCallback(
    (html: string) => { updateProblem.mutate({ symptomContent: html }); },
    [updateProblem],
  );

  const handleAddFollowup = useCallback(
    (data: FollowupFormData) => { addFollowup.mutate(data); },
    [addFollowup],
  );

  const handleAddTask = useCallback(
    (data: TaskFormData) => { addTask.mutate(data); },
    [addTask],
  );

  const handleAddSolution = useCallback(
    (data: SolutionFormData) => { addSolution.mutate(data); },
    [addSolution],
  );

  const toggleFieldsPanel = useCallback(() => {
    setFieldsPanelCollapsedState((v) => !v);
  }, []);

  // ---- Loading state ----
  if (isLoading) {
    return (
      <div style={pageContainer} role="main" aria-label="Problem detail">
        <div style={loadingState} aria-live="polite" aria-busy="true">
          Loading problem…
        </div>
      </div>
    );
  }

  // ---- Error state ----
  if (isError || !problem) {
    return (
      <div style={pageContainer} role="main" aria-label="Problem detail">
        <div style={errorState} role="alert">
          <span>Failed to load problem.</span>
          <button type="button" style={retryBtn} onClick={() => refetch()}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  const timelineEntries = buildTimelineEntries(problem);

  // Extra sections for FieldsPanel: impact analysis, root cause, symptom description
  const extraSections = [
    {
      key: 'impact-analysis',
      label: 'Impact Analysis',
      icon: '📊',
      content: (
        <div style={richTextSectionWrapper}>
          <RichTextEditor
            value={problem.impactContent}
            onChange={handleImpactChange}
            placeholder="Describe the impact of this problem…"
            ariaLabel="Impact analysis"
          />
        </div>
      ),
    },
    {
      key: 'root-cause',
      label: 'Root Cause',
      icon: '🔍',
      content: (
        <div style={richTextSectionWrapper}>
          <RichTextEditor
            value={problem.causeContent}
            onChange={handleCauseChange}
            placeholder="Describe the root cause of this problem…"
            ariaLabel="Root cause analysis"
          />
        </div>
      ),
    },
    {
      key: 'symptom-description',
      label: 'Symptom Description',
      icon: '🩺',
      content: (
        <div style={richTextSectionWrapper}>
          <RichTextEditor
            value={problem.symptomContent}
            onChange={handleSymptomChange}
            placeholder="Describe the symptoms observed…"
            ariaLabel="Symptom description"
          />
        </div>
      ),
    },
  ];

  return (
    <div style={pageContainer} role="main" aria-label={`Problem #${problem.id}: ${problem.title}`}>
      {/* Header */}
      <header style={pageHeader}>
        <button
          type="button"
          style={backBtn}
          aria-label="Back to problem list"
          onClick={() => navigate('/problems')}
        >
          ← Back
        </button>
        <div style={headerMeta}>
          <span style={problemIdBadge} aria-label={`Problem ID ${problem.id}`}>
            #{problem.id}
          </span>
        </div>
        <h1 style={problemTitle}>{problem.title}</h1>
      </header>

      {/* Two-column layout */}
      <div style={twoColumnLayout} className="problem-detail-layout">
        {/* Timeline column (8/12 ≈ 66.6%) */}
        <section
          style={timelineColumn}
          className="problem-detail-timeline"
          aria-label="Problem timeline"
        >
          {/* Linked tickets */}
          {problem.linkedTicketIds.length > 0 && (
            <div style={linkedTicketsSection} aria-label="Linked tickets">
              <div style={linkedTicketsSectionTitle}>
                Linked Tickets ({problem.linkedTicketIds.length})
              </div>
              <div>
                {problem.linkedTicketIds.map((ticketId) => (
                  <Link
                    key={ticketId}
                    to={`/tickets/${ticketId}`}
                    style={linkedTicketLink}
                    aria-label={`Navigate to ticket #${ticketId}`}
                  >
                    🎫 #{ticketId}
                  </Link>
                ))}
              </div>
            </div>
          )}

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
          className="problem-detail-fields"
        >
          <FieldsPanel
            status={problem.status}
            priority={problem.priority}
            urgency={problem.urgency}
            impact={problem.impact}
            categoryId={null}
            actors={problem.actors}
            sla={null}
            dates={{
              createdAt: problem.createdAt,
              updatedAt: problem.updatedAt,
              solvedAt: problem.solvedAt,
              closedAt: problem.closedAt,
            }}
            onUpdate={handleFieldUpdate}
            collapsed={fieldsPanelCollapsedState}
            onToggleCollapse={toggleFieldsPanel}
            extraSections={extraSections}
          />
        </div>
      </div>
    </div>
  );
}
