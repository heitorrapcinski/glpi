import { useMemo, useState, useCallback } from 'react';
import type { CSSProperties } from 'react';
import type {
  TimelineEntry as TEntry,
  FollowupFormData,
  TaskFormData,
  SolutionFormData,
} from '@/hooks/useTickets';
import TimelineEntry from './TimelineEntry';
import FollowupForm from './FollowupForm';
import TaskForm from './TaskForm';
import SolutionForm from './SolutionForm';
import type { ActorOption } from './ActorSelector';

// ---------------------------------------------------------------------------
// Timeline — Chronological entry list with action buttons
// ---------------------------------------------------------------------------

export interface TimelineProps {
  entries: TEntry[];
  order: 'newest' | 'oldest';
  onAddFollowup: (data: FollowupFormData) => void;
  onAddTask: (data: TaskFormData) => void;
  onAddSolution: (data: SolutionFormData) => void;
  canApprove?: boolean;
  onApproveSolution?: (solutionId: string) => void;
  onRejectSolution?: (solutionId: string) => void;
  /** Available users for the task assigned-user selector. */
  userOptions?: ActorOption[];
  /** Available solution types for the solution form. */
  solutionTypes?: { id: string; name: string }[];
}

type ActionForm = 'followup' | 'task' | 'solution' | 'document' | null;

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function getCreatedAt(entry: TEntry): string {
  return entry.data.createdAt;
}

function sortEntries(entries: TEntry[], order: 'newest' | 'oldest'): TEntry[] {
  return [...entries].sort((a, b) => {
    const dateA = new Date(getCreatedAt(a)).getTime();
    const dateB = new Date(getCreatedAt(b)).getTime();
    return order === 'newest' ? dateB - dateA : dateA - dateB;
  });
}

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const container: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
};

const entryList: CSSProperties = {
  flex: 1,
};

const actionBar: CSSProperties = {
  display: 'flex',
  flexWrap: 'wrap',
  gap: '0.5rem',
  padding: '0.75rem 0',
  borderTop: '1px solid var(--tblr-border-color, #d9dbde)',
  marginTop: '0.5rem',
};

const actionBtn: CSSProperties = {
  padding: '0.4rem 0.75rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  fontWeight: 600,
  fontSize: '0.8125rem',
  cursor: 'pointer',
};

const documentPlaceholder: CSSProperties = {
  padding: '1rem',
  marginTop: '0.75rem',
  borderRadius: '6px',
  border: '1px dashed var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface-secondary, #fafbfc)',
  fontSize: '0.875rem',
  color: 'var(--tblr-secondary, #606f91)',
  textAlign: 'center',
};

const emptyState: CSSProperties = {
  padding: '2rem',
  textAlign: 'center',
  color: 'var(--tblr-secondary, #606f91)',
  fontSize: '0.875rem',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function Timeline({
  entries,
  order,
  onAddFollowup,
  onAddTask,
  onAddSolution,
  canApprove = false,
  onApproveSolution,
  onRejectSolution,
  userOptions = [],
  solutionTypes = [],
}: TimelineProps) {
  const [activeForm, setActiveForm] = useState<ActionForm>(null);

  const sorted = useMemo(() => sortEntries(entries, order), [entries, order]);

  const toggleForm = (form: ActionForm) => {
    setActiveForm((prev) => (prev === form ? null : form));
  };

  const closeForm = useCallback(() => setActiveForm(null), []);

  const handleFollowupSubmit = useCallback(
    (data: FollowupFormData) => {
      onAddFollowup(data);
      setActiveForm(null);
    },
    [onAddFollowup],
  );

  const handleTaskSubmit = useCallback(
    (data: TaskFormData) => {
      onAddTask(data);
      setActiveForm(null);
    },
    [onAddTask],
  );

  const handleSolutionSubmit = useCallback(
    (data: SolutionFormData) => {
      onAddSolution(data);
      setActiveForm(null);
    },
    [onAddSolution],
  );

  return (
    <div style={container} role="region" aria-label="Timeline">
      <div style={entryList}>
        {sorted.length === 0 ? (
          <div style={emptyState}>No timeline entries yet.</div>
        ) : (
          sorted.map((entry) => (
            <TimelineEntry
              key={`${entry.type}-${entry.data.id}`}
              entry={entry}
              canApprove={canApprove}
              onApproveSolution={onApproveSolution}
              onRejectSolution={onRejectSolution}
            />
          ))
        )}
      </div>

      {/* Action forms */}
      {activeForm === 'followup' && (
        <FollowupForm onSubmit={handleFollowupSubmit} onCancel={closeForm} />
      )}
      {activeForm === 'task' && (
        <TaskForm onSubmit={handleTaskSubmit} onCancel={closeForm} userOptions={userOptions} />
      )}
      {activeForm === 'solution' && (
        <SolutionForm onSubmit={handleSolutionSubmit} onCancel={closeForm} solutionTypes={solutionTypes} />
      )}
      {activeForm === 'document' && (
        <div style={documentPlaceholder} role="region" aria-label="Add document form">
          Document upload form — coming soon
        </div>
      )}

      {/* Action buttons */}
      <div style={actionBar} role="toolbar" aria-label="Timeline actions">
        <button
          type="button"
          style={actionBtn}
          aria-label="Add Followup"
          aria-pressed={activeForm === 'followup'}
          onClick={() => toggleForm('followup')}
        >
          Add Followup
        </button>
        <button
          type="button"
          style={actionBtn}
          aria-label="Add Task"
          aria-pressed={activeForm === 'task'}
          onClick={() => toggleForm('task')}
        >
          Add Task
        </button>
        <button
          type="button"
          style={actionBtn}
          aria-label="Add Solution"
          aria-pressed={activeForm === 'solution'}
          onClick={() => toggleForm('solution')}
        >
          Add Solution
        </button>
        <button
          type="button"
          style={actionBtn}
          aria-label="Add Document"
          aria-pressed={activeForm === 'document'}
          onClick={() => toggleForm('document')}
        >
          Add Document
        </button>
      </div>
    </div>
  );
}
