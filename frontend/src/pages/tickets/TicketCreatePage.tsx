import { useState, useCallback, useMemo } from 'react';
import type { CSSProperties, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { useCreateTicket, type TicketCreateData } from '@/hooks/useTickets';
import { useAuthStore, type UserContext } from '@/stores/authStore';
import { mapValidationErrors } from '@/utils/validators';
import type { ApiError } from '@/api/types';
import RichTextEditor from '@/components/common/RichTextEditor';
import ActorSelector from '@/components/itil/ActorSelector';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const TICKET_TYPES = [
  { value: 1, label: 'Incident' },
  { value: 2, label: 'Request' },
] as const;

const URGENCY_OPTIONS = [
  { value: 1, label: 'Very low' },
  { value: 2, label: 'Low' },
  { value: 3, label: 'Medium' },
  { value: 4, label: 'High' },
  { value: 5, label: 'Very high' },
] as const;

// Placeholder categories — filtered by ticket type in a real deployment
const CATEGORIES: { id: string; label: string; type: number | null }[] = [
  { id: 'cat-1', label: 'Hardware', type: 1 },
  { id: 'cat-2', label: 'Software', type: 1 },
  { id: 'cat-3', label: 'Network', type: 1 },
  { id: 'cat-4', label: 'Access request', type: 2 },
  { id: 'cat-5', label: 'New equipment', type: 2 },
  { id: 'cat-6', label: 'General', type: null },
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

const requesterBadge: CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.4rem',
  padding: '0.3rem 0.6rem',
  borderRadius: '4px',
  backgroundColor: 'var(--tblr-bg-surface-secondary, #f5f7fb)',
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  fontSize: '0.8125rem',
  color: 'var(--tblr-body-color, #1e293b)',
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

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function isHelpdesk(user: UserContext | null): boolean {
  return user?.profileInterface === 'helpdesk';
}

function getFieldErrors(
  fieldErrors: Record<string, string[]>,
  field: string,
): string[] {
  return fieldErrors[field] ?? [];
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function TicketCreatePage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const createTicket = useCreateTicket();

  const helpdesk = isHelpdesk(user);

  // Form state
  const [ticketType, setTicketType] = useState<number>(1);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [urgency, setUrgency] = useState<number>(3);
  const [entityId, setEntityId] = useState(user?.entityId ?? '');
  const [assignedUserId, setAssignedUserId] = useState('');
  const [observerId, setObserverId] = useState('');

  // Validation state
  const [fieldErrors, setFieldErrors] = useState<Record<string, string[]>>({});
  const [generalErrorMsg, setGeneralErrorMsg] = useState('');

  // Categories filtered by selected ticket type
  const filteredCategories = useMemo(
    () => CATEGORIES.filter((c) => c.type === null || c.type === ticketType),
    [ticketType],
  );

  // Reset category when type changes and current selection is invalid
  const handleTypeChange = useCallback(
    (newType: number) => {
      setTicketType(newType);
      const valid = CATEGORIES.some(
        (c) => c.id === categoryId && (c.type === null || c.type === newType),
      );
      if (!valid) setCategoryId('');
    },
    [categoryId],
  );

  const handleSubmit = useCallback(
    async (e: FormEvent) => {
      e.preventDefault();
      setFieldErrors({});
      setGeneralErrorMsg('');

      // Build actors array — requester is always the current user
      const actors: TicketCreateData['actors'] = [];
      if (user) {
        actors.push({ actorType: 1, actorKind: 'user', actorId: user.userId });
      }
      if (!helpdesk && assignedUserId) {
        actors.push({ actorType: 2, actorKind: 'user', actorId: assignedUserId });
      }
      if (!helpdesk && observerId) {
        actors.push({ actorType: 3, actorKind: 'user', actorId: observerId });
      }

      const payload: TicketCreateData = {
        type: ticketType,
        title,
        content,
        entityId: entityId || (user?.entityId ?? ''),
        urgency,
        ...(categoryId ? { categoryId } : {}),
        actors,
      };

      try {
        const result = await createTicket.mutateAsync(payload);
        navigate(`/tickets/${result.data.id}`);
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
      ticketType, title, content, categoryId, urgency, entityId,
      assignedUserId, observerId, user, helpdesk, createTicket, navigate,
    ],
  );

  const isSubmitting = createTicket.isPending;

  return (
    <div style={pageContainer} role="main" aria-label="Create ticket">
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
        <h1 style={pageTitle}>
          {helpdesk ? 'Create a Ticket' : 'New Ticket'}
        </h1>
      </header>

      {/* Form */}
      <div style={formWrapper}>
        <form style={formCard} onSubmit={handleSubmit} noValidate>
          {generalErrorMsg && (
            <div style={generalError} role="alert">{generalErrorMsg}</div>
          )}

          {/* --- Type selector (central only) --- */}
          {!helpdesk && (
            <div style={fieldGroup}>
              <label style={labelStyle} htmlFor="ticket-type">
                Type<span style={requiredMark} aria-hidden="true">*</span>
              </label>
              <select
                id="ticket-type"
                style={getFieldErrors(fieldErrors, 'type').length ? selectError : selectStyle}
                value={ticketType}
                onChange={(e) => handleTypeChange(Number(e.target.value))}
                aria-required="true"
              >
                {TICKET_TYPES.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
              {getFieldErrors(fieldErrors, 'type').map((msg) => (
                <div key={msg} style={errorText} role="alert">{msg}</div>
              ))}
            </div>
          )}

          {/* --- Title --- */}
          <div style={fieldGroup}>
            <label style={labelStyle} htmlFor="ticket-title">
              Title<span style={requiredMark} aria-hidden="true">*</span>
            </label>
            <input
              id="ticket-title"
              type="text"
              style={getFieldErrors(fieldErrors, 'title').length ? inputError : inputStyle}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Brief description of the issue or request"
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
                placeholder="Describe the issue or request in detail…"
                ariaLabel="Ticket description"
              />
            </div>
            {getFieldErrors(fieldErrors, 'content').map((msg) => (
              <div key={msg} style={errorText} role="alert">{msg}</div>
            ))}
          </div>

          {/* --- Urgency & Category row --- */}
          <div style={twoCol}>
            <div style={fieldGroup}>
              <label style={labelStyle} htmlFor="ticket-urgency">
                Urgency<span style={requiredMark} aria-hidden="true">*</span>
              </label>
              <select
                id="ticket-urgency"
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
              <label style={labelStyle} htmlFor="ticket-category">Category</label>
              <select
                id="ticket-category"
                style={getFieldErrors(fieldErrors, 'categoryId').length ? selectError : selectStyle}
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
              >
                <option value="">— Select category —</option>
                {filteredCategories.map((c) => (
                  <option key={c.id} value={c.id}>{c.label}</option>
                ))}
              </select>
              {getFieldErrors(fieldErrors, 'categoryId').map((msg) => (
                <div key={msg} style={errorText} role="alert">{msg}</div>
              ))}
            </div>
          </div>

          {/* --- Entity (central only) --- */}
          {!helpdesk && (
            <div style={fieldGroup}>
              <label style={labelStyle} htmlFor="ticket-entity">Entity</label>
              <input
                id="ticket-entity"
                type="text"
                style={getFieldErrors(fieldErrors, 'entityId').length ? inputError : inputStyle}
                value={entityId}
                onChange={(e) => setEntityId(e.target.value)}
                placeholder={user?.entityName ?? 'Entity ID'}
              />
              {getFieldErrors(fieldErrors, 'entityId').map((msg) => (
                <div key={msg} style={errorText} role="alert">{msg}</div>
              ))}
            </div>
          )}

          {/* --- Actors section (central only) --- */}
          {!helpdesk && (
            <>
              <div style={sectionTitle}>Actors</div>

              {/* Requester — auto-populated, read-only */}
              <div style={fieldGroup}>
                <label style={labelStyle}>Requester</label>
                <div style={requesterBadge}>
                  <span aria-hidden="true">👤</span>
                  <span>{user?.username ?? 'Current user'}</span>
                </div>
              </div>

              {/* Assigned to */}
              <div style={fieldGroup}>
                <label style={labelStyle}>Assigned to</label>
                <ActorSelector
                  value={assignedUserId}
                  onChange={setAssignedUserId}
                  placeholder="Search for a user or group to assign…"
                  ariaLabel="Assigned to"
                />
              </div>

              {/* Observer */}
              <div style={fieldGroup}>
                <label style={labelStyle}>Observer</label>
                <ActorSelector
                  value={observerId}
                  onChange={setObserverId}
                  placeholder="Search for an observer…"
                  ariaLabel="Observer"
                />
              </div>
            </>
          )}

          {/* --- Form actions --- */}
          <div style={formActions}>
            <button
              type="button"
              style={cancelBtn}
              onClick={() => navigate('/tickets')}
            >
              Cancel
            </button>
            <button
              type="submit"
              style={isSubmitting ? submitBtnDisabled : submitBtn}
              disabled={isSubmitting}
              aria-busy={isSubmitting}
            >
              {isSubmitting ? 'Creating…' : 'Create Ticket'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
