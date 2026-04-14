import { useState, useCallback } from 'react';
import type { CSSProperties } from 'react';
import type { FollowupFormData } from '@/hooks/useTickets';
import RichTextEditor from '@/components/common/RichTextEditor';

// ---------------------------------------------------------------------------
// FollowupForm — Rich text editor + private/public toggle
// ---------------------------------------------------------------------------

export interface FollowupFormProps {
  onSubmit: (data: FollowupFormData) => void;
  onCancel: () => void;
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

export default function FollowupForm({ onSubmit, onCancel }: FollowupFormProps) {
  const [content, setContent] = useState('');
  const [isPrivate, setIsPrivate] = useState(false);

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      if (!content.trim()) return;
      onSubmit({ content, isPrivate });
    },
    [content, isPrivate, onSubmit],
  );

  return (
    <form onSubmit={handleSubmit} style={form} aria-label="Add followup form">
      <div style={heading}>Add Followup</div>

      <RichTextEditor
        value={content}
        onChange={setContent}
        placeholder="Write your followup…"
        ariaLabel="Followup content"
      />

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
