import { useState, useCallback } from 'react';
import type { CSSProperties } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  useChangeDetail,
  useUpdateChange,
  useAddChangeFollowup,
  useAddChangeTask,
  useAddChangeSolution,
  useChangeValidationAction,
} from '@/hooks/useChanges';
import type { ValidationStep } from '@/hooks/useChanges';
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

const changeIdBadge: CSSProperties = {
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

const changeTitle: CSSProperties = {
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

const fieldsPanelCollapsedStyle: CSSProperties = {
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

const linkedSection: CSSProperties = {
  marginBottom: '1.25rem',
  padding: '0.75rem 1rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '6px',
};

const linkedSectionTitle: CSSProperties = {
  fontSize: '0.8125rem',
  fontWeight: 700,
  color: 'var(--tblr-body-color, #1e293b)',
  marginBottom: '0.5rem',
};

const linkedItemLink: CSSProperties = {
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

const validationSection: CSSProperties = {
  marginBottom: '1.25rem',
  padding: '0.75rem 1rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '6px',
};

const validationStepRow: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0.5rem 0',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
};

const validationStatusBadge = (status: number): CSSProperties => ({
  display: 'inline-flex',
  alignItems: 'center',
  padding: '0.15rem 0.5rem',
  borderRadius: '4px',
  fontSize: '0.75rem',
  fontWeight: 600,
  backgroundColor:
    status === 2 ? '#dcfce7' : status === 3 ? '#fef2f2' : '#fef9c3',
  color:
    status === 2 ? '#166534' : status === 3 ? '#991b1b' : '#854d0e',
  border: `1px solid ${
    status === 2 ? '#86efac' : status === 3 ? '#fca5a5' : '#fde047'
  }`,
});

const approveBtn: CSSProperties = {
  padding: '0.25rem 0.6rem',
  borderRadius: '4px',
  border: '1px solid #22c55e',
  backgroundColor: '#dcfce7',
  color: '#166534',
  fontSize: '0.75rem',
  fontWeight: 600,
  cursor: 'pointer',
  marginRight: '0.3rem',
};

const rejectBtn: CSSProperties = {
  padding: '0.25rem 0.6rem',
  borderRadius: '4px',
  border: '1px solid #ef4444',
  backgroundColor: '#fef2f2',
  color: '#991b1b',
  fontSize: '0.75rem',
  fontWeight: 600,
  cursor: 'pointer',
};

// ---------------------------------------------------------------------------
// Responsive style injection
// ---------------------------------------------------------------------------

const RESPONSIVE_STYLE_ID = 'change-detail-responsive';

function ensureResponsiveStyles() {
  if (typeof document === 'undefined') return;
  if (document.getElementById(RESPONSIVE_STYLE_ID)) return;
  const style = document.createElement('style');
  style.id = RESPONSIVE_STYLE_ID;
  style.textContent = `
    @media (max-width: 767px) {
      .change-detail-layout {
        flex-direction: column !important;
        overflow: visible !important;
      }
      .change-detail-timeline {
        flex: 1 1 auto !important;
        max-width: 100% !important;
        overflow-y: visible !important;
      }
      .change-detail-fields {
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

function getValidationStatusLabel(status: number): string {
  switch (status) {
    case 1: return 'Waiting';
    case 2: return 'Approved';
    case 3: return 'Refused';
    default: return 'Unknown';
  }
}

function buildTimelineEntries(change: {
  followups: import('@/hooks/useTickets').Followup[];
  tasks: import('@/hooks/useTickets').Task[];
  solution: import('@/hooks/useTickets').Solution | null;
}): TimelineEntry[] {
  const entries: TimelineEntry[] = [];
  for (const f of change.followups) {
    entries.push({ type: 'followup', data: f });
  }
  for (const t of change.tasks) {
    entries.push({ type: 'task', data: t });
  }
  if (change.solution) {
    entries.push({ type: 'solution', data: change.solution });
  }
  return entries;
}

// ---------------------------------------------------------------------------
// Validation Workflow Sub-component
// ---------------------------------------------------------------------------

function ValidationWorkflow({
  steps,
  onApprove,
  onReject,
}: {
  steps: ValidationStep[];
  onApprove: (validationId: string) => void;
  onReject: (validationId: string) => void;
}) {
  if (steps.length === 0) return null;

  const sorted = [...steps].sort((a, b) => a.order - b.order);

  return (
    <div style={validationSection} aria-label="Validation workflow">
      <div style={linkedSectionTitle}>
        Validation Workflow ({steps.length})
      </div>
      {sorted.map((step, idx) => (
        <div
          key={step.id}
          style={{
            ...validationStepRow,
            ...(idx === sorted.length - 1 ? { borderBottom: 'none' } : {}),
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ fontSize: '0.8125rem', fontWeight: 500 }}>
              {step.validatorName ?? step.validatorId}
            </span>
            <span style={validationStatusBadge(step.status)}>
              {getValidationStatusLabel(step.status)}
            </span>
            {step.comment && (
              <span
                style={{
                  fontSize: '0.75rem',
                  color: 'var(--tblr-secondary, #606f91)',
                  fontStyle: 'italic',
                }}
              >
                "{step.comment}"
              </span>
            )}
          </div>
          {step.status === 1 && (
            <div>
              <button
                type="button"
                style={approveBtn}
                onClick={() => onApprove(step.id)}
                aria-label={`Approve validation by ${step.validatorName ?? step.validatorId}`}
              >
                ✓ Approve
              </button>
              <button
                type="button"
                style={rejectBtn}
                onClick={() => onReject(step.id)}
                aria-label={`Reject validation by ${step.validatorName ?? step.validatorId}`}
              >
                ✕ Reject
              </button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function ChangeDetailPage() {
  ensureResponsiveStyles();

  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const timelineOrder = usePreferencesStore((s) => s.timelineOrder);

  const [fieldsPanelCollapsedState, setFieldsPanelCollapsedState] = useState(false);

  const changeId = id ?? '';

  const { data: changeResponse, isLoading, isError, refetch } = useChangeDetail(changeId);
  const updateChange = useUpdateChange(changeId);
  const addFollowup = useAddChangeFollowup(changeId);
  const addTask = useAddChangeTask(changeId);
  const addSolution = useAddChangeSolution(changeId);
  const validationAction = useChangeValidationAction(changeId);

  const change = changeResponse?.data;

  const handleFieldUpdate = useCallback(
    (field: string, value: unknown) => {
      updateChange.mutate({ [field]: value } as Parameters<typeof updateChange.mutate>[0]);
    },
    [updateChange],
  );

  // Planning document handlers
  const handleImpactChange = useCallback(
    (html: string) => {
      updateChange.mutate({ planningDocuments: { impactContent: html } });
    },
    [updateChange],
  );

  const handleControlListChange = useCallback(
    (html: string) => {
      updateChange.mutate({ planningDocuments: { controlListContent: html } });
    },
    [updateChange],
  );

  const handleRolloutPlanChange = useCallback(
    (html: string) => {
      updateChange.mutate({ planningDocuments: { rolloutPlanContent: html } });
    },
    [updateChange],
  );

  const handleBackoutPlanChange = useCallback(
    (html: string) => {
      updateChange.mutate({ planningDocuments: { backoutPlanContent: html } });
    },
    [updateChange],
  );

  const handleChecklistChange = useCallback(
    (html: string) => {
      updateChange.mutate({ planningDocuments: { checklistContent: html } });
    },
    [updateChange],
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

  const handleApproveValidation = useCallback(
    (validationId: string) => {
      validationAction.mutate({ validationId, status: 2 });
    },
    [validationAction],
  );

  const handleRejectValidation = useCallback(
    (validationId: string) => {
      validationAction.mutate({ validationId, status: 3 });
    },
    [validationAction],
  );

  const toggleFieldsPanel = useCallback(() => {
    setFieldsPanelCollapsedState((v) => !v);
  }, []);

  // ---- Loading state ----
  if (isLoading) {
    return (
      <div style={pageContainer} role="main" aria-label="Change detail">
        <div style={loadingState} aria-live="polite" aria-busy="true">
          Loading change…
        </div>
      </div>
    );
  }

  // ---- Error state ----
  if (isError || !change) {
    return (
      <div style={pageContainer} role="main" aria-label="Change detail">
        <div style={errorState} role="alert">
          <span>Failed to load change.</span>
          <button type="button" style={retryBtn} onClick={() => refetch()}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  const timelineEntries = buildTimelineEntries(change);

  // Extra sections for FieldsPanel: planning documents
  const extraSections = [
    {
      key: 'impact-analysis',
      label: 'Impact Analysis',
      icon: '📊',
      content: (
        <div style={richTextSectionWrapper}>
          <RichTextEditor
            value={change.planningDocuments.impactContent}
            onChange={handleImpactChange}
            placeholder="Describe the impact of this change…"
            ariaLabel="Impact analysis"
          />
        </div>
      ),
    },
    {
      key: 'control-list',
      label: 'Control List',
      icon: '📋',
      content: (
        <div style={richTextSectionWrapper}>
          <RichTextEditor
            value={change.planningDocuments.controlListContent}
            onChange={handleControlListChange}
            placeholder="Define the control list…"
            ariaLabel="Control list"
          />
        </div>
      ),
    },
    {
      key: 'rollout-plan',
      label: 'Rollout Plan',
      icon: '🚀',
      content: (
        <div style={richTextSectionWrapper}>
          <RichTextEditor
            value={change.planningDocuments.rolloutPlanContent}
            onChange={handleRolloutPlanChange}
            placeholder="Describe the rollout plan…"
            ariaLabel="Rollout plan"
          />
        </div>
      ),
    },
    {
      key: 'backout-plan',
      label: 'Backout Plan',
      icon: '↩️',
      content: (
        <div style={richTextSectionWrapper}>
          <RichTextEditor
            value={change.planningDocuments.backoutPlanContent}
            onChange={handleBackoutPlanChange}
            placeholder="Describe the backout plan…"
            ariaLabel="Backout plan"
          />
        </div>
      ),
    },
    {
      key: 'checklist',
      label: 'Checklist',
      icon: '✅',
      content: (
        <div style={richTextSectionWrapper}>
          <RichTextEditor
            value={change.planningDocuments.checklistContent}
            onChange={handleChecklistChange}
            placeholder="Define the checklist…"
            ariaLabel="Checklist"
          />
        </div>
      ),
    },
  ];

  return (
    <div style={pageContainer} role="main" aria-label={`Change #${change.id}: ${change.title}`}>
      {/* Header */}
      <header style={pageHeader}>
        <button
          type="button"
          style={backBtn}
          aria-label="Back to change list"
          onClick={() => navigate('/changes')}
        >
          ← Back
        </button>
        <div style={headerMeta}>
          <span style={changeIdBadge} aria-label={`Change ID ${change.id}`}>
            #{change.id}
          </span>
        </div>
        <h1 style={changeTitle}>{change.title}</h1>
      </header>

      {/* Two-column layout */}
      <div style={twoColumnLayout} className="change-detail-layout">
        {/* Timeline column (8/12 ≈ 66.6%) */}
        <section
          style={timelineColumn}
          className="change-detail-timeline"
          aria-label="Change timeline"
        >
          {/* Validation workflow */}
          <ValidationWorkflow
            steps={change.validationSteps}
            onApprove={handleApproveValidation}
            onReject={handleRejectValidation}
          />

          {/* Linked tickets */}
          {change.linkedTicketIds.length > 0 && (
            <div style={linkedSection} aria-label="Linked tickets">
              <div style={linkedSectionTitle}>
                Linked Tickets ({change.linkedTicketIds.length})
              </div>
              <div>
                {change.linkedTicketIds.map((ticketId) => (
                  <Link
                    key={ticketId}
                    to={`/tickets/${ticketId}`}
                    style={linkedItemLink}
                    aria-label={`Navigate to ticket #${ticketId}`}
                  >
                    🎫 #{ticketId}
                  </Link>
                ))}
              </div>
            </div>
          )}

          {/* Linked problems */}
          {change.linkedProblemIds.length > 0 && (
            <div style={linkedSection} aria-label="Linked problems">
              <div style={linkedSectionTitle}>
                Linked Problems ({change.linkedProblemIds.length})
              </div>
              <div>
                {change.linkedProblemIds.map((problemId) => (
                  <Link
                    key={problemId}
                    to={`/problems/${problemId}`}
                    style={linkedItemLink}
                    aria-label={`Navigate to problem #${problemId}`}
                  >
                    ⚠️ #{problemId}
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
          style={fieldsPanelCollapsedState ? fieldsPanelCollapsedStyle : fieldsPanelColumn}
          className="change-detail-fields"
        >
          <FieldsPanel
            status={change.status}
            priority={change.priority}
            urgency={change.urgency}
            impact={change.impact}
            categoryId={null}
            actors={change.actors}
            sla={null}
            dates={{
              createdAt: change.createdAt,
              updatedAt: change.updatedAt,
              solvedAt: null,
              closedAt: change.closedAt,
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
