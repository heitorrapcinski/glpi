import { useState, useCallback } from 'react';
import type { CSSProperties } from 'react';
import type { TaskFormData } from '@/hooks/useTickets';
import RichTextEditor from '@/components/common/RichTextEditor';
import ActorSelector from './ActorSelector';
import type { ActorOption } from './ActorSelector';

// ---------------------------------------------------------------------------
// TaskForm — Rich text editor, assigned user, dates, duration, status
// ---------------------------------------------------------------------------

export interface TaskFormProps {
  onSubmit: (data: TaskFormData) => void;
  onCancel: () => void;
  /** Available users for the assigned user selector. */
  userOptions?: ActorOption[];
}

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const form: CSSProperties = {
  padding: '1rem',
  borderRadius: '6px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  marginTop: '0.75rem',
};

const heading: CSSProperties = {
  margin: '0 0 0.75rem',
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: 'var(--tblr-body-color, #1e293b)',
};

const fieldGroup: CSSProperties = {
  display: 'grid',
  gridTemplateColumns: '1fr 1fr',
  gap: '0.75rem',
  marginTop: '0.75rem',
};

const fieldLabel: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  gap: '0.25rem',
  fontSize: '0.8125rem',
  color: 'var(--tblr-body-color, #1e293b)',
};

const fieldInput: CSSProperties = {
  padding: '0.45rem 0.65rem',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '4px',
  fontSize: '0.875rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  boxSizing: 'border-box',
};

const toggleRow: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  margin: '0.75rem 0',
  fontSize: '0.8125rem',
};

const buttonRow: CSSProperties = {
  display: 'flex',
  gap: '0.5rem',
  justifyContent: 'flex-end',
  marginTop: '0.75rem',
};

const btnPrimary: CSSProperties = {
  padding: '0.4rem 1rem',
  borderRadius: '4px',
  border: 'none',
  backgroundColor: 'var(--tblr-primary, rgb(254,201,92))',
  color: 'var(--tblr-primary-fg, #1e293b)',
  fontWeight: 600,
  fontSize: '0.8125rem',
  cursor: 'pointer',
};

const btnSecondary: CSSProperties = {
  padding: '0.4rem 1rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  fontWeight: 600,
  fontSize: '0.8125rem',
  cursor: 'pointer',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function TaskForm({ onSubmit, onCancel, userOptions = [] }: TaskFormProps) {
  const [content, setContent] = useState('');
  const [assignedUserId, setAssignedUserId] = useState('');
  const [status, setStatus] = useState(1); // 1=TODO, 2=DONE
  const [plannedStart, setPlannedStart] = useState('');
  const [plannedEnd, setPlannedEnd] = useState('');
  const [duration, setDuration] = useState('');
  const [isPrivate, setIsPrivate] = useState(false);

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      if (!content.trim()) return;
      onSubmit({
        content,
        assignedUserId,
        status,
        plannedStart: plannedStart || undefined,
        plannedEnd: plannedEnd || undefined,
        duration: duration ? Number(duration) : undefined,
        isPrivate,
      });
    },
    [content, assignedUserId, status, plannedStart, plannedEnd, duration, isPrivate, onSubmit],
  );

  return (
    <form onSubmit={handleSubmit} style={form} aria-label="Add task form">
      <div style={heading}>Add Task</div>

      <RichTextEditor
        value={content}
        onChange={setContent}
        placeholder="Describe the task…"
        ariaLabel="Task content"
      />

      <div style={fieldGroup}>
        <label style={fieldLabel}>
          Assigned to
          <ActorSelector
            value={assignedUserId}
            onChange={setAssignedUserId}
            options={userOptions}
            placeholder="Search user…"
            ariaLabel="Assigned user"
          />
        </label>

        <label style={fieldLabel}>
          Status
          <select
            value={status}
            onChange={(e) => setStatus(Number(e.target.value))}
            style={fieldInput}
            aria-label="Task status"
          >
            <option value={1}>TODO</option>
            <option value={2}>DONE</option>
          </select>
        </label>

        <label style={fieldLabel}>
          Planned start
          <input
            type="datetime-local"
            value={plannedStart}
            onChange={(e) => setPlannedStart(e.target.value)}
            style={fieldInput}
            aria-label="Planned start date"
          />
        </label>

        <label style={fieldLabel}>
          Planned end
          <input
            type="datetime-local"
            value={plannedEnd}
            onChange={(e) => setPlannedEnd(e.target.value)}
            style={fieldInput}
            aria-label="Planned end date"
          />
        </label>

        <label style={fieldLabel}>
          Duration (minutes)
          <input
            type="number"
            min={0}
            value={duration}
            onChange={(e) => setDuration(e.target.value)}
            placeholder="0"
            style={fieldInput}
            aria-label="Duration in minutes"
          />
        </label>
      </div>

      <label style={toggleRow}>
        <input
          type="checkbox"
          checked={isPrivate}
          onChange={(e) => setIsPrivate(e.target.checked)}
          aria-label="Mark as private"
        />
        <span>Private</span>
      </label>

      <div style={buttonRow}>
        <button type="button" style={btnSecondary} onClick={onCancel}>
          Cancel
        </button>
        <button type="submit" style={btnPrimary}>
          Submit
        </button>
      </div>
    </form>
  );
}
