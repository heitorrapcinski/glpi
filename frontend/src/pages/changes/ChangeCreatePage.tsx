import { useState, useCallback } from 'react';
import type { CSSProperties, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { useCreateChange, type ChangeCreateData } from '@/hooks/useChanges';
import { useAuthStore } from '@/stores/authStore';
import { mapValidationErrors } from '@/utils/validators';
import type { ApiError } from '@/api/types';
import RichTextEditor from '@/components/common/RichTextEditor';
import ActorSelector from '@/components/itil/ActorSelector';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const URGENCY_OPTIONS = [
  { value: 1, label: 'Very low' },
  { value: 2, label: 'Low' },
  { value: 3, label: 'Medium' },
  { value: 4, label: 'High' },
  { value: 5, label: 'Very high' },
] as const;

const IMPACT_OPTIONS = [
  { value: 1, label: 'Low' },
  { value: 2, label: 'Medium' },
  { value: 3, label: 'High' },
] as const;

const CATEGORIES = [
  { id: 'cat-1', label: 'Hardware' },
  { id: 'cat-2', label: 'Software' },
  { id: 'cat-3', label: 'Network' },
  { id: 'cat-4', label: 'Security' },
  { id: 'cat-5', label: 'General' },
];

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

const pageTitle: CSSProperties = {
  margin: 0,
  fontSize: '1.125rem',
  fontWeight: 700,
  color: 'var(--tblr-body-color, #1e293b)',
};

const formWrapper: CSSProperties = {
  flex: 1,
  overflowY: 'auto',
  padding: '1.25rem',
};

const formCard: CSSProperties = {
  maxWidth: '800px',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '6px',
  padding: '1.5rem',
};

const fieldGroup: CSSProperties = {
  marginBottom: '1.25rem',
};

const labelStyle: CSSProperties = {
  display: 'block',
  marginBottom: '0.35rem',
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: 'var(--tblr-body-color, #1e293b)',
};

const requiredMark: CSSProperties = {
  color: 'var(--tblr-danger, #ef4444)',
  marginInlineStart: '0.15rem',
};

const inputStyle: CSSProperties = {
  width: '100%',
  padding: '0.45rem 0.65rem',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '4px',
  fontSize: '0.875rem',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  outline: 'none',
  boxSizing: 'border-box' as const,
};

const inputError: CSSProperties = {
  ...inputStyle,
  borderColor: 'var(--tblr-danger, #ef4444)',
};

const selectStyle: CSSProperties = {
  ...inputStyle,
  appearance: 'auto' as const,
};

const selectError: CSSProperties = {
  ...selectStyle,
  borderColor: 'var(--tblr-danger, #ef4444)',
};

const errorText: CSSProperties = {
  color: 'var(--tblr-danger, #ef4444)',
  fontSize: '0.75rem',
  marginTop: '0.25rem',
};

const formActions: CSSProperties = {
  display: 'flex',
  gap: '0.75rem',
  justifyContent: 'flex-end',
  paddingTop: '1rem',
  borderTop: '1px solid var(--tblr-border-color, #d9dbde)',
  marginTop: '0.5rem',
};

const submitBtn: CSSProperties = {
  padding: '0.5rem 1.25rem',
  borderRadius: '4px',
  border: 'none',
  backgroundColor: 'var(--tblr-primary, rgb(254,201,92))',
  color: 'var(--tblr-primary-fg, #1e293b)',
  fontWeight: 600,
  fontSize: '0.875rem',
  cursor: 'pointer',
};

const submitBtnDisabled: CSSProperties = {
  ...submitBtn,
  opacity: 0.6,
  cursor: 'not-allowed',
};

const cancelBtn: CSSProperties = {
  padding: '0.5rem 1.25rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  fontWeight: 500,
  fontSize: '0.875rem',
  cursor: 'pointer',
};

const twoCol: CSSProperties = {
  display: 'grid',
  gridTemplateColumns: '1fr 1fr',
  gap: '1rem',
};

const sectionTitle: CSSProperties = {
  fontSize: '0.875rem',
  fontWeight: 700,
  color: 'var(--tblr-body-color, #1e293b)',
  marginBottom: '0.75rem',
  paddingBottom: '0.35rem',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
};

const generalError: CSSProperties = {
  padding: '0.75rem 1rem',
  borderRadius: '4px',
  backgroundColor: '#fef2f2',
  border: '1px solid var(--tblr-danger, #ef4444)',
  color: 'var(--tblr-danger, #ef4444)',
  fontSize: '0.8125rem',
  marginBottom: '1rem',
};

const linkedItemsArea: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  gap: '0.5rem',
};

