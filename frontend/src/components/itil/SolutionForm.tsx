import { useState, useCallback } from 'react';
import type { CSSProperties } from 'react';
import type { SolutionFormData } from '@/hooks/useTickets';
import RichTextEditor from '@/components/common/RichTextEditor';

// ---------------------------------------------------------------------------
// SolutionForm — Rich text editor + solution type selector
// ---------------------------------------------------------------------------

export interface SolutionFormProps {
  onSubmit: (data: SolutionFormData) => void;
  onCancel: () => void;
  /** Available solution types for the selector. */
  solutionTypes?: { id: string; name: string }[];
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

const fieldLabel: CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  gap: '0.25rem',
  fontSize: '0.8125rem',
  color: 'var(--tblr-body-color, #1e293b)',
  marginTop: '0.75rem',
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

export default function SolutionForm({
  onSubmit,
  onCancel,
  solutionTypes = [],
}: SolutionFormProps) {
  const [content, setContent] = useState('');
  const [solutionTypeId, setSolutionTypeId] = useState('');

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      if (!content.trim()) return;
      onSubmit({
        content,
        solutionTypeId: solutionTypeId || undefined,
      });
    },
    [content, solutionTypeId, onSubmit],
  );

  return (
    <form onSubmit={handleSubmit} style={form} aria-label="Add solution form">
      <div style={heading}>Add Solution</div>

      <RichTextEditor
        value={content}
        onChange={setContent}
        placeholder="Describe the solution…"
        ariaLabel="Solution content"
      />

      {solutionTypes.length > 0 && (
        <label style={fieldLabel}>
          Solution type
          <select
            value={solutionTypeId}
            onChange={(e) => setSolutionTypeId(e.target.value)}
            style={fieldInput}
            aria-label="Solution type"
          >
            <option value="">— Select type —</option>
            {solutionTypes.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </label>
      )}

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