const linkedItemRow: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
};

const removeBtn: CSSProperties = {
  padding: '0.2rem 0.5rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-danger, #ef4444)',
  backgroundColor: 'transparent',
  color: 'var(--tblr-danger, #ef4444)',
  fontSize: '0.75rem',
  cursor: 'pointer',
};

const addItemBtn: CSSProperties = {
  padding: '0.3rem 0.75rem',
  borderRadius: '4px',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  color: 'var(--tblr-body-color, #1e293b)',
  fontSize: '0.8125rem',
  cursor: 'pointer',
  fontWeight: 500,
};

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function getFieldErrors(fieldErrors: Record<string, string[]>, field: string): string[] {
  return fieldErrors[field] ?? [];
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function ChangeCreatePage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const createChange = useCreateChange();

  // Form state
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [urgency, setUrgency] = useState<number>(3);
  const [impact, setImpact] = useState<number>(2);
  const [categoryId, setCategoryId] = useState('');
  const [assignedUserId, setAssignedUserId] = useState('');
  const [observerId, setObserverId] = useState('');
  const [linkedTicketInput, setLinkedTicketInput] = useState('');
  const [linkedTicketIds, setLinkedTicketIds] = useState<string[]>([]);
  const [linkedProblemInput, setLinkedProblemInput] = useState('');
  const [linkedProblemIds, setLinkedProblemIds] = useState<string[]>([]);

  // Validation state
  const [fieldErrors, setFieldErrors] = useState<Record<string, string[]>>({});
  const [generalErrorMsg, setGeneralErrorMsg] = useState('');

  const handleAddLinkedTicket = useCallback(() => {
    const trimmed = linkedTicketInput.trim();
    if (trimmed && !linkedTicketIds.includes(trimmed)) {
      setLinkedTicketIds((prev) => [...prev, trimmed]);
    }
    setLinkedTicketInput('');
  }, [linkedTicketInput, linkedTicketIds]);

  const handleRemoveLinkedTicket = useCallback((ticketId: string) => {
    setLinkedTicketIds((prev) => prev.filter((id) => id !== ticketId));
  }, []);

  const handleAddLinkedProblem = useCallback(() => {
    const trimmed = linkedProblemInput.trim();
    if (trimmed && !linkedProblemIds.includes(trimmed)) {
      setLinkedProblemIds((prev) => [...prev, trimmed]);
    }
    setLinkedProblemInput('');
  }, [linkedProblemInput, linkedProblemIds]);

  const handleRemoveLinkedProblem = useCallback((problemId: string) => {
    setLinkedProblemIds((prev) => prev.filter((id) => id !== problemId));
  }, []);

  const handleSubmit = useCallback(
    async (e: FormEvent) => {
      e.preventDefault();
      setFieldErrors({});
      setGeneralErrorMsg('');

      const actors: ChangeCreateData['actors'] = [];
      if (user) {
        actors.push({ actorType: 1, actorKind: 'user', actorId: user.userId });
      }
      if (assignedUserId) {
        actors.push({ actorType: 2, actorKind: 'user', actorId: assignedUserId });
      }
      if (observerId) {
        actors.push({ actorType: 3, actorKind: 'user', actorId: observerId });
      }

      const payload: ChangeCreateData = {
        title,
        content,
        urgency,
        impact,
        ...(categoryId ? { categoryId } : {}),
        actors,
        ...(linkedTicketIds.length > 0 ? { linkedTicketIds } : {}),
        ...(linkedProblemIds.length > 0 ? { linkedProblemIds } : {}),
      };

      try {
        const result = await createChange.mutateAsync(payload);
        navigate(`/changes/${result.data.id}`);
      } catch (err: unknown) {
        if (err instanceof AxiosError && err.response?.status === 422) {
          const apiErr = err.response.data as ApiError;
          if (apiErr.details) {
            setFieldErrors(mapValidationErrors(apiErr.details));
          } else if (apiErr.message) {
            setGeneralErrorMsg(apiErr.message);
          }
        } else if (err instanceof AxiosError && err.response) {
          const apiErr = err.response.data as ApiError | undefined;
          setGeneralErrorMsg(apiErr?.message ?? 'An unexpected error occurred.');
        } else {
          setGeneralErrorMsg('An unexpected error occurred.');
        }
      }
    },
    [
      title, content, urgency, impact, categoryId,
      assignedUserId, observerId, linkedTicketIds, linkedProblemIds,
      user, createChange, navigate,
    ],
  );

  const isSubmitting = createChange.isPending;

  return (
    <div style={pageContainer} role="main" aria-label="Create change">
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
        <h1 style={pageTitle}>New Change</h1>
      </header>

      {/* Form */}
      <div style={formWrapper}>
        <form style={formCard} onSubmit={handleSubmit} noValidate>
          {generalErrorMsg && (
            <div style={generalError} role="alert">{generalErrorMsg}</div>
          )}

          {/* --- Title --- */}
          <div style={fieldGroup}>
            <label style={labelStyle} htmlFor="change-title">
              Title<span style={requiredMark} aria-hidden="true">*</span>
            </label>
            <input
              id="change-title"
              type="text"
              style={getFieldErrors(fieldErrors, 'title').length ? inputError : inputStyle}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Brief description of the change"
              aria-required="true"
              autoFocus
            />
            {getFieldErrors(fieldErrors, 'title').map((msg) => (
              <div key={msg} style={errorText} role="alert">{msg}</div>
            ))}
          </div>

          {/* --- Content (Rich Text Editor) --- */}
          <div style={fieldGroup}>
            <label style={labelStyle}>
              Description<span style={requiredMark} aria-hidden="true">*</span>
            </label>
            <div
              style={getFieldErrors(fieldErrors, 'content').length
                ? { border: '1px solid var(--tblr-danger, #ef4444)', borderRadius: '6px' }
                : undefined}
            >
              <RichTextEditor
                value={content}
                onChange={setContent}
                placeholder="Describe the change in detail…"
                ariaLabel="Change description"
              />
            </div>
            {getFieldErrors(fieldErrors, 'content').map((msg) => (
              <div key={msg} style={errorText} role="alert">{msg}</div>
            ))}
          </div>

          {/* --- Urgency & Impact row --- */}
          <div style={twoCol}>
            <div style={fieldGroup}>
              <label style={labelStyle} htmlFor="change-urgency">
                Urgency<span style={requiredMark} aria-hidden="true">*</span>
              </label>
              <select
                id="change-urgency"
                style={getFieldErrors(fieldErrors, 'urgency').length ? selectError : selectStyle}
                value={urgency}
                onChange={(e) => setUrgency(Number(e.target.value))}
                aria-required="true"
              >
                {URGENCY_OPTIONS.map((u) => (
                  <option key={u.value} value={u.value}>{u.label}</option>
                ))}
              </select>
              {getFieldErrors(fieldErrors, 'urgency').map((msg) => (
                <div key={msg} style={errorText} role="alert">{msg}</div>
              ))}
            </div>

            <div style={fieldGroup}>
              <label style={labelStyle} htmlFor="change-impact">
                Impact<span style={requiredMark} aria-hidden="true">*</span>
              </label>
              <select
                id="change-impact"
                style={getFieldErrors(fieldErrors, 'impact').length ? selectError : selectStyle}
                value={impact}
                onChange={(e) => setImpact(Number(e.target.value))}
                aria-required="true"
              >
                {IMPACT_OPTIONS.map((i) => (
                  <option key={i.value} value={i.value}>{i.label}</option>
                ))}
              </select>
              {getFieldErrors(fieldErrors, 'impact').map((msg) => (
                <div key={msg} style={errorText} role="alert">{msg}</div>
              ))}
            </div>
          </div>

          {/* --- Category --- */}
          <div style={fieldGroup}>
            <label style={labelStyle} htmlFor="change-category">Category</label>
            <select
              id="change-category"
              style={getFieldErrors(fieldErrors, 'categoryId').length ? selectError : selectStyle}
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
            >
              <option value="">— Select category —</option>
              {CATEGORIES.map((c) => (
                <option key={c.id} value={c.id}>{c.label}</option>
              ))}
            </select>
            {getFieldErrors(fieldErrors, 'categoryId').map((msg) => (
              <div key={msg} style={errorText} role="alert">{msg}</div>
            ))}
          </div>

          {/* --- Actors section --- */}
          <div style={sectionTitle}>Actors</div>

          <div style={fieldGroup}>
            <label style={labelStyle}>Assigned to</label>
            <ActorSelector
              value={assignedUserId}
              onChange={setAssignedUserId}
              placeholder="Search for a user or group to assign…"
              ariaLabel="Assigned to"
            />
          </div>

          <div style={fieldGroup}>
            <label style={labelStyle}>Observer</label>
            <ActorSelector
              value={observerId}
              onChange={setObserverId}
              placeholder="Search for an observer…"
              ariaLabel="Observer"
            />
          </div>

          {/* --- Linked Tickets section --- */}
          <div style={sectionTitle}>Linked Tickets</div>

          <div style={fieldGroup}>
            <label style={labelStyle} htmlFor="linked-ticket-input">
              Add Ticket ID
            </label>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <input
                id="linked-ticket-input"
                type="text"
                style={{ ...inputStyle, flex: 1 }}
                value={linkedTicketInput}
                onChange={(e) => setLinkedTicketInput(e.target.value)}
                placeholder="Enter ticket ID…"
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddLinkedTicket();
                  }
                }}
              />
              <button
                type="button"
                style={addItemBtn}
                onClick={handleAddLinkedTicket}
                aria-label="Add linked ticket"
              >
                + Add
              </button>
            </div>

            {linkedTicketIds.length > 0 && (
              <div style={{ ...linkedItemsArea, marginTop: '0.5rem' }}>
                {linkedTicketIds.map((ticketId) => (
                  <div key={ticketId} style={linkedItemRow}>
                    <span
                      style={{
                        padding: '0.2rem 0.5rem',
                        borderRadius: '4px',
                        backgroundColor: 'var(--tblr-bg-surface-secondary, #f5f7fb)',
                        border: '1px solid var(--tblr-border-color, #d9dbde)',
                        fontSize: '0.8125rem',
                        fontWeight: 500,
                      }}
                    >
                      🎫 #{ticketId}
                    </span>
                    <button
                      type="button"
                      style={removeBtn}
                      onClick={() => handleRemoveLinkedTicket(ticketId)}
                      aria-label={`Remove linked ticket #${ticketId}`}
                    >
                      ✕
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* --- Linked Problems section --- */}
          <div style={sectionTitle}>Linked Problems</div>

          <div style={fieldGroup}>
            <label style={labelStyle} htmlFor="linked-problem-input">
              Add Problem ID
            </label>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <input
                id="linked-problem-input"
                type="text"
                style={{ ...inputStyle, flex: 1 }}
                value={linkedProblemInput}
                onChange={(e) => setLinkedProblemInput(e.target.value)}
                placeholder="Enter problem ID…"
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddLinkedProblem();
                  }
                }}
              />
              <button
                type="button"
                style={addItemBtn}
                onClick={handleAddLinkedProblem}
                aria-label="Add linked problem"
              >
                + Add
              </button>
            </div>

            {linkedProblemIds.length > 0 && (
              <div style={{ ...linkedItemsArea, marginTop: '0.5rem' }}>
                {linkedProblemIds.map((problemId) => (
                  <div key={problemId} style={linkedItemRow}>
                    <span
                      style={{
                        padding: '0.2rem 0.5rem',
                        borderRadius: '4px',
                        backgroundColor: 'var(--tblr-bg-surface-secondary, #f5f7fb)',
                        border: '1px solid var(--tblr-border-color, #d9dbde)',
                        fontSize: '0.8125rem',
                        fontWeight: 500,
                      }}
                    >
                      ⚠️ #{problemId}
                    </span>
                    <button
                      type="button"
                      style={removeBtn}
                      onClick={() => handleRemoveLinkedProblem(problemId)}
                      aria-label={`Remove linked problem #${problemId}`}
                    >
                      ✕
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* --- Form actions --- */}
          <div style={formActions}>
            <button
              type="button"
              style={cancelBtn}
              onClick={() => navigate('/changes')}
            >
              Cancel
            </button>
            <button
              type="submit"
              style={isSubmitting ? submitBtnDisabled : submitBtn}
              disabled={isSubmitting}
              aria-busy={isSubmitting}
            >
              {isSubmitting ? 'Creating…' : 'Create Change'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
